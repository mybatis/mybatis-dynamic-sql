# Case Expressions in the Java DSL

Support for case expressions was added in version 1.5.1. For information about case expressions in the Kotlin DSL, see
the [Kotlin Case Expressions](kotlinCaseExpressions.md) page.

## Case Statements in SQL
The library supports different types of case expressions - a "simple" case expression, and a "searched" case
expressions.

A simple case expression checks the values of a single column. It looks like this:

```sql
select case id
    when 1, 2, 3 then true 
    else false
  end as small_id
from foo
```

Some databases also support simple comparisons on simple case expressions, which look lke this:

```sql
select case total_length
    when < 10 then 'small'
    when > 20 then 'large'
    else 'medium'
  end as tshirt_size
from foo
```

A searched case expression allows arbitrary logic, and it can check the values of multiple columns. It looks like this:

```sql
select case
    when animal_name = 'Small brown bat' or animal_name = 'Large brown bat' then 'Bat' 
    when animal_name = 'Artic fox' or animal_name = 'Red fox' then 'Fox' 
    else 'Other'
  end as animal_type
from foo
```

## Bind Variables and Casting

The library will always render the "when" part of a case expression using bind variables. Rendering of the "then" and
"else" parts of a case expression may or may not use bind variables depending on how you write the query. In general,
the library will render "then" and "else" as constants - meaning not using bind variables. If you wish to use bind
variables for these parts of a case expressions, then you can use the `value` function to turn a constant into a 
bind variable. We will show examples of the different renderings in the following sections.

If you choose to use bind variables for all "then" and "else" values, it is highly likely that the database will
require you to specify an expected datatype by using a `cast` function.

Even for "then" and "else" sections that are rendered with constants, you may still desire to use a `cast` in some
cases. For example, if you specify Strings for all "then" and "else" values, the database will likely return all
values as datatype CHAR with the length of the longest constant string. Typically, we would prefer the use of VARCHAR,
so we don't have to strip trailing blanks from the results. This is a good use for a `cast` with a constant.
Similarly, Java float constants are often interpreted by databases as BigDecimal. You can use a `cast` to have them
returned as floats.

Note: in the following sections we will use `?` to show a bind variable, but the actual rendered SQL will be different
because bind variables will be rendered appropriately for the execution engine you are using (either MyBatis or Spring).

Also note: in Java, `case` and `else` are reserved words - meaning we cannot use them as method names. For this reason,
the library uses `case_` and `else_` respectively as method names.

Full examples for case expressions are in the test code for the library here:
https://github.com/mybatis/mybatis-dynamic-sql/blob/master/src/test/java/examples/animal/data/CaseExpressionTest.java

## Java DSL for Simple Case Statements with Simple Values

A simple case expression can be coded like the following in the Java DSL:

```java
select(case_(id)
    .when(1, 2, 3).then(true)
    .else_(false)
  .end().as("small_id"))
.from(foo)
```

A statement written this way will render as follows:

```sql
select case id when ?, ?, ? then true else false end as small_id from foo
```

Note that the "then" and "else" parts are NOT rendered with bind variables. If you with to use bind variables, then
you can write the query as follows:

```java
select(case_(id)
    .when(1, 2, 3).then(value(true))
    .else_(value(false))
  .end().as("small_id"))
.from(foo)
```

In this case, we are using the `value` function to denote a bind variable. The SQL will now be rendered as follows:

```sql
select case id when ?, ?, ? then ? else ? end as small_id from foo
```

*Important*: Be aware that your database may throw an exception for SQL like this because the database cannot determine
the datatype of the resulting column. If that happens, you will need to cast one or more of the variables to the
expected data type. Here's an example of using the `cast` function:

```java
select(case_(id)
    .when(1, 2, 3).then(value(true))
    .else_(cast(value(false)).as("BOOLEAN)"))
  .end().as("small_id"))
.from(foo)
```

In this case, the SQL will render as follows:

```sql
select case id when ?, ?, ? then ? else cast(? as BOOLEAN) end as small_id from foo
```

In our testing, casting a single bound value is enough to inform the database of your expected datatype, but
you should perform your own testing.

## Java DSL for Simple Case Statements with Conditions

A simple case expression can be coded like the following in the Java DSL:

```java
select(case_(total_length)
    .when(isLessThan(10)).then_("small")
    .when(isGreaterThan(20)).then_("large")
    .else_("medium")
  .end().as("tshirt_size"))
.from(foo)
```

A statement written this way will render as follows:

```sql
select case total_length when <  ? then 'small' when > ? then 'large' else 'medium' end as tshirt_size from foo
```

Note that the "then" and "else" parts are NOT rendered with bind variables. If you with to use bind variables, then
you can use the `value` function as shown above.

A query like this could be a good place to use casting with constants. Most databases will return the calculated
"tshirt_size" column as CHAR(6) - so the "small" and "large" values will have a trailing blank. If you wish to use
VARCHAR, you can use the `cast` function as follows:

```java
select(case_(total_length)
    .when(isLessThan(10)).then_("small")
    .when(isGreaterThan(20)).then_("large")
    .else_(cast("medium").as("VARCHAR(6)"))
  .end().as("tshirt_size"))
.from(foo)
```

In this case, we are using the `cast` function to specify the datatype of a constant. The SQL will now be rendered as
follows (without the line breaks):

```sql
select case total_length
    when <  ? then 'small' when > ? then 'large'
    else cast('medium' as VARCHAR(6)) end as tshirt_size from foo
```

## Java DSL for Searched Case Statements

A searched case statement is written as follows:

```java
select(case_()
    .when(animalName, isEqualTo("Small brown bat")).or(animalName, isEqualTo("Large brown bat")).then("Bat")
    .when(animalName, isEqualTo("Artic fox")).or(animalName, isEqualTo("Red fox")).then("Fox")
    .else_("Other")
  .end().as("animal_type"))
.from(foo)
```

The full syntax of "where" and "having" clauses is supported in the "when" clause - but that may or may not be supported
by your database. Testing is crucial. In addition, the library does not support conditions that don't render in a case
statement - so avoid the use of conditions like "isEqualToWhenPresent", etc.

The rendered SQL will be as follows (without the line breaks):
```sql
select case
    when animal_name = ? or animal_name = ? then 'Bat' 
    when animal_name = ? or animal_name = ? then 'Fox' 
    else 'Other'
  end as animal_type
from foo
```

The use of the `value` function to support bind variables, and the use of casting, is the same is shown above.
