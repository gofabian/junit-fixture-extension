package de.gofabian.jfixture;

public enum Scope {
    SESSION(1),
    FILE(2),
    CLASS(3),
    METHOD(4);

    private final int order;

    Scope(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
