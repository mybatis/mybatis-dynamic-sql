# Kotlin Support for MyBatis3
This library supports Kotlin with no modification. Many of the examples will compile as-is when translated to Kotlin. But the library does supply some Kotlin extensions that allow for coding Kotlin in a more idiomatic manner.

This page will show our recommended pattern for using the library from Kotlin. The code shown on this page is from the `src/test/kotlin/examples/kotlin` directory in this repository. That directory contains a complete example of using the library with Kotlin.
 
## Kotlin Dynamic SQL Support Objects
Because Kotlin does not support static class members, we recommend a simpler pattern for creating the class containing the support objects. For example:

```kotlin
object PersonDynamicSqlSupport {
    object Person : SqlTable("Person") {
        // the 'as' casts are to avoid a warning related to platform types
        val id = column<Int>("id", JDBCType.INTEGER) as SqlColumn<Int>
        val firstName = column<String>("first_name", JDBCType.VARCHAR) as SqlColumn<String>
        val lastName = column<String>("last_name", JDBCType.VARCHAR) as SqlColumn<String>
        val birthDate = column<Date>("birth_date", JDBCType.DATE) as SqlColumn<Date>
        val employed = column<Boolean>("employed", JDBCType.VARCHAR, "examples.kotlin.YesNoTypeHandler") as SqlColumn<Boolean>
        val occupation = column<String>("occupation", JDBCType.VARCHAR) as SqlColumn<String>
        val addressId = column<Int>("address_id", JDBCType.INTEGER) as SqlColumn<Int>
    }
}
```

This object is a singleton containing the `SqlTable` and `SqlColumn` objects that map to the database table. Note that the columns are cast to the proper type. This tells Kotlin that the objects are null safe. Without the cast, you may see warnings related to nullable platform types.

## Kotlin Mappers
Kotlin does not distinguish between Java's default methods and regular methods in an interface, if you create a single mapper that includes both abstract and non-abstract methods, MyBatis will be confused and throw errors. For this reason, Kotlin mapper interfaces should only contain the actual MyBatis mapper interface methods. What would normally be coded as default or static methods added to a mapper interface should be coded as an extension method in Kotlin. For example, a simple MyBatis mapper could be coded like this:

```kotlin
interface PersonMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(id = "PersonRecordResult", value = [
        Result(column = "a_id", property = "id"),
        Result(column = "first_name", property = "firstName"),
        Result(column = "last_name", property = "lastName"),
        Result(column = "birth_date", property = "birthDate"),
        Result(column = "employed", property = "employed", typeHandler = YesNoTypeHandler::class),
        Result(column = "occupation", property = "occupation"),
        Result(column = "address_id", property = "addressId")
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonRecord>
}
```

And then an extension method could be added to make a shortcut method as follows:

```kotlin
fun PersonMapper.select(helper: SelectListHelper<PersonRecord>) =
        helper(selectWithKotlinMapper(this::selectMany, id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
                .from(Person))
                .build()
                .execute()
```

This extension method shows the use of the `SelectListHelper` class and the `selectWithKotlinMapper` method - these are Kotlin extensions supplied with the library. We will detail their use below. For now, you can see that the above extension method can be used in client code as follows:

```kotlin
val rows = mapper.select {
    where(id, isEqualTo(1))
}
```

This shows that the Kotlin support enables a more Kotlin-like DSL experience.
 
## Count Method Support

The goal of count method support is to enable the creation of methods that execute a count query allowing a user to specify a where clause at runtime, but abstracting away all other details.

To use this support, we envision creating two methods on a MyBatis mapper interface. The first method is the standard MyBatis Dynamic SQL method that will execute a select:

```java
@SelectProvider(type=SqlProviderAdapter.class, method="select")
long count(SelectStatementProvider selectStatement);
```

This is a standard method for MyBatis Dynamic SQL that executes a query and returns a `long`. The second method will reuse this method and supply everything needed to build the select statement except the where clause:

```java
default long count(MyBatis3CountHelper helper) {
    return helper.apply(SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
            .from(simpleTable))
            .build()
            .execute();
}
```

This method shows the use of `MyBatis3CountHelper` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause. Clients can use the method as follows:

```java
long rows = mapper.count(h ->
        h.where(occupation, isNull()));
```

There is a utility method that can be used to count all rows in a table:

```java
long rows = mapper.count(MyBatis3CountHelper.allRows());
```

## Delete Method Support

The goal of delete method support is to enable the creation of methods that execute a delete statement allowing a user to specify a where clause at runtime, but abstracting away all other details.

To use this support, we envision creating two methods on a MyBatis mapper interface. The first method is the standard MyBatis Dynamic SQL method that will execute a delete:

```java
@DeleteProvider(type=SqlProviderAdapter.class, method="delete")
int delete(DeleteStatementProvider deleteStatement);
```

This is a standard method for MyBatis Dynamic SQL that executes a delete and returns an `int` - the number of rows deleted. The second method will reuse this method and supply everything needed to build the delete statement except the where clause:

```java
default int delete(MyBatis3DeleteHelper helper) {
    return helper.apply(MyBatis3Utils.deleteFrom(this::delete, simpleTable))
            .build()
            .execute();
}
```

This method shows the use of `MyBatis3DeleteHelper` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause. Clients can use the method as follows:

```java
int rows = mapper.delete(h ->
        h.where(occupation, isNull()));
```

There is a utility method that can be used to delete all rows in a table:

```java
int rows = mapper.delete(MyBatis3DeleteHelper.allRows());
```

## Select Method Support

The goal of select method support is to enable the creation of methods that execute a select statement allowing a user to specify a where clause and/or order by clause at runtime, but abstracting away all other details.

