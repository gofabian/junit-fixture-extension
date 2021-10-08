package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.ExtensionContextUtil;
import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.Scope;
import de.gofabian.jfixture.api.Fixture;
import de.gofabian.jfixture.api.FixtureContext;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ScopeTest.AfterAllCheck.class, FixtureExtension.class})
@TestClassOrder(ClassOrderer.ClassName.class)
public class ScopeTest {

    private static final List<String> hooks = new ArrayList<>();

    @Fixture(autoUse = true, scope = Scope.METHOD)
    public String method(FixtureContext context) {
        hooks.add("setUpMethod");
        context.addTearDown(() -> hooks.add("tearDownMethod"));
        return "method";
    }

    @Fixture(autoUse = true, scope = Scope.CLASS)
    public long clazz(FixtureContext context) {
        hooks.add("setUpClass");
        context.addTearDown(() -> hooks.add("tearDownClass"));
        return 1337L;
    }

    @Fixture(autoUse = true, scope = Scope.FILE)
    public double file(FixtureContext context) {
        hooks.add("setUpFile");
        context.addTearDown(() -> hooks.add("tearDownFile"));
        return 0.42;
    }

    @Fixture(autoUse = true, scope = Scope.SESSION)
    public int session(FixtureContext context) {
        hooks.add("setUpSession");
        context.addTearDown(() -> hooks.add("tearDownSession"));
        return 42;
    }

    @Nested
    public class NestedTest1 {
        @Test
        public void test1() {
        }

        @Test
        public void test2() {
        }
    }

    @Nested
    public class NestedTest2 {
        @Test
        public void test3() {
        }
    }

    public static class AfterAllCheck implements AfterAllCallback {
        @Override
        public void afterAll(ExtensionContext context) {
            if (ExtensionContextUtil.isOuterClassContext(context)) {
                assertEquals(List.of(
                        "setUpSession",
                        "setUpFile",
                        // NestedTest1...
                        "setUpClass",
                        "setUpMethod",
                        "tearDownMethod",
                        "setUpMethod",
                        "tearDownMethod",
                        "tearDownClass",
                        // ...NestedTest1
                        // NestedTest2...
                        "setUpClass",
                        "setUpMethod",
                        "tearDownMethod",
                        "tearDownClass",
                        // ...NestedTest2
                        "tearDownFile"
                ), hooks);
            }
        }
    }

}
