package jfixture;

public class FixtureLifecycle {
    private final FixtureDefinition definition;
    private boolean isSetUp = false;
    private Object object;

    public FixtureLifecycle(FixtureDefinition definition) {
        this.definition = definition;
    }

    public Object setUp(FixtureResolver resolver) {
        if (!isSetUp) {
            object = definition.setUp(resolver);
            isSetUp = true;
        }
        return object;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public void tearDown() {
        if (!isSetUp) {
            throw new IllegalStateException("not set up");
        }
        definition.tearDown(object);
        object = null;
        isSetUp = false;
    }
}
