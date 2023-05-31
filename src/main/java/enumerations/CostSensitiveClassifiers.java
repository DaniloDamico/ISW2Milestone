package enumerations;

public enum CostSensitiveClassifiers {
    NO_COST_SENSITIVE("NoCostSensitive"),
    SENSITIVE_THRESHOLD("SensitiveThreshold"),
    SENSITIVE_LEARNING("SensitiveLearning");

    private final String name;
    CostSensitiveClassifiers(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
