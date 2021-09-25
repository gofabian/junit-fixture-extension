package jfixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixtureManager {

    private final List<FixtureDefinition> definitions;
    private final Map<Class<?>, FixtureLifecycle> typeLifecycleMap = new LinkedHashMap<>();
    private final FixtureResolver resolver = new FixtureResolver(this);

    public FixtureManager(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public void setUp() {
        for (var definition : definitions) {
            if (definition.isAutoUse()) {
                setUpDefinition(definition);
            }
        }
    }

    public Object setUp(Class<?> type) {
        var definition = getFixtureDefinition(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        return setUpDefinition(definition);
    }

    private Object setUpDefinition(FixtureDefinition definition) {
        var lifecycle = getFixtureLifecycle(definition);
        var object = lifecycle.setUp(resolver);
        typeLifecycleMap.put(definition.getType(), lifecycle);
        return object;
    }

    public FixtureDefinition getFixtureDefinition(Class<?> type) {
        var it = definitions.listIterator(definitions.size());
        while (it.hasPrevious()) {
            var definition = it.previous();
            if (type.isAssignableFrom(definition.getType())) {
                return definition;
            }
        }
        return null;
    }

    FixtureLifecycle getFixtureLifecycle(Class<?> type) {
        var definition = getFixtureDefinition(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        return getFixtureLifecycle(definition);
    }

    private FixtureLifecycle getFixtureLifecycle(FixtureDefinition definition) {
        var lifecycle = typeLifecycleMap.get(definition.getType());
        if (lifecycle != null) {
            return lifecycle;
        }
        return new FixtureLifecycle(definition);
    }

    public void tearDown() {
        var lifecycles = new ArrayList<>(typeLifecycleMap.values());
        var it = lifecycles.listIterator(lifecycles.size());
        while (it.hasPrevious()) {
            it.previous().tearDown();
        }
    }

}
