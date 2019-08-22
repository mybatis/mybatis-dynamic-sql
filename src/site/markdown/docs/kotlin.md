# Kotlin Support for MyBatis3
MyBatis Dynamic SQL includes Kotlin extension methods that enable an SQL DSL for Kotlin. This is the preferred method of using the library in Kotlin.

We will also note that the standard usage patterns for MyBatis Dynamic SQL and MyBatis3 in Java must be modified for Kotlin. Kotlin interfaces can contain non abstract methods (somewhat similar to Java's default methods in an interface). But using these methods in Kotlin based mapper interfaces will cause a failure with MyBatis.

This page will show our recommended pattern for using the library with Kotlin. The code shown on this page is from the `src/test/kotlin/examples/kotlin/canonical` directory in this repository. That directory contains a complete example of using the library with Kotlin.
 
## Kotlin Dynamic SQL Support Objects
Because Kotlin does not support static class members, we recommend a simpler pattern for creating the class containing the support objects. For example:

```kotlin
object PersonDynamicSqlSupport {
    object Person : SqlTable("Person") {
        val id = column<Int>("id", JDBCType.INTEGER)
        val firstName = column<String>("first_name", JDBCType.VARCHAR)
        val lastName = column<String>("last_name", JDBCType.VARCHAR)
        val birthDate = column<Date>("birth_date", JDBCType.DATE)
        val employed = column<Boolean>("employed", JDBCType.VARCHAR, "examples.kotlin.YesNoTypeHandler")
        val occupation = column<String>("occupation", JDBCType.VARCHAR)
        val addressId = column<Int>("address_id", JDBCType.INTEGER)
    }
}
```

This object is a singleton containing the `SqlTable` and `SqlColumn` objects that map to the database table. Note that the columns are cast to the proper type.

## Kotlin Mappers
Kotlin does not distinguish between Java's default methods and regular methods in an interface. If you create a Kotlin mapper interface that includes both abstract and non-abstract methods, MyBatis will be confused and throw errors. For this reason, Kotlin mapper interfaces should only contain the actual MyBatis mapper abstract interface methods. What would normally be coded as default or static methods in a mapper interface should be coded as extension methods in Kotlin. For example, a simple MyBatis mapper could be coded like this:

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
fun PersonMapper.select(completer: QueryExpressionCompleter): List<PersonRecord> =
        MyBatis3Utils.selectList(this::selectMany, selectList, Person, completer)
```

This extension method shows the use of the `QueryExpressionCompleter` type alias. This is a DSL extension supplied with the library. We will detail its use below. For now you can see that the extension method can be used in client code as follows:

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
default long count(SelectDSLCompleter completer) {
    return MyBatis3Utils.count(this::count, person, completer);
}
```

This method shows the use of `SelectDSLCompleter` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause. Clients can use the method as follows:

```java
long rows = mapper.count(c ->
        c.where(occupation, isNull()));
```

There is a utility method that can be used to count all rows in a table:

```java
long rows = mapper.count(SelectDSLCompleter.allRows());
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
default int delete(DeleteDSLCompleter completer) {
    return MyBatis3Utils.deleteFrom(this::delete, person, completer);
}
```

This method shows the use of `DeleteDSLCompleter` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause. Clients can use the method as follows:

```java
int rows = mapper.delete(c ->
        c.where(occupation, isNull()));
```

There is a utility method that can be used to delete all rows in a table:

```java
int rows = mapper.delete(DeleteDSLCompleter.allRows());
```

## Insert Method Support

The goal of insert method support is to remove some of the boilerplate code from insert methods in a mapper interfaces.

To use this support, we envision creating several methods on a MyBatis mapper interface. The first two methods are the standard MyBatis Dynamic SQL method that will execute an insert:

```java
@InsertProvider(type=SqlProviderAdapter.class, method="insert")
int insert(InsertStatementProvider<PersonRecord> insertStatement);

@InsertProvider(type=SqlProviderAdapter.class, method="insertMultiple")
int insertMultiple(MultiRowInsertStatementProvider<PersonRecord> insertStatement);
```

These two methods are standard methods for MyBatis Dynamic SQL. They execute a single row insert and a multiple row insert.

These methods can be used to implement simplified insert methods:

```java
default int insert(PersonRecord record) {
    return MyBatis3Utils.insert(this::insert, record, person, c -> 
        c.map(id).toProperty("id")
        .map(firstName).toProperty("firstName")
        .map(lastName).toProperty("lastName")
        .map(birthDate).toProperty("birthDate")
        .map(employed).toProperty("employed")
        .map(occupation).toProperty("occupation")
        .map(addressId).toProperty("addressId")
    );
}

default int insertMultiple(PersonRecord...records) {
    return insertMultiple(Arrays.asList(records));
}

default int insertMultiple(Collection<PersonRecord> records) {
    return MyBatis3Utils.insertMultiple(this::insertMultiple, records, person, c ->
        c.map(id).toProperty("id")
        .map(firstName).toProperty("firstName")
        .map(lastName).toProperty("lastName")
        .map(birthDate).toProperty("birthDate")
        .map(employed).toProperty("employed")
        .map(occupation).toProperty("occupation")
        .map(addressId).toProperty("addressId")
    );
}
```

In the mapper, only the column mappings need to be specified and no other boilerplate code is needed.

## Select Method Support

The goal of select method support is to enable the creation of methods that execute a select statement allowing a user to specify a where clause and/or order by clause at runtime, but abstracting away all other details.

To use this support, we envision creating several methods on a MyBatis mapper interface. The first two methods are the standard MyBatis Dynamic SQL method that will execute a select:

```java
@SelectProvider(type=SqlProviderAdapter.class, method="select")
@Results(id="PersonResult", value= {
        @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
        @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
        @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
        @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR),
        @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
})
List<PersonRecord> selectMany(SelectStatementProvider selectStatement);
    
@SelectProvider(type=SqlProviderAdapter.class, method="select")
@ResultMap("PersonResult")
Optional<PersonRecord> selectOne(SelectStatementProvider selectStatement);
```

These two methods are standard methods for MyBatis Dynamic SQL. They execute a select and return either a list of records, or a single record.

We also envision creating a static field for a reusable list of columns for a select statement:

```java
BasicColumn[] selectList =
    BasicColumn.columnList(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId);
```

The `selectOne` method can be used to implement a generalized select one method:

```java
default Optional<PersonRecord> selectOne(SelectDSLCompleter completer) {
    return MyBatis3Utils.selectOne(this::selectOne, selectList, person, completer);
}
```

This method shows the use of `SelectDSLCompleter` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause.

The general `selectOne` method can be used to implement a `selectByPrimaryKey` method:

```java
default Optional<PersonRecord> selectByPrimaryKey(Integer id_) {
    return selectOne(c ->
        c.where(id, isEqualTo(id_))
    );
}
```

The `selectMany` method can be used to implement generalized select methods where a user can specify a where clause and/or an order by clause. Typically we recommend two of these methods - for select, and select distinct: 

```java
default List<PersonRecord> select(SelectDSLCompleter completer) {
    return MyBatis3Utils.selectList(this::selectMany, selectList, person, completer);
}
    
default List<PersonRecord> selectDistinct(SelectDSLCompleter completer) {
    return MyBatis3Utils.selectDistinct(this::selectMany, selectList, person, completer);
}
```

These methods show the use of `MyBatis3SelectListHelper` which is a specialization of a `java.util.Function` that will allow a user to supply a where clause and/or an order by clause.

Clients can use the methods as follows:

```java
List<PersonRecord> rows = mapper.select(c ->
        c.where(id, isEqualTo(1))
        .or(occupation, isNull()));
```

There are utility methods that will select all rows in a table:

```java
List<PersonRecord> rows =
    mapper.selectByExample(SelectDSLCompleter.allRows());
```

The following query will select all rows in a specified order:

```java
List<PersonRecord> rows =
    mapper.selectByExample(SelectDSLCompleter.allRowsOrderedBy(lastName, firstName));
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
default int update(UpdateDSLCompleter completer) {
    return MyBatis3Utils.update(this::update, person, completer);
}
```

This method shows the use of `UpdateDSLCompleter` which is a specialization of a `java.util.Function` that will allow a user to supply values and a where clause. Clients can use the method as follows:

```java
int rows = mapper.update(c ->
    c.set(occupation).equalTo("Programmer")
    .where(id, isEqualTo(100)));
```

All rows in a table can be updated by simply omitting the where clause:

```java
int rows = mapper.update(c ->
    c.set(occupation).equalTo("Programmer"));
```

It is also possible to write a utility method that will set values. For example:

```java
static UpdateDSL<UpdateModel> setSelective(PersonRecord record,
        UpdateDSL<UpdateModel> dsl) {
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
