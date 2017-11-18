# Delete Statements

Delete statements are composed with a table and a where clause.  The result of building a delete
statement is a DeleteProvider object.  For example

```java
    DeleteProvider deleteProvider = deleteFrom(simpleTable)
            .where(occupation, isNull())
            .build()
            .render(RenderingStrategy.MYBATIS3);
```
You can also build a delete statement without a where clause.  This will delete every row in a table.
For example:

```java
    DeleteProvider deleteProvider = deleteFrom(foo)
            .build()
            .render(RenderingStrategy.MYBATIS3);
``` 

## Annotated Mapper for Delete Statements

The DeleteProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the delete method should look like this:
  
```java
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.dynamic.sql.delete.render.DeleteProvider;

@Mapper
public interface SimpleTableAnnotatedMapper {
    @Delete({
        "${fullDeleteStatement}"
    })
    int delete(DeleteProvider deleteProvider);
}
```

## XML Mapper for Delete Statements

The DeleteProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an XML mapper, the delete method should look like this:
  
```java
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.dynamic.sql.delete.render.DeleteProvider;

@Mapper
public interface SimpleTableAnnotatedMapper {
    int delete(DeleteProvider deleteProvider);
}
```

```xml
  <delete id="delete">
    ${fullDeleteStatement}
  </delete>
```
