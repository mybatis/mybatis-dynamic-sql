# WHERE Clause Support

This library supports the creation of very flexible WHERE clauses.  WHERE clauses can be added to DELETE, SELECT,
and UPDATE statements.

## Simple WHERE Clauses

The simplest WHERE clause is of this form:

```java
        SelectStatement selectStatement = select(count())
                .from(simpleTable)
                .where(id, isEqualTo(3))
                .build()
                .render(RenderingStrategy.MYBATIS3);
```

The library ships with a wide variety of conditions that can be used in WHERE clauses including
"in", "like", "between", "isNull", "isNotNull", and all the normal comparison operators.  For example:

```java
        SelectStatement selectStatement = select(count())
                .from(simpleTable)
                .where(id, isBetween(3).and(6))
                .build()
                .render(RenderingStrategy.MYBATIS3);
```

```java
        SelectStatement selectStatement = select(count())
                .from(simpleTable)
                .where(id, isIn(3,4,5))
                .build()
                .render(RenderingStrategy.MYBATIS3);
```

```java
        SelectStatement selectStatement = select(count())
                .from(simpleTable)
                .where(id, isNotNull())
                .build()
                .render(RenderingStrategy.MYBATIS3);
```

## Complex WHERE Clauses

Conditions can be "anded" and "ored" in virtually any combination. For example:

```java
        SelectStatement selectStatement = select(count())
                .from(simpleTable, "a")
                .where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .build()
                .render(RenderingStrategy.MYBATIS3);
```

## Subqueries

Most of the conditions also support a subquery.  For example:

```java
        SelectStatement selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column2, isIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .or(column1, isLessThan(d))
                .build()
                .render(RenderingStrategy.MYBATIS3);
```
