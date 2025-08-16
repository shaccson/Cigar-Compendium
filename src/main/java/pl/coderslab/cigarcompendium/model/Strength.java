package pl.coderslab.cigarcompendium.model;

public enum Strength {
    MILD("Mild"),
    MILD_MEDIUM("Mild-Medium"),
    MEDIUM("Medium"),
    MEDIUM_FULL("Medium-Full"),
    FULL("Full");

    private final String label;

    Strength(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
