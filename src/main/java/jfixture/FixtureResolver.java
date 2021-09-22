package jfixture;

public class FixtureResolver {

    private final FixtureManager manager;

    public FixtureResolver(FixtureManager manager) {
        this.manager = manager;
    }

    public Object resolve(Class<?> type) {
        return manager.setUp(type);
    }
}
