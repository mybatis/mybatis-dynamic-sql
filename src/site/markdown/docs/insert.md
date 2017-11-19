# Insert Statements
The library will generate a variety of INSERT statements.

## Single Record Insert

## Insert Selective

## Batch Insert Support

## Insert with Select

## Generated Keys
MyBatis supports returning generated key from a single record insert, or a batch insert.  In either case, it is simply a matter of configuring the insert mapper method appropriately.


## Annotated Mapper for Insert Statements

## XML Mapper for Insert Statements
We do not recommend using an XML mapper for insert statements, but if you want to do so the InsertStatement or InsertSelectStatement objects can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the insert method should look like this in the Java interface:
  
```java
import org.mybatis.dynamic.sql.insert.render.InsertStatement;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatement;

...
    int insert(InsertStatement insertStatement);
    int insertSelect(InsertSelectStatement insertSelectStatement);
...

```

The XML element should look like this (with attributes added for generated keys if necessary):

```xml
  <insert id="insert">
    ${insertStatement}
  </delete>

  <insert id="insertSelect">
    ${insertStatement}
  </delete>
```
