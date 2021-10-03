package jfixture;

import java.util.List;

public abstract class FixtureDefinition {

    private final Scope scope;
    private final Class<?> type;
    private final List<Class<?>> dependencyTypes;
    private final boolean autoUse;

    protected FixtureDefinition(Scope scope, Class<?> type, List<Class<?>> dependencyTypes, boolean autoUse) {
        this.scope = scope;
        this.type = type;
        this.dependencyTypes = dependencyTypes;
        this.autoUse = autoUse;
    }

    public Scope getScope() {
        return scope;
    }

    public Class<?> getType() {
        return type;
    }

    public List<Class<?>> getDependencyTypes() {
        return dependencyTypes;
    }

    public boolean isAutoUse() {
        return autoUse;
    }

    public abstract Object setUp(List<Object> dependencies);

    public abstract void tearDown(Object object);

    @Override
    public String toString() {
        return "FixtureDefinition{" +
                "scope=" + scope +
                ", type=" + type +
                ", dependencyTypes=" + dependencyTypes +
                ", autoUse=" + autoUse +
                '}';
    }

}
