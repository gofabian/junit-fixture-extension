package jfixture;

import java.util.List;
import java.util.stream.Collectors;

public class FixtureDefinitionBucket {

    private final List<FixtureDefinition> definitions;

    public FixtureDefinitionBucket(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<FixtureDefinition> findByAutoUse() {
        return definitions.stream().filter(FixtureDefinition::isAutoUse).collect(Collectors.toList());
    }

    public FixtureDefinition findByType(Class<?> type) {
        var it = definitions.listIterator(definitions.size());
        while (it.hasPrevious()) {
            var definition = it.previous();
            if (supportsType(definition, type)) {
                return definition;
            }
        }
        return null;
    }

    public static boolean supportsType(FixtureDefinition definition, Class<?> type) {
        return type.isAssignableFrom(definition.getType());
    }

}
