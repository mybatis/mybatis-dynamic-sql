# WHERE Clause Support

This library supports the creation of very flexible WHERE clauses.  WHERE clauses can be added to DELETE, SELECT,
and UPDATE statements.  WHERE clauses can also stand alone for use in other hand coded SQL.

## Simple WHERE Clauses

The simplest WHERE clause is of this form:

```java
        SelectStatementProvider selectStatement = select(count())
                .from(simpleTable)
                .where(id, isEqualTo(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

The library ships with a wide variety of conditions that can be used in WHERE clauses including
"in", "like", "between", "isNull", "isNotNull", and all the normal comparison operators.  For example:

```java
        SelectStatementProvider selectStatement = select(count())
                .from(simpleTable)
                .where(id, isBetween(3).and(6))
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

```java
        SelectStatementProvider selectStatement = select(count())
                .from(simpleTable)
                .where(id, isIn(3,4,5))
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

```java
        SelectStatementProvider selectStatement = select(count())
                .from(simpleTable)
                .where(id, isNotNull())
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

## Complex WHERE Clauses

Conditions can be "anded" and "ored" in virtually any combination. For example:

```java
        SelectStatementProvider selectStatement = select(count())
                .from(simpleTable, "a")
                .where(id, isGreaterThan(2))
                .or(occupation, isNull(), and(id, isLessThan(6)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

## Subqueries

Most of the conditions also support a subquery.  For example:

```java
        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column2, isIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .or(column1, isLessThan(d))
                .build()
                .render(RenderingStrategies.MYBATIS3);
```

## Stand Alone Where Clauses
You can use the where clause support on its own if you would rather code your own SQL for the remainder of a statement.  There may be several reasons to do this - mainly if the library doesn't support some SQL or MyBatis feature you want to use.  A good example would be paginated queries which are currently not support by the library.  If you want to use a stand alone where clause, you can code a mapper method that looks like this:

```java
    @Select({
        "select id, animal_name, brain_weight, body_weight",
        "from AnimalData",
        "${whereClause}"
    })
    @ResultMap("AnimalDataResult")
    List<AnimalData> selectByExample(WhereClauseProvider whereClause);
```

You can build a stand alone where clause and call your mapper like this:

```java
    WhereClauseProvider whereClause = where(id, isNotBetween(10).and(60))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    List<AnimalData> animals = mapper.selectByExample(whereClause);
```
This method works well when there are no other parameters needed for the statement and when there are no table aliases involved.  If you have those other needs, then see the following.

### Table Aliases
If you need to use a table alias in the generated where clause you can supply it on the render method using the `TableAliasCalculator` class.  For example, if you have a mapper like this:

```java
    @Select({
        "select a.id, a.animal_name, a.brain_weight, a.body_weight",
        "from AnimalData a",
        "${whereClause}"
    })
    @ResultMap("AnimalDataResult")
    List<AnimalData> selectByExampleWithAlias(WhereClauseProvider whereClause);
```
Then you can specify the alias for the generated WHERE clause on the render method like this:

```java
    WhereClauseProvider whereClause = where(id, isEqualTo(1), or(bodyWeight, isGreaterThan(1.0)))
            .build()
            .render(RenderingStrategies.MYBATIS3, TableAliasCalculator.of(animalData, "a"));

    List<AnimalData> animals = mapper.selectByExampleWithAlias(whereClause);
```
It is more likely that you will be using table aliases with hand coded joins where there is more than on table alias.  In this case, you supply a `Map<SqlTable, String>` to the TableAliasCalculator that holds an alias for each table involved in the WHERE clause.

### Handling Multiple Parameters
By default, the WHERE clause renderer assumes that the rendered WHERE clause will be the only parameter to the mapper method. This is not always the case. For example, suppose you have a paginated query like this (this is HSQLDB syntax):

```java
    @Select({
        "select id, animal_name, brain_weight, body_weight",
        "from AnimalData",
        "${whereClauseProvider.whereClause}",
        "order by id",
        "OFFSET #{offset,jdbcType=INTEGER} LIMIT #{limit,jdbcType=INTEGER}"
    })
    @ResultMap("AnimalDataResult")
    List<AnimalData> selectByExampleWithLimitAndOffset(@Param("whereClauseProvider") WhereClauseProvider whereClause,
            @Param("limit") int limit, @Param("offset") int offset);
```

In this mapper method there are three parameters.  So in this case it will be necessary to tell the WHERE rendered what parameter name to use the for rendered where clause.  That code looks like this:

```java
    WhereClauseProvider whereClause = where(id, isLessThan(60))
            .build()
            .render(RenderingStrategies.MYBATIS3, "whereClauseProvider");
            
    List<AnimalData> animals = mapper.selectByExampleWithLimitAndOffset(whereClause, 5, 15);
```
Notice that the string `whereClauseProvider` is used both as the parameter name in the mapper `@Param` annotation and the parameter name in the `render` method.

The render method also has an override that accepts a TableAliasCalculator and a parameter name.
