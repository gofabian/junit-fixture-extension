package de.gofabian.jfixture;

import de.gofabian.jfixture.api.FixtureId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FixtureManager {

    private final FixtureSession session;
    private final FixtureDefinitionQueries definitions;

    public FixtureManager(FixtureSession session, List<FixtureDefinition> definitions) {
        this.session = session;
        this.definitions = new FixtureDefinitionQueries(definitions);
    }

    public void enter(Scope scope) {
        for (var definition : definitions.filterBy(d -> d.getScope() == scope && d.isAutoUse())) {
            setUp(definition);
        }
    }

    public void leave(Scope scope) {
        var lifecycles = session.orderedLifecycles.stream()
                .filter(l -> l.getDefinition().getScope() == scope)
                .collect(Collectors.toList());
        var it = lifecycles.listIterator(lifecycles.size());
        while (it.hasPrevious()) {
            var lifecycle = it.previous();
            lifecycle.tearDown();
            session.orderedLifecycles.remove(lifecycle);
        }
    }

    public boolean supports(FixtureId id) {
        var definition = definitions.findById(id);
        return definition != null;
    }

    public Object resolve(FixtureId id) {
        var definition = getFixtureDefinition(id);
        return setUp(definition);
    }

    private Object setUp(FixtureDefinition definition) {
        var lifecycle = getFixtureLifecycle(definition);

        if (lifecycle.isSetUp()) {
            return lifecycle.getObject();
        }

        var dependencies = new ArrayList<>();
        for (var type : definition.getDependencyIds()) {
            var dependencyDefinition = getFixtureDefinition(type);
            if (dependencyDefinition.getScope().getOrder() > definition.getScope().getOrder()) {
                throw new IllegalArgumentException("Fixture has wider scope than dependency: " +
                        "fixture=" + definition + ", dependency: " + dependencyDefinition);
            }
            var dependency = setUp(dependencyDefinition);
            dependencies.add(dependency);
        }

        var object = lifecycle.setUp(dependencies);
        session.orderedLifecycles.add(lifecycle);
        return object;
    }

    private FixtureDefinition getFixtureDefinition(FixtureId id) {
        var definition = definitions.findById(id);
        if (definition == null) {
            throw new IllegalArgumentException("could not find fixture by id " + id);
        }
        return definition;
    }

    FixtureLifecycle getFixtureLifecycle(FixtureDefinition definition) {
        return session.definitionLifecycleMap.computeIfAbsent(definition, d -> new FixtureLifecycle(definition));
    }

}
