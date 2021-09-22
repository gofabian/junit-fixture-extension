package jfixture.integration;

import jfixture.api.Fixture;

public class ExampleFixtures {

    @Fixture
    public long longInteger() {
        return 1000;
    }

    @Fixture
    public int integer() {
        return 5;
    }

}
