# Where Conditions

MyBatis Dynamic SQL supports a wide variety of where clause conditions. All conditions can be combined with "and" and "or" operators to create arbitrarily complex where clauses.

In the following examples:

  * "x" and "y" are values that will be rendered as prepared statement parameters. The resulting SQL is rendered in a format that is compatible with the target runtime (either MyBatis or Spring), but we will show standard prepared statement parameter markers for simplicity.
  * "foo" and "bar" are instances of SqlColumn.

## Simple Conditions

Simple conditions are the most common - they render the basic SQL operators.

| Condition | Example | Result |
|-----------|---------|--------|
| Between | where(foo, isBetween(x).and(y)) | `where foo between ? and ?` |
| Equals | where(foo, isEqualTo(x)) | `where foo = ?` |
| Greater Than | where(foo, isGreaterThan(x)) | `where foo > ?` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(x)) | `where foo >= ?` |
| In | where(foo, isIn(x, y)) | `where foo in (?,?)` |
| In (case insensitive) | where(foo, isInCaseInsensitive(x, y)) | `where upper(foo) in (?,?)` (the framework will transform the values for x and y to upper case)|
| Less Than | where(foo, isLessThan(x)) | `where foo < ?` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(x)) | `where foo <= ?` |
| Like | where(foo, isLike(x)) | `where foo like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Like (case insensitive) | where(foo, isLikeCaseInsensitive(x)) | `where upper(foo) like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Between | where(foo, isNotBetween(x).and(y)) | `where foo not between ? and ?` |
| Not Equals | where(foo, isNotEqualTo(x)) | `where foo <> ?` |
| Not In | where(foo, isNotIn(x, y)) | `where foo not in (?,?)` |
| Not In (case insensitive) | where(foo, isNotInCaseInsensitive(x, y)) | `where upper(foo) not in (?,?)` (the framework will transform the values for x and y to upper case)|
| Not Like | where(foo, isLike(x)) | `where foo not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Not Like (case insensitive) | where(foo, isNotLikeCaseInsensitive(x)) | `where upper(foo) not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Null | where(foo, isNotNull()) | `where foo is not null` |
| Null | where(foo, isNull()) | `where foo is null` |


## Sub-Selects

Many conditions can be rendered with sub selects.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo = (select bar from table2 where bar = ?)` |
| Greater Than | where(foo, isGreaterThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo > (select bar from table2 where bar = ?)` |
| Greater Than  or Equals | where(foo, isGreaterThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo >= (select bar from table2 where bar = ?)` |
| In | where(foo, isIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo in (select bar from table2 where bar < ?)` |
| Less Than | where(foo, isLessThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo < (select bar from table2 where bar = ?)` |
| Less Than  or Equals | where(foo, isLessThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <= (select bar from table2 where bar = ?)` |
| Not Equals | where(foo, isNotEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <> (select bar from table2 where bar = ?)` |
| Not In | where(foo, isNotIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo not in (select bar from table2 where bar < ?)` |


## Column Comparison Conditions

Column comparison conditions can be used to write where clauses comparing the values of columns in a table.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(bar)) | `where foo = bar` |
| Greater Than | where(foo, isGreaterThan(bar)) | `where foo > bar` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(bar)) | `where foo >= bar` |
| Less Than | where(foo, isLessThan(bar)) | `where foo < bar` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(bar)) | `where foo <= bar` |
| Not Equals | where(foo, isNotEqualTo(bar)) | `where foo <> bar` |

## Optional Conditions

All conditions support optionality - meaning they can be configured to render into the final SQL if a configured test passes.

For example, you could code a search like this:

```java
    public List<AnimalData> searchPeople(String animalName_, Double bodyWeight_, Double brainWeight_) {
        ...
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(animalName, isEqualTo(animalName_).when(Objects::nonNull))
                .and(bodyWeight, isEqualToWhen(bodyWeight_).when(Objects::nonNull))
                .and(brainWeight, isEqualToWhen(brainWeight_).when(Objects::nonNull))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        ...
    }
```

In this example, the three conditions will only be rendered if the values passed to them are not null. If all three values are null, then no where clause will be generated.

Each of the conditions accepts a lambda expression that can be used to determine if the condition should render or not. The lambdas will all be of standard JDK types (either `java.util.function.BooleanSupplier`, `java.util.function.Predicate`, or `java.util.function.BiPredicate` depending on the type of condition). The following table lists the optional conditions and shows how to use them: 

