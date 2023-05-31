package enumerations;

public enum ClassifiersEnum {
    RANDOM_FOREST("RandomForest"),
    NAIVE_BAYES("NaiveBayes"),
    IBK("IBK");

    private final String name;
    ClassifiersEnum(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }

}
