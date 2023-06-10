package weka;

import enumerations.ClassifiersEnum;
import enumerations.CostSensitiveClassifiers;
import enumerations.FeatureSelection;
import enumerations.Sampling;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WekaManager{

    private static final String FOLDER = "output/";
    private static final List<String> PROJLIST = new ArrayList<>(List.of("BOOKKEEPER", "SYNCOPE"));
    private static WekaResultsManager wekaResultsManager;

    public static void main(String[] args) throws Exception{

        for(String projName:PROJLIST){
            Instances dataset = loadData(projName);
            assert dataset != null;

            wekaResultsManager = new WekaResultsManager(projName);

            walkForward(dataset);
            wekaResultsManager.close();
        }

    }

    private static Instances loadData(String projName) {
        try {
            DataSource data = new DataSource(FOLDER + projName + "_dataset.arff");
            Instances instances = data.getDataSet();
            instances.setClassIndex(instances.numAttributes()-1);
            return instances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void walkForward(Instances dataset) throws Exception {

        Instances training = new Instances(dataset,0);
        Instances testing = new Instances(dataset,0);

        training.setClassIndex(training.numAttributes() - 1);
        testing.setClassIndex(testing.numAttributes() - 1);


        HashSet<Integer> uniqueValues = new HashSet<>();
        for(Instance row: dataset)  uniqueValues.add((int) row.value(0));
        List<Integer> versionsList = new ArrayList<>(uniqueValues);
        Collections.sort(versionsList);

        for(int ver:versionsList){
            for(Instance row: dataset){
                if(row.value(0)==ver){
                    testing.add(row);
                }
            }

            if(!training.isEmpty()){
                Instances trainingCopy = new Instances(training);
                Instances testingCopy = new Instances(testing);
                setupAndRunWeka(trainingCopy, testingCopy);
            }

            training.addAll(testing);
            testing.clear();
        }
    }

    private static void setupAndRunWeka(Instances training, Instances testing) throws Exception{

        int testingRelease = (int) testing.get(0).value(0);

        for(FeatureSelection f : FeatureSelection.values()) {
            for(Sampling s : Sampling.values()) {
                for(CostSensitiveClassifiers csc : CostSensitiveClassifiers.values()) {
                    for (ClassifiersEnum c : ClassifiersEnum.values()) {
                        Instances trainingCopy = new Instances(training);
                        Instances testingCopy = new Instances(testing);
                        Evaluation eval = runWeka(trainingCopy, testingCopy, c, f, s, csc);
                        wekaResultsManager.writeResults(eval, testingRelease, c, f, s, csc);
                    }
                }
            }
        }
    }

    public static Evaluation runWeka(Instances training, Instances testing, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc) throws Exception {

        StringToNominal filter = new StringToNominal();
        String[] options = new String[]{ "-R", "2"}; // converts filenames to numeric values
        filter.setOptions(options);

        filter.setInputFormat(training);
        training = Filter.useFilter(training, filter);
        testing = Filter.useFilter(testing, filter);

        //feature selection
        if(f.equals(FeatureSelection.BEST_FIRST)) {
            // Apply the feature selection algorithm
            AttributeSelection featureSelectionFilter = new AttributeSelection();
            CfsSubsetEval evaluator = new CfsSubsetEval();
            BestFirst search = new BestFirst();

            String[] evalOptions = {"-P", "1", "-E", "1"};
            evaluator.setOptions(evalOptions);

            String[] searchOptions = {"-D", "1", "-N", "5"};
            search.setOptions(searchOptions);

            featureSelectionFilter.setEvaluator(evaluator);
            featureSelectionFilter.setSearch(search);
            featureSelectionFilter.setInputFormat(training);

            training = Filter.useFilter(training, featureSelectionFilter);
            testing = Filter.useFilter(testing, featureSelectionFilter);
        }

        Instances minorityClassInstances = new Instances(training, 0);
        Instances majorityClassInstances = new Instances(training, 0);
        for (Instance instance : training) {
            if (instance.stringValue(training.numAttributes()-1).equals("yes")) {
                minorityClassInstances.add(instance);
            } else {
                majorityClassInstances.add(instance);
            }
        }

        // Calculate the oversampling ratio
        int minoritySize = minorityClassInstances.size();
        int majoritySize = majorityClassInstances.size();
        double oversamplingRatio = (double) majoritySize / minoritySize;

        //sampling
        switch (s) {
            case NO_SAMPLING -> {
                //do nothing
            }
            case OVERSAMPLING -> {

                // Apply Resample filter for oversampling
                Resample resampleFilter = new Resample();
                resampleFilter.setSampleSizePercent(oversamplingRatio*100);
                resampleFilter.setBiasToUniformClass(1.0);

                // Oversample the minority class
                resampleFilter.setInputFormat(minorityClassInstances);
                Instances oversampledMinorityInstances = Filter.useFilter(minorityClassInstances, resampleFilter);

                // Combine oversampled minority instances with majority instances
                Instances oversampledData = new Instances(training, 0);
                oversampledData.addAll(majorityClassInstances);
                oversampledData.addAll(oversampledMinorityInstances);

                training = oversampledData;

            }
            case UNDERSAMPLING -> {
                SpreadSubsample spreadSubsample = new SpreadSubsample();
                String[] opts = new String[]{ "-M", "1.0"};

                spreadSubsample.setOptions(opts);
                spreadSubsample.setInputFormat(training);
                training = Filter.useFilter(training, spreadSubsample);

            }
            case SMOTE -> {
                SMOTE smote = new SMOTE();
                smote.setInputFormat(training);

                //make both groups the same dimension
                String percentageToCreate = "0";
                if(minoritySize !=0)
                    percentageToCreate= String.valueOf((majoritySize-minoritySize)/(double)minoritySize*100.0);

                String[] opts = new String[]{ "-P", percentageToCreate};
                smote.setOptions(opts);

                training = Filter.useFilter(training, smote);
            }
            default -> throw new IllegalStateException("Unexpected Sampling value: " + s);
        }

        Classifier classifier;

        // classifier
        switch (c) {
            case RANDOM_FOREST -> {
                classifier = new RandomForest();
            }
            case NAIVE_BAYES -> classifier = new NaiveBayes();
            case IBK -> {
                classifier = new IBk();
            }
            default -> throw new IllegalStateException("Unexpected Classifier value: " + c);
        }

        Evaluation eval = new Evaluation(testing);

        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setClassifier(classifier);

        // cost sensitive classifier
        switch (csc) {
            case NO_COST_SENSITIVE -> {
                classifier.buildClassifier(training);

                eval.evaluateModel(classifier, testing);
            }
            case SENSITIVE_THRESHOLD -> {

                costSensitiveClassifier.setCostMatrix( createCostMatrix(1));
                costSensitiveClassifier.buildClassifier(training);
                eval.evaluateModel(classifier, testing);
            }
            case SENSITIVE_LEARNING -> {
                costSensitiveClassifier.setCostMatrix( createCostMatrix(10));
                costSensitiveClassifier.buildClassifier(training);
                eval.evaluateModel(classifier, testing);
            }
            default -> throw new IllegalStateException("Unexpected Cost Sensitive Classifier value: " + c);
        }

        return eval;
    }

    private static CostMatrix createCostMatrix(double
            weightFalseNegative){
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, (double) 1);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }

}
