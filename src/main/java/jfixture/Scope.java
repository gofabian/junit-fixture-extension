package jfixture;

public enum Scope {
    SESSION(1),
    CLASS(2),
    METHOD(3);

    private final int order;

    Scope(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
