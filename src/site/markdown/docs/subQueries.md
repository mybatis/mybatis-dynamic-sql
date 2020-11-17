# Subquery Support
The library currently supports subqueries in the following areas:

1. In certain where conditions
1. In certain insert statements
1. In update statements
1. In the "from" clause of a select statement

## Subqueries in Where Conditions
The library support subqueries in the following where conditions:

- isEqualTo
- isNotEqualTo
- isIn
- isNotIn
- isGreaterThan
- isGreaterThanOrEqualTo
- isLessThan
- isLessThanOrEqualTo

A Java example is as follows:

```java
SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
        .from(animalData)
        .where(brainWeight, isEqualTo(
                select(min(brainWeight))
                .from(animalData)
            )
        )
        .orderBy(animalName)
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

### Kotlin Support
The library includes Kotlin versions of the where conditions that allow use of the Kotlin subquery builder. The Kotlin
where conditions are in the `org.mybatis.dynamic.sql.util.kotlin` package. An example is as follows:

```kotlin
val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
    from(Person)
    where(id, isEqualTo {
        select(max(id)) {
            from(Person)
        }
    })
}
```

## Subqueries in Insert Statements
The library supports an INSERT statement that retrieves values from a SELECT statement. For example:

```java
InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
        .withColumnList(id, animalName, bodyWeight, brainWeight)
        .withSelectStatement(
            select(id, animalName, bodyWeight, brainWeight)
            .from(animalData)
            .where(id, isLessThan(22))
        )
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

### Kotlin Support

The library includes a Kotlin builder for subqueries in insert statements that integrates with the select DSL. You
can write inserts like this:

```kotlin
val insertStatement = insertSelect(Person) {
    columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
    select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
        from(Person)
        orderBy(id)
    }
}
```

## Subqueries in Update Statements
The library supports setting update values based on the results of a subquery. For example:

```java
UpdateStatementProvider updateStatement = update(animalData)
        .set(brainWeight).equalTo(
            select(avg(brainWeight))
            .from(animalData)
            .where(brainWeight, isGreaterThan(22.0))
        )
        .where(brainWeight, isLessThan(1.0))
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

### Kotlin Support
The library includes a Kotlin builder for subqueries in update statements that integrates
with the select DSL. You can write subqueries like this:

```kotlin
val updateStatement = update(Person) {
    set(addressId).equalToQueryResult {
        select(add(max(addressId), constant<Int>("1"))) {
            from(Person)
        }
    }
    where(id, isEqualTo(3))
}
```

Note the Kotlin method name is `set(xxx).equalToQueryResult(...)` - this is to avoid a collison with
other methods in the update DSL.

## Subqueries in a From Clause

The library supports subqueries in from clauses and the syntax is a natural extension of the
select DSL. An example is as follows:

```java
DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

SelectStatementProvider selectStatement =
    select(animalName, rowNum)
    .from(
        select(id, animalName)
        .from(animalData)
        .where(id, isLessThan(22))
        .orderBy(animalName.descending())
    )
    .where(rowNum, isLessThan(5))
    .and(animalName, isLike("%a%"))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

Notice the use of a `DerivedColumn` to easily specify a function like `rownum()` that can be
used both in the select list and in a where condition.

### Table Qualifiers with Subqueries

The library attempts to automatically calculate table qualifiers. If a table qualifier is specified,
the library will automatically render the table qualifier on all columns associated with the
table. For example with the following query: 

```java
SelectStatementProvider selectStatement =
    select(id, animalName)
    .from(animalData, "ad")
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

The library will render SQL as:

```sql
select ad.id, ad.animal_name
from AnimalData ad
```

Notice that the table qualifier `ad` is automatically applied to columns in the select list.

In the case of join queries the table qualifier specified, or if not specified the table name
itself, will be used as the table qualifier.

With subqueries, it is important to understand the limits of automatic table qualifiers. The rules are
as follows:

1. The scope of automatic table qualifiers is limited to a single select statement. For subqueries, the outer
   query has a different scope than the subquery.
1. A qualifier can be applied to a subquery as a whole, but that qualifier is not automatically applied to
   any column

As an example, consider the following query:

```java
DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

SelectStatementProvider selectStatement =
    select(animalName, rowNum)
    .from(
        select(id, animalName)
        .from(animalData, "a")
        .where(id, isLessThan(22))
        .orderBy(animalName.descending()),
        "b"
    )
    .where(rowNum, isLessThan(5))
    .and(animalName, isLike("%a%"))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

The rendered SQL will be as follows:

```sql
select animal_name, rownum()
from (select a.id, a.animal_name
      from AnimalDate a
      where id < #{parameters.p1}
      order by animal_name desc) b
where rownum() < #{parameters.p2}
  and animal_name like  #{parameters.p3} 
```

Notice that the qualifier `a` is automatically applied to columns in the subquery and that the 
qualifier `b` is not applied anywhere.

If your query requires the subquery qualifier to be applied to columns in the outer select list,
you can manually apply the qualifier to columns as follows:

```java
DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

SelectStatementProvider selectStatement =
    select(animalName.qualifiedWith("b"), rowNum)
    .from(
        select(id, animalName)
        .from(animalData, "a")
        .where(id, isLessThan(22))
        .orderBy(animalName.descending()),
        "b"
    )
    .where(rowNum, isLessThan(5))
    .and(animalName.qualifiedWith("b"), isLike("%a%"))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

In this case, we have manually applied the qualifier `b` to columns in the outer query. The
rendered SQL looks like this:

```sql
select b.animal_name, rownum()
from (select a.id, a.animal_name
      from AnimalDate a
      where id < #{parameters.p1}
      order by animal_name desc) b
where rownum() < #{parameters.p2}
  and b.animal_name like  #{parameters.p3} 
```

### Kotlin Support

The library includes a Kotlin builder for subqueries that integrates with the select DSL. You
can write queries like this:

```kotlin
val selectStatement =
    select(firstName, rowNum) {
        from {
            select(id, firstName) {
                from(Person)
                where(id, isLessThan(22))
                orderBy(firstName.descending())
            }
        }
        where(rowNum, isLessThan(5))
        and(firstName, isLike("%a%"))
    }
```

The same rules about table qualifiers apply as stated above. In Kotlin, a subquery qualifier
can be added with the overloaded "+" operator as shown below:

```kotlin
val selectStatement =
    select(firstName, rowNum) {
        from {
            select(id, firstName) {
                from(Person, "a")
                where(id, isLessThan(22))
                orderBy(firstName.descending())
            }
            + "b"
        }
        where(rowNum, isLessThan(5))
        and(firstName, isLike("%a%"))
    }
```

In this case the `a` qualifier is used in the context of the inner select statement and
the `b` qualifier is applied to the subquery as a whole.