# Coding Standards

## General Principles

This library is coded in the pseudo-functional style that has become possible with Java 8. We use
these general principles for functional style coding in Java:

- Immutability is the core concept of functional programming
    - Use private constructors for most classes
    - Use a static "of" method as a builder method if there are one or two attributes. If there are two attributes, they may not be of the same type.  A Builder class is preferred with two attributes, but "of" may be used to make the code more compact or readable in some cases.
    - Generally use a static Builder class if more than one attribute is required to initialize a class
    - Builders only have a zero argument constructor.  All attributes set with "with" methods.
    - When using a Builder, private class constructors take the Builder as the only argument
    - Class constructors using a Builder should check for null if an attribute is required.
    - In Builders, Lists should be initialized and then populated with `add` or `addAll`.  Classes may directly reference the list in the builder.
    - In Builders, Maps should be initialized and then populated with `put` or `putAll`.  Classes may directly reference the map in the builder.
    - Builders may be mutable, other classes may not be mutable.
    - No setters
    - Classes never expose a modifiable List. Lists are exposed with an unmodifiable List, or a Stream.
    - Classes never expose a modifiable Map. A Map may be exposed with an unmodifiable Map.
- Avoid direct use of null. Any Class attribute that could be null in normal use should be wrapped in a `java.util.Optional`
- Avoid for loops (imperative) - use map/filter/reduce/collect (declarative) instead
- Avoid Stream.forEach() - this method is only used for side effects, and we want no side-effects
- Avoid Optional.ifPresent() - this method is only used for side effects, and we want no side-effects
- The only good function is a pure function.  Some functions in the library accept an AtomicInteger which is a necessary evil
- Classes with no internal attributes are usually a collection of utility functions. Use static methods in an interface instead.
- Remember the single responsibility principle - methods do one thing, classes have one responsibility

## Clean Code

We are committed to clean code.  This means:

- Small methods - less than 5 lines is good, 1 line is ideal 
- Small classes - less than 100 lines is good, less than 50 lines is ideal
- Use descriptive names
- Comments are a last resort - don't comment bad code, refactor it
- No nested control structures - ideal cyclomatic complexity of a function is 1
- Maintain 100% test coverage
- Run SonarQube analysis - do not add any technical debt, bugs, code smells, etc.

## Test Driven Development

Remember the three rules of TDD:

1. You may not write production code until you have written a failing unit test.
2. You may not write more of a unit test that is sufficient to fail, and not compiling is failing. 
3. You may not write more production code than is sufficient to passing the currently failing test.


