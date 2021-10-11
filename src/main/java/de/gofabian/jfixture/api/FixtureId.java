package de.gofabian.jfixture.api;

public class FixtureId {

    private final Class<?> type;
    private final String name;

    public FixtureId(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FixtureId{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