To use this support, we envision creating several methods on a MyBatis mapper interface. The first two methods are the standard MyBatis Dynamic SQL method that will execute a select:

```java
@SelectProvider(type=SqlProviderAdapter.class, method="select")
@Results(id="SimpleTableResult", value= {
        @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
        @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
        @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
        @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR),
        @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
})
List<SimpleTableRecord> selectMany(SelectStatementProvider selectStatement);
    
@SelectProvider(type=SqlProviderAdapter.class, method="select")
@ResultMap("SimpleTableResult")
Optional<SimpleTableRecord> selectOne(SelectStatementProvider selectStatement);
```

These two methods are standard methods for MyBatis Dynamic SQL. They execute a select and return either a list of records, or a single record.

The `selectOne` method can be used to implement a generalized select one method:

```java
default Optional<SimpleTableRecord> selectOne(MyBatis3SelectOneHelper<SimpleTableRecord> helper) {
    return helper.apply(SelectDSL.selectWithMapper(this::selectOne, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable))
            .build()
            .execute();
}
```

This method shows the use of `MyBatis3SelectOneHelper` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause.

The general `selectOne` method can be used to implement a `selectByPrimaryKey` method:

```java
default Optional<SimpleTableRecord> selectByPrimaryKey(Integer id_) {
    return selectOne(h ->
        h.where(id, isEqualTo(id_))
    );
}
```

The `selectMany` method can be used to implement generalized select methods where a user can specify a where clause and/or an order by clause. Typically we recommend two of these methods - for select, and select distinct: 

```java
default List<SimpleTableRecord> select(MyBatis3SelectListHelper<SimpleTableRecord> helper) {
    return helper.apply(SelectDSL.selectWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate,
                employed, occupation)
            .from(simpleTable))
            .build()
            .execute();
}
    
default List<SimpleTableRecord> selectDistinct(MyBatis3SelectListHelper<SimpleTableRecord> helper) {
    return helper.apply(SelectDSL.selectDistinctWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName,
                birthDate, employed, occupation)
            .from(simpleTable))
            .build()
            .execute();
}
```


These methods show the use of `MyBatis3SelectListHelper` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause and/or an order by clause.

Clients can use the methods as follows:

```java
List<SimpleTableRecord> rows = mapper.select(h ->
        h.where(id, isEqualTo(1))
        .or(occupation, isNull()));
```

There are utility methods that will select all rows in a table:

```java
List<SimpleTableRecord> rows =
    mapper.selectByExample(MyBatis3SelectListHelper.allRows());
```

The following query will select all rows in a specified order:

```java
List<SimpleTableRecord> rows =
    mapper.selectByExample(MyBatis3SelectListHelper.allRowsOrderedBy(lastName, firstName));
```

## Update Method Support

The goal of update method support is to enable the creation of methods that execute an update statement allowing a user to specify values to set and a where clause at runtime, but abstracting away all other details.

To use this support, we envision creating several methods on a MyBatis mapper interface. The first method is a standard MyBatis Dynamic SQL method that will execute a update:

```java
@UpdateProvider(type=SqlProviderAdapter.class, method="update")
int update(UpdateStatementProvider updateStatement);
```

This is a standard method for MyBatis Dynamic SQL that executes a query and returns an `int` - the number of rows updated. The second method will reuse this method and supply everything needed to build the update statement except the values and the where clause:

```java
default int update(MyBatis3UpdateHelper helper) {
    return helper.apply(MyBatis3Utils.update(this::update, simpleTable))
            .build()
            .execute();
}
```

This method shows the use of `MyBatis3UpdateHelper` which is a specialization of a `java.util.Function` that will allow a user to supply values and a where clause. Clients can use the method as follows:

```java
int rows = mapper.update(h ->
    h.set(occupation).equalTo("Programmer")
    .where(id, isEqualTo(100)));
```

All rows in a table can be updated by simply omitting the where clause:

```java
int rows = mapper.update(h ->
    h.set(occupation).equalTo("Programmer"));
```

It is also possible to write a utility method that will set values. For example:

```java
static UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> setSelective(SimpleTableRecord record,
        UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> dsl) {
    return dsl.set(id).equalToWhenPresent(record::getId)
            .set(firstName).equalToWhenPresent(record::getFirstName)
            .set(lastName).equalToWhenPresent(record::getLastName)
            .set(birthDate).equalToWhenPresent(record::getBirthDate)
            .set(employed).equalToWhenPresent(record::getEmployed)
            .set(occupation).equalToWhenPresent(record::getOccupation);
}
```

This method will selectively set values if corresponding fields in a record are non null. This method can be used as follows:

```java
rows = mapper.update(h ->
    setSelective(updateRecord, h)
    .where(id, isEqualTo(100)));
```

# Prior Support
Prior to version 1.1.3, it was also possible to write reusable methods, but they were a bit inconsistent with other helper methods.  

For example, it is possible to write a mapper interface like this:

```java
import static examples.simple.SimpleTableDynamicSqlSupport.*;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

@Mapper
public interface SimpleTableMapper {
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SimpleTableResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectStatementProvider selectStatement);
    
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<SimpleTableRecord>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable);
    }
}
```

Notice the `selectByExample` method - it specifies the column list and table name and returns the intermediate builder that can be used to finish the WHERE clause.  It also reuses the `selectMany` mapper method.  Mapper methods built using this added support all finish with an `execute` method that builds the statement and executes the mapper method.

The code is used like this:

```java
    List<SimpleTableRecord> rows = mapper.selectByExample()
            .where(id, isEqualTo(1))
            .or(occupation, isNull())
            .build()
            .execute();
```
