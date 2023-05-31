package entities;

import enumerations.ClassifiersEnum;
import enumerations.CostSensitiveClassifiers;
import enumerations.FeatureSelection;
import enumerations.Sampling;
import weka.classifiers.Evaluation;

import java.util.ArrayList;

public class WekaEval {
    private final ClassifiersEnum classifier;
    private final FeatureSelection featureSelection;
    private final Sampling sampling;
    private final CostSensitiveClassifiers costSensitiveClassifier;
    private final ArrayList<Evaluation> evaluationList = new ArrayList<>();

    public WekaEval(ClassifiersEnum classifier, FeatureSelection featureSelection, Sampling sampling, CostSensitiveClassifiers costSensitiveClassifier){
        this.classifier = classifier;
        this.featureSelection = featureSelection;
        this.sampling = sampling;
        this.costSensitiveClassifier = costSensitiveClassifier;
    }

    public ClassifiersEnum getClassifier(){
        return classifier;
    }

    public FeatureSelection getFeatureSelection(){
        return featureSelection;
    }

    public Sampling getSampling(){
        return sampling;
    }

    public CostSensitiveClassifiers getCostSensitiveClassifier(){
        return costSensitiveClassifier;
    }

    public ArrayList<Evaluation> getEvaluations(){
        return evaluationList;
    }

    public void addEvaluation(Evaluation e){
        this.evaluationList.add(e);
    }

}
