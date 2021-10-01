package jfixture;

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

    public boolean supports(Class<?> type) {
        var definition = definitions.findByType(type);
        return definition != null;
    }

    public Object resolve(Class<?> type) {
        var definition = definitions.findByType(type);
        if (definition == null) {
            throw new IllegalArgumentException("could not find fixture of type " + type);
        }
        return setUp(definition);
    }

    private Object setUp(FixtureDefinition definition) {
        var lifecycle = getFixtureLifecycle(definition);
        if (lifecycle.isSetUp()) {
            return lifecycle.getObject();
        }

        var dependencies = definition.getDependencyTypes().stream()
                .map(this::resolve).collect(Collectors.toList());
        var object = lifecycle.setUp(dependencies);

        session.orderedLifecycles.add(lifecycle);
        return object;
    }

    FixtureLifecycle getFixtureLifecycle(FixtureDefinition definition) {
        return session.definitionLifecycleMap.computeIfAbsent(definition, d -> new FixtureLifecycle(definition));
    }

}
