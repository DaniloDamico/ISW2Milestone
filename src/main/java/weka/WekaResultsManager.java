package weka;

import controller.FileManager;
import enumerations.ClassifiersEnum;
import enumerations.CostSensitiveClassifiers;
import enumerations.FeatureSelection;
import enumerations.Sampling;
import weka.classifiers.Evaluation;

public class WekaResultsManager {

    private static final String CSVHEADER = "Testing Release,Classifier,FeatureSelection,Sampling,CostSensitiveClassifier,True Positives,False Positives,True Negatives,False Negatives,Precision,Recall,AUC,Kappa";
    private static final String SEPARATOR = ",";
    private final FileManager fileManager;

    public WekaResultsManager(String projName){
        fileManager = new FileManager(projName+"_weka_results.csv");
        fileManager.writeLine(CSVHEADER);
        fileManager.writeLine("\n");
    }


    public void writeResults(Evaluation eval, int testingRelease, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc){

        fileManager.writeLine(testingRelease+SEPARATOR);
        fileManager.writeLine(c.getName()+SEPARATOR);
        fileManager.writeLine(f.getName()+SEPARATOR);
        fileManager.writeLine(s.getName()+SEPARATOR);
        fileManager.writeLine(csc.getName()+SEPARATOR);
        fileManager.writeLine(eval.truePositiveRate(0)+SEPARATOR);
        fileManager.writeLine(eval.falsePositiveRate(0)+SEPARATOR);
        fileManager.writeLine(eval.trueNegativeRate(0)+SEPARATOR);
        fileManager.writeLine(eval.falseNegativeRate(0)+SEPARATOR);
        fileManager.writeLine(eval.precision(0) +SEPARATOR);
        fileManager.writeLine(eval.recall(0) +SEPARATOR);
        fileManager.writeLine(eval.areaUnderROC(0) +SEPARATOR);
        double kappa = eval.kappa();
        if(kappa <= -1 ) kappa = -1;
        fileManager.writeLine(kappa +"\n");

    }

    public void close(){
        fileManager.close();
    }
}
