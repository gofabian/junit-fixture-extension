# JUnit 5 Fixture Extension

A Fixture is something you can

- set up and tear down
- use in a test
- identify by type and name
- give a scoped lifecycle (e.g. test method, class, file, session)

More about fixtures: <https://docs.pytest.org/en/6.2.x/fixture.html>

## Add fixtures to your project

```xml
<dependency>
    <groupId>de.gofabian</groupId>
    <artifactId>junit-fixture-extension</artifactId>
    <version>0.1.1</version>
    <scope>test</scope>
</dependency>
```

## Use fixtures in tests

<table>
<tr>
<th>With Fixture Extension</th>
<th>Plain JUnit 5</th>
</tr>
<tr><td valign="top">

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture
    Database db(FixtureContext c) {
        var db = DbUtil.open(...);
        c.addTearDown(() -> db.close());
        return db;
    }

    @Test
    void test(Database db) {
        db.put("key", "value");
        assertEquals("value", db.get("key"));
    }

}
```
</td><td valign="top">

```
class TestClass {
    Database db;

    @BeforeEach
    void setUp() {
        db = DbUtil.open(...);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void test() {
        db.put("key", "value");
        assertEquals("value", db.get("key"));
    }

}
```
</td></tr>
</table>

## Use fixture scopes

<table>
<tr><td valign="top">

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture(scope = Scope.SESSION)
    long fixtureWithFileScope(FixtureContext c) {
        println("session-up");
        c.addTearDown(() -> println("session-down"));
        return 1234567890;
    }

    @Fixture(scope = Scope.FILE)
    double fixtureWithFileScope(FixtureContext c) {
        println("file-up");
        c.addTearDown(() -> println("file-down"));
        return 13.37;
    }
    
    @Fixture(scope = Scope.CLASS)
    int fixtureWithClassScope(FixtureContext c) {
        println("class-up");
        c.addTearDown(() -> println("class-down"));
        return 42;
    }
    
    @Fixture // default scope is "METHOD"
    String fixtureWithMethodScope(FixtureContext c) {
        println("method-up");
        c.addTearDown(() -> println("method-down"));
        return "method";
    }

    @Test
    void test1(String m, int c, double f, long s) {
        println("test1");
    }

    @Test
    void test2(String m, int c, double f, long s) {
        println("test2");
    }

}
```

</td><td valign="top" width="250px">

```
Test Output:

session-up
file-up
class-up
method-up
test1
method-down
method-up
test2
method-down
class-down
file-down
session-down
```

</td></tr>
</table>

## Use fixtures in fixtures

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture
    Database db(FixtureContext c) {
        var db = DbUtil.open(...);
        c.addTearDown(() -> db.close());
        return db;
    }

    @Fixture
    Table bookTable(Database db) {
        return db.table("book");
    }

    @Test
    void test(Table table) {
        table.add("Science Book");
        assertEquals(1, table.count());
    }

}
```

## Load fixtures from other classes

<table>
<tr><td valign="top">

```java
@ExtendWith(FixtureExtension.class)
@LoadFixtures(DatabaseFixtures.class)
class TestClass {

    @Fixture
    Table bookTable(Database db) {
        return db.table("book");
    }

    @Test
    void test(Database db) {
        db.put("key", "value");
        assertEquals("value", db.get("key"));
    }

}
```

</td><td valign="top">

```java
class DatabaseFixtures {

    @Fixture
    Database db(FixtureContext c) {
        var db = DbUtil.open(...);
        c.addTearDown(() -> db.close());
        return db;
    }

}
```

</td></tr>
</table>

## "Auto use" fixtures without explicit parameter

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture(autoUse = true)
    Database db(FixtureContext c) {
        var db = DbUtil.start(...);
        c.addTearDown(() -> db.stop());
        return db;
    }

    @Test
    void test(/* Database db */) {
        print("db is set up without explicit parameter");
    }

}
```

## Use fixtures in nested tests

<table>
<tr><td valign="top">

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture(scope = Scope.FILE)
    double fixtureWithFileScope(FixtureContext c) {
        println("file-up");
        c.addTearDown(() -> println("file-down"));
        return 13.37;
    }
    
    @Fixture(scope = Scope.CLASS)
    int fixtureWithClassScope(FixtureContext c) {
        println("class-up");
        c.addTearDown(() -> println("class-down"));
        return 42;
    }
    
    @Nested
    class NestedTest1 {
        @Test
        void test1(String m, int c, double f, long s) {
            println("test1");
        }
    }

    @Nested
    class NestedTest2 {
        @Test
        void test2(String m, int c, double f, long s) {
            println("test2");
        }
    }

}
```

</td><td valign="top" width="250px">

```
Test Output:

file-up
class-up
test1
class-down
class-up
test2
class-down
file-down
```

</td></tr>
</table>



## Reference Fixture by type and optional name

```java
@ExtendWith(FixtureExtension.class)
class TestClass {

    @Fixture
    String fixture1() {
        return "fixture1";
    }
    
    @Fixture
    String fixture2() {
        return "fixture2";
    }

    @Test
    void test(String fixture1, String fixture2, String any) {
        assertEquals("fixture1", fixture1);
        assertEquals("fixture2", fixture2);
        assertTrue(any.equals("fixture1") || any.equals("fixture2"));
    }

}
```


## License

[MIT License](LICENSE)

## Development

Run tests:

    mvn test

## Release

Set new version (implicit `git tag` and `git push`):

    ./bump_version.sh x.y.z

Create a release in Github and the Github workflow will automatically publish it to Maven Central.

Manual alternative:

    mvn clean deploy -P release

## Open Points

- [ ] generic dependency types, e.g. List<Xyz>
- [ ] @UseFixture
- [ ] support parallel execution
- [ ] support static fixture class methods
- [ ] fixture mark (given argument)
- [ ] parameterized fixture (multiple executions)
- [ ] more attributes in fixture context
- [ ] Programmatic FixtureDefinition
