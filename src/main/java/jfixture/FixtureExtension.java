package jfixture;

import jfixture.api.FixtureMethodParser;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class FixtureExtension implements TestInstancePostProcessor, ParameterResolver,
        BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Namespace NAMESPACE = Namespace.create(FixtureExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        var definitions = new FixtureMethodParser().parseFixtureDefinitions(testInstance);
        var bucket = new FixtureDefinitionBucket(definitions);
        setToStore(bucket, extensionContext);
        setToStore(new FixtureManager(bucket), extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var manager = getFromStore(FixtureManager.class, context);
        manager.setUp();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var bucket = getFromStore(FixtureDefinitionBucket.class, extensionContext);
        var type = parameterContext.getParameter().getType();
        return bucket.findByType(type) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var manager = getFromStore(FixtureManager.class, extensionContext);
        var type = parameterContext.getParameter().getType();
        return manager.setUp(type);
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
