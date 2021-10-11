package de.gofabian.jfixture;

import de.gofabian.jfixture.api.FixtureId;

import java.util.List;

public abstract class FixtureDefinition {

    private final Scope scope;
    private final FixtureId id;
    private final List<FixtureId> dependencyIds;
    private final boolean autoUse;

    protected FixtureDefinition(Scope scope, FixtureId id, List<FixtureId> dependencyIds, boolean autoUse) {
        this.scope = scope;
        this.id = id;
        this.dependencyIds = dependencyIds;
        this.autoUse = autoUse;
    }

    public Scope getScope() {
        return scope;
    }

    public FixtureId getId() {
        return id;
    }

    public List<FixtureId> getDependencyIds() {
        return dependencyIds;
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
                ", id=" + id +
                ", dependencyIds=" + dependencyIds +
                ", autoUse=" + autoUse +
                '}';
    }

}
