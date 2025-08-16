package pl.coderslab.cigarcompendium.model;

public enum Origin {
    CUBA("Cuba"),
    DOMINICAN_REPUBLIC("Dominican Republic"),
    NICARAGUA("Nicaragua"),
    HONDURAS("Honduras"),
    MEXICO("Mexico"),
    ECUADOR("Ecuador"),
    BRAZIL("Brazil"),
    COSTA_RICA("Costa Rica"),
    USA("USA"),
    OTHER("Other");

    private final String label;

    Origin(String label) {
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
