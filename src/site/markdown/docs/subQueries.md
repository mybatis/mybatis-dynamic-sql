# Subquery Support
The library currently supports subqueries in the following areas:

1. In where clauses - both with the "exists" operator and with column-based conditions
1. In certain insert statements
1. In update statements
1. In the "from" clause of a select statement
1. In join clauses of a select statement

Before we show examples of subqueries, it is important to understand how the library generates and applies
table qualifiers in select statements. We'll cover that first.

## Table Qualifiers in Select Statements

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
itself, will be used as the table qualifier. However, this function is disabled for joins on subqueries.

With subqueries, it is important to understand the limits of automatic table qualifiers. The rules are
as follows:

1. The scope of automatic table qualifiers is limited to a single select statement. For subqueries, the outer
   query has a different scope than the subquery.
1. A qualifier can be applied to a subquery, but that qualifier is not automatically applied to
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

## Subqueries in Where Conditions
The library support subqueries in the following where conditions:

- exists
- notExists
- isEqualTo
- isNotEqualTo
- isIn
- isNotIn
- isGreaterThan
- isGreaterThanOrEqualTo
- isLessThan
- isLessThanOrEqualTo

An example of an exists subquery is as follows:

```java
SelectStatementProvider selectStatement = select(itemMaster.allColumns())
        .from(itemMaster, "im")
        .where(exists(
                select(orderLine.allColumns())
                .from(orderLine, "ol")
                .where(orderLine.itemId, isEqualTo(itemMaster.itemId))
        ))
        .orderBy(itemMaster.itemId)
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

Note that the qualifier for the outer query ("im") is automatically applied to the inner query, as well as the
qualifier for the inner query ("ol"). Carrying alias from an outer query to an inner query is only supported with
exists or not exists sub queries.

An example of a column based subquery is as follows:

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
where conditions are in the `org.mybatis.dynamic.sql.util.kotlin` package.
 
An example of an exists subquery is as follows:
```kotlin
val selectStatement = select(ItemMaster.allColumns()) {
    from(ItemMaster, "im")
    where {
       exists {
          select(OrderLine.allColumns()) {
             from(OrderLine, "ol")
             where { OrderLine.itemId isEqualTo ItemMaster.itemId }
          }
       }
       orderBy(ItemMaster.itemId)
    }
}
```

An example of a column based subquery is as follows:
```kotlin
val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
    from(Person)
    where {
       id isEqualTo {
          select(max(id)) {
             from(Person)
          }
       }
    }
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
    set(addressId) equalToQueryResult {
        select(add(max(addressId), constant<Int>("1"))) {
            from(Person)
        }
    }
    where { id isEqualTo 3 }
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

### Kotlin Support

The library includes a Kotlin builder for subqueries that integrates with the select DSL. You
can write queries like this:

```kotlin
val selectStatement =
    select(firstName, rowNum) {
        from {
            select(id, firstName) {
                from(Person)
                where { id isLessThan 22 }
                orderBy(firstName.descending())
            }
        }
        where { rowNum isLessThan 5 }
        and { firstName isLike "%a%" }
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
                where { id isLessThan 22 }
                orderBy(firstName.descending())
            }
            + "b"
        }
        where { rowNum isLessThan 5 }
        and { firstName isLike "%a%" }
    }
```

In this case the `a` qualifier is used in the context of the inner select statement and
the `b` qualifier is applied to the subquery as a whole.

## Subqueries in Join Clauses
The library supports subqueries in "join" clauses similarly to subqueries in "from" clauses. For example:

```java
SelectStatementProvider selectStatement = select(orderMaster.orderId, orderMaster.orderDate,
        orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
    .from(orderMaster, "om")
    .join(
        select(orderDetail.orderId, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
        .from(orderDetail),
        "od")
    .on(orderMaster.orderId, equalTo(orderDetail.orderId.qualifiedWith("od")))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

This is rendered as:

```sql
select om.order_id, om.order_date, line_number, description, quantity
from OrderMaster om
join (select order_id, line_number, description, quantity from OrderDetail) od
on om.order_id = od.order_id
```

Notice that the subquery is aliased with "od", but that alias is not automatically applied so it must
be specified when required. If in doubt, specify the alias with the `qualifiedBy` method.

### Kotlin Support
The Kotlin select build supports subqueries in joins as follows:

```kotlin
val selectStatement = select(OrderLine.orderId, OrderLine.quantity,
        "im"(ItemMaster.itemId), ItemMaster.description) {
    from(OrderMaster, "om")
    join(OrderLine, "ol") {
        on(OrderMaster.orderId) equalTo OrderLine.orderId
    }
    leftJoin(
       {
          select(ItemMaster.allColumns()) {
             from(ItemMaster)
          }
          + "im"
      }
    ) {
        on(OrderLine.itemId) equalTo "im"(ItemMaster.itemId)
    }
    orderBy(OrderLine.orderId, ItemMaster.itemId)
}
```

This is rendered as:

```sql
select ol.order_id, ol.quantity, im.item_id, description
from OrderMaster om join OrderLine ol on om.order_id = ol.order_id
left join (select * from ItemMaster) im on ol.item_id = im.item_id
order by order_id, item_id
```

Notice again that sub query qualifiers must be specified when needed. In this case we use a Kotlin trick - an invoke
operator function that gets close to natural SQL syntax (```"im"(ItemMaster.itemId)```).  Also note that the Kotlin
join methods accept two lambda functions - one for the sub query and one for the join specification. Only the join
specification can be outside the parenthesis of the join methods.
