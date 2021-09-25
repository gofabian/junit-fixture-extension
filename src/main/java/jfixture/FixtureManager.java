package jfixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixtureManager {

    private final FixtureDefinitionBucket definitions;
    private final Map<FixtureDefinition, FixtureLifecycle> definitionLifecycleMap = new HashMap<>();
    private final List<FixtureLifecycle> setUpLifecycles = new ArrayList<>();
    private final FixtureResolver resolver = new FixtureResolver(this);

    public FixtureManager(FixtureDefinitionBucket definitions) {
        this.definitions = definitions;
    }

    public void setUp() {
        for (var definition : definitions.findByAutoUse()) {
            setUpDefinition(definition);
        }
    }

    public Object setUp(Class<?> type) {
        var definition = definitions.findByType(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        return setUpDefinition(definition);
    }

    private Object setUpDefinition(FixtureDefinition definition) {
        var lifecycle = getFixtureLifecycle(definition);
        var object = lifecycle.setUp(resolver);
        setUpLifecycles.add(lifecycle);
        return object;
    }

    FixtureLifecycle getFixtureLifecycle(Class<?> type) {
        var definition = definitions.findByType(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        return getFixtureLifecycle(definition);
    }

    private FixtureLifecycle getFixtureLifecycle(FixtureDefinition definition) {
        return definitionLifecycleMap.computeIfAbsent(definition, d -> new FixtureLifecycle(definition));
    }

    public void tearDown() {
        var it = setUpLifecycles.listIterator(setUpLifecycles.size());
        while (it.hasPrevious()) {
            it.previous().tearDown();
            it.remove();
        }
    }

}
