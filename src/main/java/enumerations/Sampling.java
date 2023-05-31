package enumerations;

public enum Sampling {
    NO_SAMPLING("NoSampling"),
    OVERSAMPLING("Oversampling"),
    UNDERSAMPLING("Undersampling"),
    SMOTE("SMOTE");

    private final String name;
    Sampling(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
