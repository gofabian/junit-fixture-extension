package jfixture;

import jfixture.parse.FixtureMethodParser;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class FixtureExtension implements TestInstancePostProcessor, ParameterResolver,
        BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Namespace NAMESPACE = Namespace.create(FixtureExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        var definitions = new FixtureMethodParser().parseFixtureDefinitions(testInstance);

        setToStore(new FixtureDefinitionQueries(definitions), extensionContext);
        setToStore(new FixtureManager(), extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var queries = getFromStore(FixtureDefinitionQueries.class, context);
        var manager = getFromStore(FixtureManager.class, context);

        for (var definition : queries.filterBy(FixtureDefinition::isAutoUse)) {
            manager.setUp(definition);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var queries = getFromStore(FixtureDefinitionQueries.class, extensionContext);

        var type = parameterContext.getParameter().getType();
        return queries.findByType(type) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var queries = getFromStore(FixtureDefinitionQueries.class, extensionContext);
        var manager = getFromStore(FixtureManager.class, extensionContext);

        var type = parameterContext.getParameter().getType();
        var definition = queries.findByType(type);
        return manager.setUp(definition);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        var manager = getFromStore(FixtureManager.class, extensionContext);
        manager.tearDown();
    }

    private <T> T getFromStore(Class<T> type, ExtensionContext extensionContext) {
        var store = extensionContext.getStore(NAMESPACE);
        return store.get(type, type);
    }

    private void setToStore(Object object, ExtensionContext extensionContext) {
        var store = extensionContext.getStore(NAMESPACE);
        store.put(object.getClass(), object);
    }
}
