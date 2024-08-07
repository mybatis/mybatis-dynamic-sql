# Change Log

This log will detail notable changes to MyBatis Dynamic SQL. Full details are available on the GitHub milestone pages.

## Release 2.0.0 - Unreleased

Release 2.0.0 is a significant milestone for the library. We have moved to Java 17 as the minimum version supported. If
you are unable to move to this version of Java then the releases in the 1.x line can be used with Java 8.

In addition, we have taken the opportunity to make changes to the library that may break existing code. We have
worked to make these changes as minimal as possible.

**Potentially Breaking Changes:**

- If you have implemented any custom implementations of `SortSpecification`, you will need to update those
  implementations due to a new rendering strategy for ORDER BY phrases. The old methods `isDescending` and `orderByName`
  are removed in favor of a new method `renderForOrderBy` 
- If you have implemented any custom functions, you will likely need to make changes. The supplied base classes now
  hold an instance of `BasicColumn` rather than `BindableColumn`. This change was made to make the functions more
  useful in variety of circumstances. If you follow the patterns shown on the
  [Extending the Library](https://mybatis.org/mybatis-dynamic-sql/docs/extending.html) page, the change should be
  limited to changing the private constructor to accept `BasicColumn` rather than `BindableColumn`.

Other important changes:

- The library now requires Java 17
- Deprecated code from prior releases is removed
- We now allow CASE expressions in ORDER BY Clauses

## Release 1.5.2 - June 3, 2024

This is a small maintenance release with the following changes:

1. Improvements to the Kotlin DSL for CASE expressions (infix methods for "else" and "then"). See this PR for 
   details: ([#785](https://github.com/mybatis/mybatis-dynamic-sql/pull/785))
2. **Potentially Breaking Change**: the "in" conditions ("isIn", "isNotIn", "isInCaseInsensitive",
   "isNotInCaseInsensitive") will now render if the input list of values is empty. This will lead
   to a runtime exception. This change was made out of an abundance of caution and is the safest choice.
   If you wish to allow "in" conditions to be removed from where clauses when the list is empty,
   then use the "when present" versions of those conditions. If you are unsure how this works, please
   read the documentation here: https://mybatis.org/mybatis-dynamic-sql/docs/conditions.html#optionality-with-the-%E2%80%9Cin%E2%80%9D-conditions
   For background on the reason for the change, see the discussion here: https://github.com/mybatis/mybatis-dynamic-sql/issues/788

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/milestone/14?closed=1](https://github.com/mybatis/mybatis-dynamic-sql/milestone/14?closed=1)

**Important:** This is the last release that will be compatible with Java 8.

## Release 1.5.1 - April 30, 2024

This is a minor release with several enhancements.

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/milestone/13?closed=1](https://github.com/mybatis/mybatis-dynamic-sql/milestone/13?closed=1)

### Case Expressions and Cast Function
We've added support for CASE expressions to the library. Both simple and searched case expressions are supported.
This is a fairly extensive enhancement as case expressions are quite complex, but we were able to reuse many of the
building blocks from the WHERE and HAVING support already in the library. You should be able to build CASE expressions
with relatively few limitations.

It is also common to use a CAST function with CASE expressions, so we have added CAST as a built-in function
in the library.

The DSL for both Java and Kotlin has been updated to fully support CASE expressions in the same idiomatic forms
as other parts of the library.

We've tested this extensively and the code is, of course, 100% covered by test code. But it is possible that we've not
covered every scenario. Please let us know if you find issues.

Full documentation is available here:
- [Java Case Expression DSL Documentation](https://mybatis.org/mybatis-dynamic-sql/docs/caseExpressions.html)
- [Kotlin Case Expression DSL Documentation](https://mybatis.org/mybatis-dynamic-sql/docs/kotlinCaseExpressions.html)

The pull request for this change is ([#761](https://github.com/mybatis/mybatis-dynamic-sql/pull/761))

### Parameter Values in Joins

We've added the ability to specify typed values in equi-joins. This allows you to avoid the use of constants, and it is
type safe. For example:

```java
SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
    .from(itemMaster, "im")
    .join(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
    .and(orderLine.orderId, equalTo(1))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

Note the phrase `and(orderLine.orderId, equalTo(1))` which will be rendered with a bound SQL parameter. Currently, this
capability is limited to equality only. If you have a use for other functions (not equal, less then, greater than, etc.)
please let us know.

In order to add this capability, we've modified the join DSL to add type information to the join columns. This should
be source code compatible with most uses. There could be an issue if you are joining tables with columns of different
types - which is a rare usage. Please let us know if this causes an undo hardship.

### Other Changes

1. Rendering of conditions and columns was refactored. One benefit of this change is that
   it is now easier to support more complex functions - such as the aggregate function `sum(id < 5)` which is the
   initial enhancement request that inspired this change. As a result of the changes, one method is deprecated
   in the `BasicColumn` object. If you have implemented any custom functions, please note this deprecation and update
   your code accordingly. ([#662](https://github.com/mybatis/mybatis-dynamic-sql/pull/662))
2. Added the ability to code a bound value in rendered SQL. This is similar to a constant, but the value is added to
   the parameter map and a bind parameter marker is rendered. ([#738](https://github.com/mybatis/mybatis-dynamic-sql/pull/738))
3. Refactored the conditions to separate the concept of an empty condition from that of a renderable condition. This
   will enable a future change where conditions could decide to allow rendering even if they are considered empty (such
   as rendering empty lists). This change should be transparent to users unless they have implemented custom conditions.
4. Added a configuration setting to allow empty list conditions to render. This could generate invalid SQL, but might be
   a good safety measure in some cases.
5. Added Array based functions for the "in" and "not in" conditions in the Kotlin DSL. These functions allow a more
   natural use of an Array as an input for an "in" condition. They also allow easy reuse of a vararg argument in a
   function. ([#781](https://github.com/mybatis/mybatis-dynamic-sql/pull/781))

## Release 1.5.0 - April 21, 2023

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/milestone/12?closed=1](https://github.com/mybatis/mybatis-dynamic-sql/milestone/12?closed=1)

### Potentially Breaking Changes

This release includes a major refactoring of the "where" clause support. This is done to support common code for
"having" clauses which is a new feature (see below). Most changes are source code compatible with previous
releases and should be transparent with no impact. Following is a list of some more visible changes...

First, the "where" methods in `SqlBuilder` now return an instance of `WhereDSL.StandaloneWhereFinisher` rather than
`WhereDSL`. This will only impact you if you are using the WhereDSL directly which is a rare use case.

Second, if you are using independent or reusable where clauses you will need to make changes. Previously you might have
coded an independent where clause like this:

```java
private WhereApplier commonWhere = d -> d.where(id, isEqualTo(1)).or(occupation, isNull());
```

Code like this will no longer compile. There are two options for updates. The simplest change to make is to
replace "where" with "and" or "or" in the above code. For example...

```java
private WhereApplier commonWhere = d -> d.and(id, isEqualTo(1)).or(occupation, isNull());
```

This will function as before, but you may think it looks a bit strange because the phrase starts with "and". If you
want this to look more like true SQL, you can write code like this:

```java
private final WhereApplier commonWhere = where(id, isEqualTo(1)).or(occupation, isNull()).toWhereApplier();
```

This uses a `where` method from the `SqlBuilder` class. 

### "Having" Clause Support

This release adds support for "having" clauses in select statements. This includes a refactoring of the "where"
support, so we can reuse the and/or logic and rendering that is already present in the "where" clause support.
This because "having" and "where" are essentially the same.

One slight behavior change with this refactoring is that the renderer will now remove a useless open/close
parentheses around certain rendered where clauses. Previously it was possible to have a rendered where clause like
this:

```sql
where (a < 2 and b > 3)
```

The renderer will now remove the open/close parentheses in a case like this.

In the Java DSL, a "having" clause can only be coded after a "group by" clause - which is a reasonable restriction
as "having" is only needed if there is a "group by".

In the Kotlin DSL, the "group by" restriction is not present because of the free form nature of that DSL - but you 
should probably only use "having" if there is a "group by". Also note that the freestanding "and" and "or"
functions in the Kotlin DSL still only apply to the where clause. For this reason, the freestanding "and" and "or"
methods are deprecated. Please only use the "and" and "or" methods inside a "where" or "having" lambda.

The pull request for this change is ([#550](https://github.com/mybatis/mybatis-dynamic-sql/pull/550))

### Multi-Select Queries

A multi-select query is a special case of a union select statement. The difference is that it allows "order by" and
paging clauses to be applied to the nested queries. A multi-select query looks like this:

```java
SelectStatementProvider selectStatement = multiSelect(
        select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
        .from(person)
        .where(id, isLessThanOrEqualTo(2))
        .orderBy(id)
        .limit(1)
).unionAll(
        select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
        .from(person)
        .where(id, isGreaterThanOrEqualTo(4))
        .orderBy(id.descending())
        .limit(1)
).orderBy(sortColumn("A_ID"))
.fetchFirst(2).rowsOnly()
.build()
.render(RenderingStrategies.MYBATIS3);
```

Notice how both inner queries have `order by` and `limit` phrases, then there is an `order by` phrase
for the entire query.

The pull request for this change is ([#591](https://github.com/mybatis/mybatis-dynamic-sql/pull/591))

### Other Changes

1. Added support for specifying "limit" and "order by" on the DELETE and UPDATE statements. Not all databases support
   this SQL extension, and different databases have different levels of support. For example, MySQL/MariaDB have full
   support but HSQLDB only supports limit as an extension to the WHERE clause. If you choose to use this new capability,
   please test to make sure it is supported in your database. ([#544](https://github.com/mybatis/mybatis-dynamic-sql/pull/544))
2. Deprecated Kotlin DSL functions have been removed, as well as deprecated support for "EmptyListCallback" in the "in"
   conditions. ([#548](https://github.com/mybatis/mybatis-dynamic-sql/pull/548))
3. Refactored the common insert mapper support for MyBatis3 by adding a CommonGeneralInsertMapper that can be used
   without a class that matches the table row. It includes methods for general insert and insert select.
   ([#570](https://github.com/mybatis/mybatis-dynamic-sql/pull/570))
4. Added the ability to change a table name on AliasableSqlTable - this creates a new instance of the object with a new
   name. This is useful in sharded databases where the name of the table is calculated based on some sharding
   algorithm. Also deprecated the constructors on SqlTable that accept Suppliers for table name - this creates an
   effectively mutable object and goes against the principles of immutability that we strive for in the library.
   ([#572](https://github.com/mybatis/mybatis-dynamic-sql/pull/572))
5. Add `SqlBuilder.concat` and the equivalent in Kotlin. This is a concatenate function that works on more databases.
   ([#573](https://github.com/mybatis/mybatis-dynamic-sql/pull/573))
6. Several classes and methods in the Kotlin DSL are deprecated in response to the new "having" support
7. Added support for inserting a list of simple classes like Integers, Strings, etc. This is via a new "map to row"
   function on the insert, batch insert, and multirow insert statements. ([#612](https://github.com/mybatis/mybatis-dynamic-sql/pull/612)) 

## Release 1.4.1 - October 7, 2022

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.4.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.4.1+)

### Potentially Breaking Change

In this release we have changed the default behavior of the library in one key area. If a where clause is coded,
but fails to render because all the optional conditionals drop out of the where clause, then the library will now
throw a `NonRenderingWhereClauseException`. We have made this change out of an abundance of caution. The prior
behavior would allow generation of statements that inadvertently affected all rows in a table.

We have also deprecated the "empty callback" functions in the "in" conditions in favor of this new configuration
strategy. The "empty callback" methods were effective for "in" conditions that failed to render, but they offered
no help for other conditions that failed to render, or if all conditions fail to render - which is arguably a more
dangerous outcome. If you were using any of these methods, you should remove the calls to those methods and catch the
new `NonRenderingWhereClauseException`.

If you desire the prior behavior where non rendering where clauses are allowed, you can change the global configuration
of the library or - even better - change the configuration of individual statements where this behavior should be allowed.

For examples of global and statement configuration, see the "Configuration of the Library" page.

### Other Changes

1. Added support for criteria groups without an initial criteria. This makes it possible to create an independent list
   of pre-created criteria and then add the list to a where clause. See the tests in the related pull request for
   usage examples. ([#462](https://github.com/mybatis/mybatis-dynamic-sql/pull/462))
2. Added the ability to specify a table alias on DELETE and UPDATE statements.
   This is especially useful when working with a sub-query with an exists or not exists condition.
   ([#489](https://github.com/mybatis/mybatis-dynamic-sql/pull/489))
3. Updated the Kotlin DSL to use Kotlin 1.7's new "definitely non-null" types where appropriate. This helps us to more
   accurately represent the nullable/non-nullable expectations for API method calls.
   ([#496](https://github.com/mybatis/mybatis-dynamic-sql/pull/496))
4. Added the ability to configure the library and change some default behaviors. Currently, this is limited to changing
   the behavior of the library in regard to where clauses that will not render. See the "Configuration of the Library"
   page for details. ([#515](https://github.com/mybatis/mybatis-dynamic-sql/pull/515))
5. Added several checks for invalid SQL ([#516](https://github.com/mybatis/mybatis-dynamic-sql/pull/516))
6. Added documentation for the various exceptions thrown by the library ([#517](https://github.com/mybatis/mybatis-dynamic-sql/pull/517))
7. Update the "insertSelect" method in the Kotlin DSL to make it consistent with the other insert methods ([#524](https://github.com/mybatis/mybatis-dynamic-sql/pull/524))

## Release 1.4.0 - March 3, 2022

The release includes new functionality in the Where Clause DSL to support arbitrary grouping of conditions, and also use
of a "not" condition. It should now be possible to write any type of where clause.

Additionally, there were significant updates to the Kotlin DSL - both to support the new functionality in the
where clause, and significant updates to insert statements. There were also many minor updates in Kotlin
to make more use of Kotlin language features like infix functions and operator overloads.

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.4.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.4.0+)

1. Added support for arbitrary placement of nested criteria. For example, it is now
   possible to write a where clause like this: `where (a < 5 and B = 3) and ((C = 4 or D = 5) and E = 6)`. Previously
   we did not support the grouping of criteria at the beginning of a where clause or the beginning of an and/or
   condition. Adding this support required significant refactoring, but that should be transparent to most users.
   ([#434](https://github.com/mybatis/mybatis-dynamic-sql/pull/434))
2. Remove deprecated "when" and "then" methods on all conditions. The methods have been replaced by more appropriately
   named "filter" and "map" methods that function as expected for method chaining.
   ([#435](https://github.com/mybatis/mybatis-dynamic-sql/pull/435))
3. Added support for a "not" criteria grouping on a where clause. It is now possible to write a where clause like
   `where (a < 5 and B = 3) and (not (C = 4 or D = 5) and E = 6)`. With this enhancement (and the enhancement for
   arbitrary grouping) it should now be possible to write virtually any where clause imaginable.
   ([#438](https://github.com/mybatis/mybatis-dynamic-sql/pull/438))
4. Major update to the Kotlin where clause DSL. Where clauses now support the "group" and "not" features from above. In
   addition, the where clause DSL has been fully updated to make it feel more like natural SQL. The previous version
   of the where clause DSL would have yielded almost unreadable code had the "group" and "not" functions been added.
   This update is better all around and yields a DSL that is very similar to native SQL. The new DSL includes many
   Kotlin DSL construction features including infix functions, operator overloads, and functions with receivers.
   We believe it will be well worth the effort to migrate to the new DSL. The prior where clause DSL remains in the
   library for now, but is deprecated. It will be removed in version 1.5.0 of the library. Documentation for the new
   DSL is here: https://github.com/mybatis/mybatis-dynamic-sql/blob/master/src/site/markdown/docs/kotlinWhereClauses.md
   ([#442](https://github.com/mybatis/mybatis-dynamic-sql/pull/442))
5. General cleanup of the Kotlin DSL. The Kotlin DSL functions are now mostly Unit functions. This should have
   no impact on most users and is source code compatible with prior versions of the library when the library was used
   as described in the documentation. This change greatly simplifies the type hierarchy of the Kotlin builders.
   ([#446](https://github.com/mybatis/mybatis-dynamic-sql/pull/446))
6. Minor update the Kotlin join DSL to make it closer to natural SQL. The existing join methods are deprecated and
   will be removed in version 1.5.0. ([#447](https://github.com/mybatis/mybatis-dynamic-sql/pull/447))
7. Updated most of the Kotlin insert DSL functions to be more like natural SQL. The main difference is that for insert,
   insertBatch, and insertMultiple, the "into" function is moved inside the completer lambda. The old methods are now
   deprecated and will be removed in version 1.5.0 of the library. This also allowed us to make some insert DSL
   methods into infix functions. ([#452](https://github.com/mybatis/mybatis-dynamic-sql/pull/452))
8. Updated the where clause to expose table aliases specified in an outer query to sub queries in the where clause
   (either an "exists" clause, or a sub query to column comparison condition) This makes it easier to use these types
   of sub queries without having to re-specify the aliases for columns from the outer query.
   ([#459](https://github.com/mybatis/mybatis-dynamic-sql/pull/459))

## Release 1.3.1 - December 18, 2021

This is a minor release with a few small enhancements. Most deprecated methods will be removed in the next release.

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.1+)

### Added

- Added the ability to specify a JavaType associated with a column. The JavaType will be rendered properly for MyBatis ([#386](https://github.com/mybatis/mybatis-dynamic-sql/pull/386))
- Added a few missing groupBy and orderBy methods on the `select` statement ([#409](https://github.com/mybatis/mybatis-dynamic-sql/pull/409))
- Added a check for when a table alias is re-used in error (typically in a self-join) ([#425](https://github.com/mybatis/mybatis-dynamic-sql/pull/425))
- Added a new extension of SqlTable that supports setting a table alias directly within the table definition ([#426](https://github.com/mybatis/mybatis-dynamic-sql/pull/426))

## Release 1.3.0 - May 6, 2021

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.0+)

### Release Themes

The major themes of this release include the following:

1. Add support for subqueries in select statements - both in a from clause and a join clause.
1. Add support for the "exists" and "not exists" operator. This will work in "where" clauses anywhere
   they are supported.
1. Refactor and improve the built-in conditions for consistency (see below). There is one breaking change also
   detailed below.
1. Continue to refine the Kotlin DSL. Many changes to the Kotlin DSL are internal and should be source code
   compatible with existing code. There is one breaking change detailed below.
1. Remove deprecated code from prior releases.

### Built-In Condition Refactoring and Breaking Change
All built-in conditions have been refactored. The changes should have little impact for the vast majority of users.
However, there are some changes in behavior and one breaking change.

1. Internally, the conditions no longer hold value Suppliers, they now hold the values themselves. The SqlBuilder
   methods that accept Suppliers will call the `Supplier.get()` method when the condition is constructed. This should
   have no impact unless you were somehow relying on the delay in obtaining a value until the condition was rendered.
1. The existing "then" and "when" methods have been deprecated and replaced with "map" and "filter" respectively.
   The new method names are more familiar and more representative of what these methods actually do. In effect
   these methods mimic the function of the "map" and "filter" methods on "java.util.Optional" and they are used
   for a similar purpose.
1. The new "filter" method works a bit differently than the "when" method it replaces. The old "when" method could not
   be chained - if it was called multiple times, only the last call would take effect. The new "filter" methods works
   as it should and every call will take effect. This allows you to construct map/filter pipelines as you would
   expect.
1. The new "map" method will allow you to change the datatype of a condition as is normal for a "map" method. You
   can use this method to apply a type conversion directly within condition.
1. All the "WhenPresent" conditions have been removed as separate classes. The methods that produced these conditions
   in the SqlBuilder remain, and they will now produce a condition with a "NotNull" filter applied. So at the API level
   things will function exactly as before, but the intermediate classes will be different.
1. One **breaking change** is that the builder for List value conditions has been removed without replacement. If you
   were using this builder to supply a "value stream transformer", then the replacement is to build a new List value
   condition and then call the "map" and "filter" methods as needed. For example, prior code looked like this

   ```java
    public static IsIn<String> isIn(String...values) {
        return new IsIn.Builder<String>()
                .withValues(Arrays.asList(values))
                .withValueStreamTransformer(s -> s.filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(st -> !st.isEmpty()))
                .build();
    }
   ```
   New code should look like this:
   ```java
    public static IsIn<String> isIn(String...values) {
        return SqlBuilder.isIn(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(st -> !st.isEmpty());
    }
   ```
   We think this is a marked improvement!

### Kotlin DSL Update and Breaking Change for Kotlin

The Kotlin DSL continues to evolve. With this release we have fully built out the DSL, and it is no longer necessary
to use any functions in `org.mybatis.dynamic.sql.SqlBuilder`. The advantages of this are many and are detailed on the
Kotlin overview page in the documentation. Many functions in `SqlBuilder` have been replaced by
top level functions the `org.mybatis.dynamic.sql.util.kotlin.elements` package. In most cases you can switch to the
native Kotlin DSL by simply changing the import statements. For example, you can switch usage of the `isEqualTo`
function by changing

```kotlin
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
```

to

```kotlin
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
```

Several functions that accepted supplier arguments are not present in the Kotlin DSL. This is to avoid difficult
and confusing method overload problems for methods that did not offer any real benefit. If you were using one of these
methods in the Java DSL, then in the Kotlin DSL you will have to change the function argument from a supplier to the
actual value itself. 

A **breaking change** is that Kotlin support for `select` and `count` statements has been refactored. This will not impact code
created by MyBatis generator. It will have an impact on Spring/Kotlin users as well as MyBatis users that coded joins or
other queries directly in Kotlin. The difference is that the `from` clause has been moved inside the lambda for select
and count statements.

Previously, code looked like this:
```kotlin
   val selectStatement = select(foo).from(bar) {
       where(id, isLessThan(3))
   }
```
  
The new code looks like this:
```kotlin
   val selectStatement = select(foo) {
       from(bar)
       where(id, isLessThan(3))
   }
```
  
This change makes the Kotlin DSL more consistent and also makes it easier to implement subquery support in the
Kotlin DSL.

### Added

- Added a new sort specification that is useful in selects with joins ([#269](https://github.com/mybatis/mybatis-dynamic-sql/pull/269))
- Added the capability to generate a camel cased alias for a column ([#272](https://github.com/mybatis/mybatis-dynamic-sql/issues/272))
- Added subquery support for "from" clauses in a select statement ([#282](https://github.com/mybatis/mybatis-dynamic-sql/pull/282))
- Added Kotlin DSL updates to support sub-queries in select statements, where clauses, and insert statements ([#282](https://github.com/mybatis/mybatis-dynamic-sql/pull/282))
- Added subquery support for "join" clauses in a select statement ([#293](https://github.com/mybatis/mybatis-dynamic-sql/pull/293))
- Added support for the "exists" and "not exists" operator in where clauses ([#296](https://github.com/mybatis/mybatis-dynamic-sql/pull/296))
- Refactored the built-in conditions ([#331](https://github.com/mybatis/mybatis-dynamic-sql/pull/331)) ([#336](https://github.com/mybatis/mybatis-dynamic-sql/pull/336))
- Added composition functions for WhereApplier ([#335](https://github.com/mybatis/mybatis-dynamic-sql/pull/335))
- Added a mapping for general insert and update statements that will render null values as "null" in the SQL ([#343](https://github.com/mybatis/mybatis-dynamic-sql/pull/343))
- Allow the "in when present" conditions to accept a null Collection as a parameter ([#346](https://github.com/mybatis/mybatis-dynamic-sql/pull/346))
- Add Better Support for MyBatis Multi-Row Inserts that Return Generated Keys ([#349](https://github.com/mybatis/mybatis-dynamic-sql/pull/349))
- Major improvement to the Kotlin DSL ([#353](https://github.com/mybatis/mybatis-dynamic-sql/pull/353))
- Remove use of "record" as an identifier (it is restricted in JDK16) ([#357](https://github.com/mybatis/mybatis-dynamic-sql/pull/357))

## Release 1.2.1 - September 29, 2020

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.1+)

### Fixed

- Fixed a bug where the In conditions could render incorrectly in certain circumstances. ([#239](https://github.com/mybatis/mybatis-dynamic-sql/issues/239))

### Added

- Added a callback capability to the "In" conditions that will be called before rendering when the conditions are empty. Also, removed the option that forced the library to render invalid SQL in that case. ([#241](https://github.com/mybatis/mybatis-dynamic-sql/pull/241))
- Added a utility mapper for MyBatis that allows you to run any select query without having to predefine a result mapping. ([#255](https://github.com/mybatis/mybatis-dynamic-sql/pull/255))
- Added utility mappers for MyBatis that allow you to run generic CRUD operations. ([#263](https://github.com/mybatis/mybatis-dynamic-sql/pull/263))

## Release 1.2.0 - August 19, 2020

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.0+)

### General Announcements

This release includes major improvements to the Spring support in the library. Spring support is now functionally equivalent to MyBatis support.

This release includes a significant refactoring of the classes in the "org.mybatis.dynamic.sql.select.function" package. The new classes are more consistent and flexible and should be compatible with existing code at the source level (meaning code should be recompiled for the new version of the library). If you have written your own set of functions to extend the library, you will notice that the base classes 'AbstractFunction" and "AbstractMultipleColumnArithmeticFunction" are now deprecated. Their replacement classes are "AbstractUniTypeFunction" and "OperatorFunction" respectively.

With this release, we deprecated several insert methods because they were inconsistently named or awkward. All deprecated methods have documented direct replacements.

All deprecated code will be removed in the next minor release.

### Added

- Added a general insert statement that does not require a separate record class to hold values for the insert. ([#201](https://github.com/mybatis/mybatis-dynamic-sql/issues/201))
- Added the capability to specify a rendering strategy on a column to override the default rendering strategy for a statement. This will allow certain edge cases where a parameter marker needs to be formatted uniquely (for example, "::jsonb" needs to be added to parameter markers for JSON fields in PostgreSQL) ([#200](https://github.com/mybatis/mybatis-dynamic-sql/issues/200))
- Added the ability to write a function that will change the column data type ([#197](https://github.com/mybatis/mybatis-dynamic-sql/issues/197))
- Added the `applyOperator` function to make it easy to use non-standard database operators in expressions ([#220](https://github.com/mybatis/mybatis-dynamic-sql/issues/220))
- Added convenience methods for count(column) and count(distinct column) ([#221](https://github.com/mybatis/mybatis-dynamic-sql/issues/221))
- Added support for union queries in Kotlin ([#187](https://github.com/mybatis/mybatis-dynamic-sql/issues/187))
- Added the ability to write "in" conditions that will render even if empty ([#228](https://github.com/mybatis/mybatis-dynamic-sql/issues/228))
- Many enhancements for Spring including:
  - Fixed a bug where multi-row insert statements did not render properly for Spring ([#224](https://github.com/mybatis/mybatis-dynamic-sql/issues/224))
  - Added support for a parameter type converter for use cases where the Java type of a column does not match the database column type ([#131](https://github.com/mybatis/mybatis-dynamic-sql/issues/131))
  - Added a utility class which simplifies the use of the named parameter JDBC template for Java code - `org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions`
  - Added support for general inserts, multi-row inserts, batch inserts in the Kotlin DSL for Spring ([#225](https://github.com/mybatis/mybatis-dynamic-sql/issues/225))
  - Added support for generated keys in the Kotlin DSL for Spring ([#226](https://github.com/mybatis/mybatis-dynamic-sql/issues/226))

## Release 1.1.4 - November 23, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.4+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.4+)

### Added

- Added support for reusing WHERE clauses among count, delete, select, and update statements ([#152](https://github.com/mybatis/mybatis-dynamic-sql/pull/152))
- Improved Kotlin support. Previously, several overloaded methods could collide causing queries to be fragile and very dependent on having the correct imports in a Kotlin file. With this improved support there is no longer any ambiguity. ([#154](https://github.com/mybatis/mybatis-dynamic-sql/pull/154))

### Bugs Fixed

- Fixed issue where limit and offset in sub-queries could cause a parameter name collision ([#142](https://github.com/mybatis/mybatis-dynamic-sql/pull/142))

## Release 1.1.3 - September 16, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+)

### Added

- Added support for `count(distinct ...)` ([#112](https://github.com/mybatis/mybatis-dynamic-sql/issues/112))
- Added support for multiple row inserts ([#116](https://github.com/mybatis/mybatis-dynamic-sql/issues/116))
- Utility classes and a new canonical pattern for MyBatis Generator (CRUD) mappers ([#118](https://github.com/mybatis/mybatis-dynamic-sql/issues/118)) ([#125](https://github.com/mybatis/mybatis-dynamic-sql/pull/125)) ([#128](https://github.com/mybatis/mybatis-dynamic-sql/pull/128))
- Kotlin Extensions and Kotlin DSL ([#133](https://github.com/mybatis/mybatis-dynamic-sql/pull/133)) ([#139](https://github.com/mybatis/mybatis-dynamic-sql/pull/139))

## Release 1.1.2 - July 5, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+)

### Added

- Changed the public SQLBuilder API to accept Collection instead of List for in conditions and batch record inserts. This should have no impact on existing code, but allow for some future flexibility ([#88](https://github.com/mybatis/mybatis-dynamic-sql/pull/88))
- Added the ability to have table catalog and/or schema calculated at runtime. This is useful for situations where there are different database schemas for different environments, or in some sharding situations ([#92](https://github.com/mybatis/mybatis-dynamic-sql/pull/92))
- Add support for paging queries with "offset" and "fetch first" - this seems to be standard on most databases ([#96](https://github.com/mybatis/mybatis-dynamic-sql/pull/96))
- Added the ability to call a builder method on any intermediate object in a select statement and receive a fully rendered statement. This makes it easier to build very dynamic queries ([#106](https://github.com/mybatis/mybatis-dynamic-sql/pull/106))
- Add the ability to modify values on any condition before they are placed in the parameter map ([#105](https://github.com/mybatis/mybatis-dynamic-sql/issues/105))
- Add the ability to call `where()` with no parameters. This aids in constructing very dynamic queries ([#107](https://github.com/mybatis/mybatis-dynamic-sql/issues/107))

## Release 1.1.1 - April 7, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+)

### Added

- Limit and offset support in the select statement
- Utilities for Spring Batch
- All conditions now support conditional rendering with lambdas
- Select \* support
- Union all support

### Bugs Fixed

- Fixed self joins

## Release 1.1.0 - April 24, 2018

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+)

### Added

- Support for optional conditions
- Support for column comparison conditions
- Support for sub-queries in the update statement
- Support for expressions and constants in the select statement
- Support for function in the update statement

### Bugs Fixed

- Support group by after where

## Initial Release - December 17, 2017
