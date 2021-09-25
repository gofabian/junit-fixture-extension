package jfixture;

public abstract class FixtureDefinition {

    private final Class<?> type;
    private final boolean autoUse;

    protected FixtureDefinition(Class<?> type, boolean autoUse) {
        this.type = type;
        this.autoUse = autoUse;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isAutoUse() {
        return autoUse;
    }

    public abstract Object setUp(FixtureResolver resolver);

    public abstract void tearDown(Object object);

}
