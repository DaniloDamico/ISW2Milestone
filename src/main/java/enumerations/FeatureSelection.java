package enumerations;

public enum FeatureSelection {
    NO_SELECTION("NoSelection"),
    BEST_FIRST("BestFirst");

    private final String name;
    FeatureSelection(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
