package jfixture.parse;

import jfixture.api.Fixture;
import jfixture.api.LoadFixtures;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

class FixtureMethodCollector {

    public List<FixtureMethod> collectMethodsFromInstance(Object testInstance) {
        var methods = new ArrayList<FixtureMethod>();

        var annotations = testInstance.getClass().getAnnotationsByType(LoadFixtures.class);
        for (var annotation : annotations) {
            methods.addAll(collectMethodsFromClass(annotation.value()));
        }

        for (var method : testInstance.getClass().getMethods()) {
            var annotation = method.getAnnotation(Fixture.class);
            if (annotation != null) {
                methods.add(new FixtureMethod(testInstance, method));
            }
        }

        return methods;
    }

    public List<FixtureMethod> collectMethodsFromClass(Class<?> containerClass) {
        try {
            var containerInstance = containerClass.getDeclaredConstructor().newInstance();
            return collectMethodsFromInstance(containerInstance);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("cannot create instance of fixture container", e);
        }
    }

}
