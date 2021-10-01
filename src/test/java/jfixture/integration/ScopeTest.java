package jfixture.integration;

import jfixture.FixtureExtension;
import jfixture.Scope;
import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ScopeTest.AfterAllCheck.class)
@ExtendWith(FixtureExtension.class)
public class ScopeTest {

    private static final List<String> setUps = new ArrayList<>();
    private static final List<String> tearDowns = new ArrayList<>();

    @Fixture(autoUse = true, scope = Scope.METHOD)
    public String method(FixtureContext context) {
        System.out.println("before method");
        setUps.add("method");
        context.addTearDown(() -> {
            System.out.println("after method");
            tearDowns.add("method");
        });
        return "method";
    }

    @Fixture(autoUse = true, scope = Scope.CLASS)
    public long clazz(FixtureContext context) {
        System.out.println("before class");
        setUps.add("class");
        context.addTearDown(() -> {
            System.out.println("after class");
            tearDowns.add("class");
        });
        return 1337L;
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    public static class AfterAllCheck implements AfterAllCallback {
        @Override
        public void afterAll(ExtensionContext context) {
            assertEquals(List.of("class", "method", "method"), setUps);
            assertEquals(List.of("method", "method", "class"), tearDowns);
        }
    }

}
