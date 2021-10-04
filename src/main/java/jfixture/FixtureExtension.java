package jfixture;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixtureExtension implements TestInstancePostProcessor, ParameterResolver,
        BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {

    private static final Namespace NAMESPACE = Namespace.create(FixtureExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContextUtil.registerAfterSessionCallback(context, this::afterSession);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        var parser = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent("parser", k -> new FixtureMethodParser(), FixtureMethodParser.class);
        var definitions = parser.parseInstance(testInstance);
        storeAdditionalDefinitions(definitions, context);
    }


    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var manager = getManager(context);
        manager.enter(Scope.SESSION);
        manager.enter(Scope.FILE);
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

        if (ExtensionContextUtil.isOuterClassContext(context)) {
            manager.leave(Scope.FILE);
        }
    }

    private void afterSession(ExtensionContext context) {
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
        return store.getOrComputeIfAbsent("manager", k -> storeEmptyManager(context), FixtureManager.class);
    }

    private FixtureManager storeAdditionalDefinitions(List<FixtureDefinition> plusDefinitions, ExtensionContext context) {
        // store definitions
        var store = context.getStore(NAMESPACE);
        //noinspection unchecked
        var parentDefinitions = (List<FixtureDefinition>) store.getOrDefault("definitions", List.class, new ArrayList<>());
        var childDefinitions = new ArrayList<>(parentDefinitions);
        childDefinitions.addAll(plusDefinitions);
        store.put("definitions", childDefinitions);

        // store manager
        var session = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent("session", k -> new FixtureSession(), FixtureSession.class);
        var manager = new FixtureManager(session, childDefinitions);
        context.getStore(NAMESPACE).put("manager", manager);
        return manager;
    }

    private FixtureManager storeEmptyManager(ExtensionContext context) {
        return storeAdditionalDefinitions(Collections.emptyList(), context);
    }


}
