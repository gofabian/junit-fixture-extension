package de.gofabian.jfixture;

import java.util.List;

public class FixtureLifecycle {
    private final FixtureDefinition definition;
    private boolean isSetUp = false;
    private Object object;

    public FixtureLifecycle(FixtureDefinition definition) {
        this.definition = definition;
    }

    public FixtureDefinition getDefinition() {
        return definition;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public Object getObject() {
        return object;
    }

    public Object setUp(List<Object> dependencies) {
        if (!isSetUp) {
            object = definition.setUp(dependencies);
            isSetUp = true;
        }
        return object;
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