| Condition | Example | Rendering Rules |
|-----------|---------|-----------------|
| Between| where(foo, isBetween(x).and(y).when(BiPredicate)) | The library will pass x and y to the BiPredicate's test method. The condition will render if BiPredicate.test(x, y) returns true |
| Equals | where(foo, isEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Greater Than | where(id, isGreaterThan(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Greater Than or Equals | where(id, isGreaterThanOrEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Less Than | where(id, isLessThan(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Less Than or Equals | where(id, isLessThanOrEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Like | where(id, isLike(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Like Case Insensitive | where(id, isLikeCaseInsensitive(x).when(Predicate&lt;String&gt;)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Between | where(id, isNotBetween(x).and(y).when(BiPredicate)) | The library will pass x and y to the BiPredicate's test method. The condition will render if BiPredicate.test(x, y) returns true |
| Not Equals | where(id, isNotEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Like | where(id, isNotLike(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Like Case Insensitive | where(id, isNotLikeCaseInsensitive(x).when(Predicate&lt;String&gt;)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Null | where(id, isNotNull().when(BooleanSupplier) | The condition will render if BooleanSupplier.getAsBoolean() returns true |
| Null | where(id, isNull().when(BooleanSupplier) | The condition will render if BooleanSupplier.getAsBoolean() returns true |

### "When Present" Optional Conditions
The library supplies several specializations of optional conditions to be used in the common case of checking for null values. The table below lists the rendering rules for each of these "when present" optional conditions.

| Condition | Example | Rendering Rules |
|-----------|---------|-----------------|
| Between| where(foo, isBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Equals | where(foo, isEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than | where(id, isGreaterThanWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than or Equals | where(id, isGreaterThanOrEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Less Than | where(id, isLessThanWhenPresent(x)) | The condition will render if x is non-null |
| Less Than orEquals | where(id, isLessThanOrEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Like | where(id, isLikeWhenPresent(x)) | The condition will render if x is non-null |
| Like Case Insensitive | where(id, isLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |
| Not Between | where(id, isNotBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Not Equals | where(id, isNotEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Not Like | where(id, isNotLikeWhenPresent(x)) | The condition will render if x is non-null |
| Not Like Case Insensitive | where(id, isNotLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |

### Optionality with the "In" Conditions
Optionality with the "in" and "not in" conditions is a bit more complex than the other types of conditions. The first thing to know is that no "in" or "not in" condition will render if the list of values is empty. For example, there will never be rendered SQL like `where name in ()`. So optionality of the "in" conditions is more about optionality of the *values* of the condition. The library comes with functions that will filter out null values, and will upper case String values to enable case insensitive queries. There are extension points to add additional filtering and mapping if you so desire.

The following table shows the different supplied In conditions and how they will render for different sets of inputs. The table assumes the following types of input:

- Example 1 assumes an input list of ("foo", null, "bar") - like `where(name, isIn("foo", null, "bar"))`
- Example 2 assumes an input list of (null) - like `where(name, isIn((String)null))`


| Condition | Nulls Filtered | Strings Mapped to Uppercase | Example 1 Rendering | Example 2 Rendering |
|-----------|----------------|--------------------|---------------------|---------------------|
| IsIn| No | No| name in ('foo', null, 'bar') | name in (null) |
| IsInWhenPresent | Yes | No | name in ('foo', 'bar') | No Render |
| IsInCaseInsensitive | No | Yes | upper(name) in ('FOO', null, 'BAR') | upper(name) in (null) |
| IsInCaseInsensiteveWhenPresent | Yes | Yes | upper(name) in ('FOO', 'BAR') | No Render |
| IsNotIn| No | No| name not in ('foo', null, 'bar') | name not in (null) |
| IsNotInWhenPresent | Yes | No | name not in ('foo', 'bar') | No render |
| IsNotInCaseInsensitive | No | Yes | upper(name) not in ('FOO', null, 'BAR') | upper(name) not in (null) |
| IsNotInCaseInsensiteveWhenPresent | Yes | Yes | upper(name) not in ('FOO', 'BAR') | No Render |

If none of these options meet your needs, there is an extension point where you can add your own filter and/or map conditions to the value stream. This gives you great flexibility to alter or filter the value list before the condition is rendered.

The extension point for modifying the value list is the method `then(UnaryOperator<Stream<T>>)`. This method accepts a `UnaryOperator<Stream<T>>` in which you can specify map and/or filter operations for the value stream. For example, suppose you wanted to code an "in" condition that accepted a list of strings, but you want to filter out any null or blank string, and you want to trim all strings. This can be accomplished with code like this:

```java
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isIn("  Mouse", "  ", null, "", "Musk shrew  ")
                            .then(s -> s.filter(Objects::nonNull)
                                    .map(String::trim)
                                    .filter(st -> !st.isEmpty())))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
```

This code is a bit cumbersome, so if this is a common use case you could write a specialization of the `IsIn` condition as follows:

```java
public class MyInCondition extends IsIn<String> {
    protected MyInCondition(List<String> values) {
        super(values, s -> s.filter(Objects::nonNull)
                .map(String::trim)
                .filter(st -> !st.isEmpty()));
    }

    public static MyInCondition isIn(String...values) {
        return new MyInCondition(Arrays.asList(values));
    }
}
```

Then the condition could be used in a query as follows:

```java
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, MyInCondition.isIn("  Mouse", "  ", null, "", "Musk shrew  "))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
```

You can apply value stream operations to the conditions `IsIn`, `IsInCaseInsensitive`, `IsNotIn`, and `IsNotInCaseInsensitive`. With the case insensitive conditions, the library will automatically convert non-null strings to upper case after any value stream operation you specify.
