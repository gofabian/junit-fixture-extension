package jfixture.parse;

import jfixture.FixtureDefinition;

import java.util.List;

public class FixtureMethodParser {

    public List<FixtureDefinition> parseFixtureDefinitions(Object testInstance) {
        var methods = new FixtureMethodCollector().collectMethodsFromInstance(testInstance);
        return new FixtureDefinitionFactory().createFixtureDefinitions(methods);
    }

}
