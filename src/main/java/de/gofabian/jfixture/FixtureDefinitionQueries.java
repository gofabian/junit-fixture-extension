package de.gofabian.jfixture;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FixtureDefinitionQueries {

    private final List<FixtureDefinition> definitions;

    public FixtureDefinitionQueries(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<FixtureDefinition> filterBy(Predicate<FixtureDefinition> predicate) {
        return definitions.stream().filter(predicate).collect(Collectors.toList());
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

    private static boolean supportsType(FixtureDefinition definition, Class<?> type) {
        return type.isAssignableFrom(definition.getType());
    }

}
