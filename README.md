# JUnit 5 Fixtures

Idea from: https://docs.pytest.org/en/6.2.x/fixture.html

## todo

- [x] order of tear downs is wrong? (FixtureManager)
- [ ] reference fixture by name (not only by type)
- [ ] hierarchies
    - [ ] fixtures in outer and inner test classes
    - [ ] order of find fixture by type? should be "last" first?
    - [ ] override fixture
- [x] autouse
- [ ] @UseFixture
- [ ] readme
- [ ] scope
    - [ ] test, class, file, session
- [ ] fixture mark (given argument)
- [ ] parameterized fixture (multiple executions)
- [ ] more attributes in fixture context
- [ ] Programmatic FixtureDefinition
- [x] external fixture definition
- [x] fixture dependencies
