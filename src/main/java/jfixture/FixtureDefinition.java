package jfixture;

import java.util.List;

public abstract class FixtureDefinition {

    private final Class<?> type;
    private final List<FixtureDefinition> dependencies;
    private final boolean autoUse;

    protected FixtureDefinition(Class<?> type, List<FixtureDefinition> dependencies, boolean autoUse) {
        this.type = type;
        this.dependencies = dependencies;
        this.autoUse = autoUse;
    }

    public Class<?> getType() {
        return type;
    }

    public List<FixtureDefinition> getDependencies() {
        return dependencies;
    }

    public boolean isAutoUse() {
        return autoUse;
    }

    public abstract Object setUp(List<Object> dependencies);

    public abstract void tearDown(Object object);

}
