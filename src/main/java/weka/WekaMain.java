package weka;


import enumerations.ClassifiersEnum;
import enumerations.CostSensitiveClassifiers;
import enumerations.FeatureSelection;
import enumerations.Sampling;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;

import static weka.WekaManager.walkForward;

public class WekaMain {

    private static final List<String> PROJLIST = new ArrayList<>(List.of("BOOKKEEPER", "SYNCOPE"));
    private static final String FOLDER = "output/";

    public static void main(String[] args) throws Exception{
        for(String projName:PROJLIST){
            Instances dataset = loadData(projName);
            assert dataset != null;

            WekaResultsManager wekaResultsManager = new WekaResultsManager(projName);
            allPossibilities(dataset, wekaResultsManager);
            wekaResultsManager.close();
        }

    }

    private static void allPossibilities(Instances dataset, WekaResultsManager wrm) throws Exception {
        for(FeatureSelection f : FeatureSelection.values()) {
            for(Sampling s : Sampling.values()) {
                for(CostSensitiveClassifiers csc : CostSensitiveClassifiers.values()) {
                    for (ClassifiersEnum c : ClassifiersEnum.values()) {
                        Instances datasetCopy = new Instances(dataset);
                        walkForward(datasetCopy,wrm,c,f,s,csc);
                    }
                }
            }
        }
    }


    private static Instances loadData(String projName) {
        try {
            ConverterUtils.DataSource data = new ConverterUtils.DataSource(FOLDER + projName + "_dataset.arff");
            Instances instances = data.getDataSet();
            instances.setClassIndex(instances.numAttributes()-1);
            return instances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
