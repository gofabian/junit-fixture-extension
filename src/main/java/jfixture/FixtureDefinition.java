package jfixture;

public abstract class FixtureDefinition {

    private final Class<?> type;

    protected FixtureDefinition(Class<?> type) {
        this.type = type;
    }

    public abstract Object setUp(FixtureResolver resolver);

    public abstract void tearDown(Object object);

    public Class<?> getType() {
        return type;
    }
}
