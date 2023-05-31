package weka;

import controller.FileManager;
import enumerations.ClassifiersEnum;
import enumerations.CostSensitiveClassifiers;
import enumerations.FeatureSelection;
import enumerations.Sampling;
import weka.classifiers.Evaluation;

public class PerformanceManager {

    private static final String CSVHEADER = "Dataset,#TrainingRelease,Classifier,FeatureSelection,Sampling,CostSensitiveClassifier,Precision,Recall,AUC,Kappa";
    private static final String WALKFORWARDHEADER = "Dataset,Classifier,FeatureSelection,Sampling,CostSensitiveClassifier,Precision,Recall,AUC,Kappa";
    private static final String SEPARATOR = ",";
    private final String projName;
    private final FileManager fileManager;

    public PerformanceManager(String projName){
        this.projName = projName;
        fileManager = new FileManager(projName+"_Performance.csv");
    }

    public void initializePerformanceFile(){
        fileManager.writeLine(CSVHEADER);
        fileManager.writeLine("\n");
    }

    public void initializeWalkForwardFile(){
        fileManager.writeLine(WALKFORWARDHEADER);
        fileManager.writeLine("\n");
    }


    public void writeResults(Evaluation eval, int trainingRelease, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc){

        fileManager.writeLine(projName+SEPARATOR);
        fileManager.writeLine(trainingRelease+SEPARATOR);
        fileManager.writeLine(c.getName()+SEPARATOR);
        fileManager.writeLine(f.getName()+SEPARATOR);
        fileManager.writeLine(s.getName()+SEPARATOR);
        fileManager.writeLine(csc.getName()+SEPARATOR);
        fileManager.writeLine(eval.precision(0) +SEPARATOR);
        fileManager.writeLine(eval.recall(0) +SEPARATOR);
        fileManager.writeLine(eval.areaUnderROC(0) +SEPARATOR);
        fileManager.writeLine(eval.kappa() +"\n");
    }

    public void writeWalkForwardResults(Double precision, Double recall, Double auc, Double kappa, ClassifiersEnum c, FeatureSelection f, Sampling s, CostSensitiveClassifiers csc){

        fileManager.writeLine(projName+SEPARATOR);
        fileManager.writeLine(c.getName()+SEPARATOR);
        fileManager.writeLine(f.getName()+SEPARATOR);
        fileManager.writeLine(s.getName()+SEPARATOR);
        fileManager.writeLine(csc.getName()+SEPARATOR);
        fileManager.writeLine(precision +SEPARATOR);
        fileManager.writeLine(recall +SEPARATOR);
        fileManager.writeLine(auc +SEPARATOR);
        fileManager.writeLine(kappa +"\n");
    }

    public void close(){
        fileManager.close();
    }
}
