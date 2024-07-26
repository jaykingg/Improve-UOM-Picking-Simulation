public enum UnitOfMeasure {
    EA("Each"),
    PK("Pack"),
    CS("Case");

    private final String label;


    UnitOfMeasure(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

