package jfixture;

import jfixture.api.FixtureMethodParser;
import org.junit.jupiter.api.extension.*;

public class FixtureExtension implements TestInstancePostProcessor, ParameterResolver, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(FixtureExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        var store = extensionContext.getStore(NAMESPACE);
        var definitions = new FixtureMethodParser().parseFixtureDefinitions(testInstance);
        store.put("manager", new FixtureManager(definitions));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var manager = getManager(extensionContext);
        var type = parameterContext.getParameter().getType();
        return manager.findFixtureDefinition(type) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var manager = getManager(extensionContext);
        var type = parameterContext.getParameter().getType();
        var lifecycle = manager.getFixtureLifecycle(type);
        return lifecycle.setUp();
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        var manager = getManager(extensionContext);
        var lifecycles = manager.getFixtureLifecycles();
        var it = lifecycles.listIterator(lifecycles.size());
        while (it.hasPrevious()) {
            it.previous().tearDown();
        }
    }

    private FixtureManager getManager(ExtensionContext extensionContext) {
        var store = extensionContext.getStore(NAMESPACE);
        return store.get("manager", FixtureManager.class);
    }
}
