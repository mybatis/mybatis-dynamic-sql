# Delete Statements

Delete statements are composed with a table and an optional where clause.  The result of building a delete
statement is a DeleteStatementProvider object.  For example

```java
    DeleteStatementProvider deleteStatement = deleteFrom(simpleTable)
            .where(occupation, isNull())
            .build()
            .render(RenderingStrategies.MYBATIS3);
```
You can also build a delete statement without a where clause.  This will delete every row in a table.
For example:

```java
    DeleteStatementProvider deleteStatement = deleteFrom(foo)
            .build()
            .render(RenderingStrategies.MYBATIS3);
``` 

## Annotated Mapper for Delete Statements

The DeleteStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the delete method should look like this:
  
```java
import org.apache.ibatis.annotations.DeleteProvider;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);
...

```

## XML Mapper for Delete Statements

We do not recommend using an XML mapper for delete statements, but if you want to do so the DeleteStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the delete method should look like this in the Java interface:
  
```java
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;

...
    int delete(DeleteStatementProvider deleteStatement);
...

```

The XML element should look like this:

```xml
  <delete id="delete">
    ${deleteStatement}
  </delete>
```
