package jfixture;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixtureExtension implements TestInstancePostProcessor, ParameterResolver,
        BeforeAllCallback, AfterAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Namespace NAMESPACE = Namespace.create(FixtureExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        var parser = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent("parser", k -> new FixtureMethodParser(), FixtureMethodParser.class);
        var plusDefinitions = parser.parseInstance(testInstance);
        resetManager(plusDefinitions, context);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(NAMESPACE);
        store.put("afterEngineCallback", (ExtensionContext.Store.CloseableResource) () -> afterSession(context));
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var manager = getManager(context);
        manager.enter(Scope.SESSION);
        manager.enter(Scope.CLASS);
        manager.enter(Scope.METHOD);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        var manager = getManager(extensionContext);
        manager.leave(Scope.METHOD);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        var manager = getManager(context);
        manager.leave(Scope.CLASS);
    }

    public void afterSession(ExtensionContext context) {
        var manager = getManager(context);
        manager.leave(Scope.SESSION);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var manager = getManager(extensionContext);
        var type = parameterContext.getParameter().getType();
        return manager.supports(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var manager = getManager(extensionContext);
        var type = parameterContext.getParameter().getType();
        return manager.resolve(type);
    }

    private FixtureManager getManager(ExtensionContext context) {
        var store = context.getStore(NAMESPACE);
        return store.getOrComputeIfAbsent("manager", k -> resetManager(Collections.emptyList(), context), FixtureManager.class);
    }

    private FixtureManager resetManager(List<FixtureDefinition> plusDefinitions, ExtensionContext context) {
        var session = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent("session", k -> new FixtureSession(), FixtureSession.class);
        var store = context.getStore(NAMESPACE);
        //noinspection unchecked
        var oldDefinitions = (List<FixtureDefinition>) store.getOrComputeIfAbsent("definitions", k -> new ArrayList<>());
        var definitions = new ArrayList<>(oldDefinitions);
        definitions.addAll(plusDefinitions);
        var manager = new FixtureManager(session, definitions);
        store.put("manager", new FixtureManager(session, plusDefinitions));
        return manager;
    }

}
