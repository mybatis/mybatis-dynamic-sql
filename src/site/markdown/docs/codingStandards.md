# Coding Standards

## General Principles

This library is coded in the pseudo-functional style that has become possible with Java 8.  We use
these general principles for functional style coding in Java:

1. Classes are immutable
2. Private constructors for most classes
3. Use a static "of" method as a builder method if there are zero to two attributes.  If there are two attributes,
   they may not be of the same type.  A Builder class is preferred with two attributes, but "of" may be used to make
   the code more compact or readable in some cases.
4. Use a static Builder class if more than one attribute is required to initialize a class
5. Builders only have a zero argument constructor.  All attributes set with "with" methods.
6. When using a Builder, private class constructors take the Builder as the only argument
7. Class constructors using a Builder should check for null of an attribute is required.
8. Any Class attribute that is optional should be wrapped in a ```java.util.Optional```
9. In Builders Lists should be initialized and then populated with add or addAll.  Classes
   can directly reference the list in the builder.
10. In Builders Maps should be initialized and then populated with put or putAll.  Classes
    can directly reference the map in the builder.
11. Builders can be mutable.
12. Classes never expose a modifiable List. Lists are exposed with Streams or, better, with a mapping method.
13. Classes never expose a modifiable Map. A Map may be exposed with an unmodifiable Map.
14. Remember the single responsibility principle - methods do one thing, classes have one responsibility

## Clean Code

We are committed to clean code.  This means:

1. Small methods - less than 5 lines is good, 1 line is ideal 
2. Small classes - less than 50 lines is good, less than 20 lines is ideal
3. Descriptive variable names
4. Descriptive method names
5. Descriptive class names
6. Comments are a last resort - don't comment bad code, refactor it
7. No nested control structures
8. Maintain 100% test coverage

## Test Driven Development

Remember the three rules of TDD:

1. You may not write production code until you have written a failing unit test.
2. You may not write more of a unit test that is sufficient to fail, and not compiling is failing. 
3. You may not write more production code than is sufficient to passing the currently failing test.


