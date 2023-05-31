package weka;

import entities.WekaEval;
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
import java.util.List;

public class WekaManager{

    private static final String FOLDER = "output/";
    private static final List<String> PROJLIST = new ArrayList<>(List.of("BOOKKEEPER", "SYNCOPE"));
    private static PerformanceManager performanceManager;

    private static ArrayList<WekaEval> evals;
    public static void main(String[] args) throws Exception{

        for(String projName:PROJLIST){
            evals = new ArrayList<>();
            Instances dataset = loadData(projName);
            assert dataset != null;
            performanceManager = new PerformanceManager(projName);
            performanceManager.initializePerformanceFile();
            walkForward(dataset);
            performanceManager.close();
            computeWalkForwardDataset(projName);
        }

    }

    private static void computeWalkForwardDataset(String projName){
        PerformanceManager walkForwardManager = new PerformanceManager(projName + "WalkForward");
        walkForwardManager.initializeWalkForwardFile();
        for(WekaEval e:evals){

            int numInstances = e.getEvaluations().size();
            double precision = 0;
            double recall = 0;
            double auc = 0;
            double kappa = 0;
            for (Evaluation evaluation : e.getEvaluations()) {
                precision += evaluation.precision(0);
                recall += evaluation.recall(0);
                auc += evaluation.areaUnderROC(0);
                kappa += evaluation.kappa();
            }
            precision /= numInstances;
            recall /= numInstances;
            auc /= numInstances;
            kappa /= numInstances;

            Double[] performance = {precision, recall, auc, kappa};

            walkForwardManager.writeWalkForwardResults(performance, e.getClassifier(), e.getFeatureSelection(), e.getSampling(), e.getCostSensitiveClassifier());
        }
        walkForwardManager.close();
    }

    private static Instances loadData(String projName) {
        try {
            DataSource data = new DataSource(FOLDER + projName + "_dataset.arff");
            Instances instances = data.getDataSet();
            instances.setClassIndex(instances.numAttributes()-1); // Assuming last attribute as the class attribute
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

        double currVersion = dataset.get(0).value(0);

        for (Instance row : dataset) {
            if(row.value(0)==currVersion){
                testing.add(row);
            } else{
                if(!training.isEmpty()){
                    setupAndRunWeka(training, testing);
                }

                currVersion = row.value(0);
                training.addAll(testing);
                testing.clear();
                testing.add(row);
            }
        }
        setupAndRunWeka(training, testing);

    }

    private static void setupAndRunWeka(Instances training, Instances testing) throws Exception{

        int numberOfTrainingRelease = (int) training.lastInstance().value(0);

        for(FeatureSelection f : FeatureSelection.values()) {
            for(Sampling s : Sampling.values()) {
                for(CostSensitiveClassifiers csc : CostSensitiveClassifiers.values()) {
                    for (ClassifiersEnum c : ClassifiersEnum.values()) {
                        Evaluation eval = runWeka(training, testing, c, f, s, csc);
                        performanceManager.writeResults(eval, numberOfTrainingRelease, c, f, s, csc);
                        addToEvalList(eval, c, f, s, csc);
                    }
                }
            }
        }
    }

    private static void addToEvalList(Evaluation eval, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc) {
        for(WekaEval e: evals){
            if(e.getClassifier().equals(c) && e.getFeatureSelection().equals(f) && e.getSampling().equals(s) && e.getCostSensitiveClassifier().equals(csc)){
                e.addEvaluation(eval);
                return;
            }
        }
        evals.add(new WekaEval(c, f, s, csc));
    }

    public static Evaluation runWeka(Instances training, Instances testing, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc) throws Exception {

        StringToNominal filter = new StringToNominal();
        String[] options = new String[]{ "-R", "2"}; // converts filenames to numeric values
        filter.setOptions(options);

        filter.setInputFormat(training);
        training = Filter.useFilter(training, filter);
        testing = Filter.useFilter(testing, filter);

        double y = computeBalancingPercentage(training);


        //feature selection
        if(f.equals(FeatureSelection.BEST_FIRST)) {
            // Apply the feature selection algorithm
            AttributeSelection featureSelectionFilter = new AttributeSelection();
            CfsSubsetEval evaluator = new CfsSubsetEval();
            BestFirst search = new BestFirst();

            featureSelectionFilter.setEvaluator(evaluator);
            featureSelectionFilter.setSearch(search);
            featureSelectionFilter.setInputFormat(training);

            training = Filter.useFilter(training, featureSelectionFilter);
            testing = Filter.useFilter(testing, featureSelectionFilter);
        }

        //sampling
        switch (s) {
            case NO_SAMPLING -> {
                //do nothing
            }
            case OVERSAMPLING -> {
                Resample resample = new Resample();

                resample.setInputFormat(training);
                resample.setNoReplacement(false);
                // samples minority instances until they match majority instances in number
                resample.setBiasToUniformClass(1.0);
                resample.setSampleSizePercent(y);
                training = Filter.useFilter(training, resample);

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
                String[] opts = new String[]{ "-P", String.valueOf(y)};
                smote.setOptions(opts);

                training = Filter.useFilter(training, smote);
            }
            default -> throw new IllegalStateException("Unexpected Sampling value: " + s);
        }

        Classifier classifier;

        // classifier
        switch (c) {
            case RANDOM_FOREST -> classifier = new RandomForest();
            case NAIVE_BAYES -> classifier = new NaiveBayes();
            case IBK -> classifier = new IBk();
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

    private static double computeBalancingPercentage(Instances training) {
        // Calculate the number of instances in each class
        int minorityClassSize = 0;
        int majorityClassSize = 0;
        for (int i = 0; i < training.numInstances(); i++) {
            if (training.instance(i).stringValue(training.numAttributes()-1).equals("yes")) {
                minorityClassSize++;
            } else {
                majorityClassSize++;
            }
        }

        // The size of the output dataset, as a percentage of the input dataset
        if(minorityClassSize != 0)
            return 100.0*(majorityClassSize - minorityClassSize)/ minorityClassSize;
        else return majorityClassSize;
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
