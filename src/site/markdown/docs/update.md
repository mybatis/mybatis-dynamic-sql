# Update Statements
Update statements are composed by specifying the table and columns to update, and an optional where clause.  For example:

```java
    UpdateStatementProvider updateStatement = update(animalData)
            .set(bodyWeight).equalTo(record.getBodyWeight())
            .set(animalName).equalToNull()
            .where(id, isIn(1, 5, 7))
            .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
            .or(id, isGreaterThan(60))
            .and(bodyWeight, isBetween(1.0).and(3.0))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    int rows = mapper.update(updateStatement);
```

Notice the `set` method. It is used to set the value of a database column.  There are several options for setting a value:

1. `set(column).equalToNull()` will set a null into a column
2. `set(column).equalToConstant(String constant)` will set a constant into a column.  The constant_value will be written into the generated update statement exactly as entered
3. `set(column).equalToStringConstant(String constant)` will set a constant into a column.  The constant_value will be written into the generated update statement surrounded by single quote marks (as an SQL String)
4. `set(column).equalTo(T value)` will set a value into a column.  The value will be bound to the SQL statement as a prepared statement parameter
5. `set(column).equalTo(Supplier<T> valueSupplier)` will set a value into a column.  The value will be bound to the SQL statement as a prepared statement parameter
6. `set(column).equalToWhenPresent(T value)` will set a value into a column if the value is non-null.  The value of the property will be bound to the SQL statement as a prepared statement parameter.  This is used to generate a "selective" update as defined in MyBatis Generator.
7. `set(column).equalToWhenPresent(Supplier<T> valueSupplier)` will set a value into a column if the value is non-null.  The value of the property will be bound to the SQL statement as a prepared statement parameter.  This is used to generate a "selective" update as defined in MyBatis Generator.
8. `set(column).equalTo(Buildable<SelectModel> selectModelBuilder)` will set the result of a sub-query into a column.  The query should only have one column and the type of the returned column must be able to be converted by the database if it is not the same type. These constraints are NOT validated by the library.
9. `set(column).equalTo(BasicColumn rightColumn)` will set the value of a column the be the value of another column.  This is also useful for specifying a function such as add, subtract, etc.

You can also build an update statement without a where clause.  This will update every row in a table.
For example:

```java
    UpdateStatementProvider updateStatement = update(animalData)
            .set(bodyWeight).equalTo(record.getBodyWeight())
            .set(animalName).equalToNull()
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

## Annotated Mapper for Update Statements

The UpdateStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the update method should look like this:
  
```java
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);
...
```

## XML Mapper for Update Statements

We do not recommend using an XML mapper for update statements, but if you want to do so the UpdateStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the update method should look like this in the Java interface:
  
```java
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

...
    int update(UpdateStatementProvider updateStatement);
...

```

The XML element should look like this:

```xml
  <update id="update">
    ${updateStatement}
  </update>
```
