package jfixture.parse;

import java.lang.reflect.Method;

class FixtureMethod {
    final Object instance;
    final Method method;

    FixtureMethod(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    Object getInstance() {
        return instance;
    }

    Method getMethod() {
        return method;
    }
}
