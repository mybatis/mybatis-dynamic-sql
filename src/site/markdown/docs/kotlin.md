# Kotlin Support for MyBatis3
MyBatis Dynamic SQL includes Kotlin extension methods that enable an SQL DSL for Kotlin. This is the recommended method of using the library in Kotlin.

The standard usage patterns for MyBatis Dynamic SQL and MyBatis3 in Java must be modified somewhat for Kotlin. Kotlin interfaces can contain both abstract and non-abstract methods (somewhat similar to Java's default methods in an interface). But using these methods in Kotlin based mapper interfaces will cause a failure with MyBatis because of the underlying Kotlin implementation.

This page will show our recommended pattern for using the MyBatis Dynamic SQL with Kotlin. The code shown on this page is from the `src/test/kotlin/examples/kotlin/canonical` directory in this repository. That directory contains a complete example of using this library with Kotlin.

All Kotlin support is available in two packages:

* `org.mybatis.dynamic.sql.util.kotlin` - contains extension methods and utilities to enable an idiomatic Kotlin DSL for MyBatis Dynamic SQL. These objects can be used for clients using any execution target (i.e. MyBatis3 or Spring JDBC Templates)
* `org.mybatis.dynamic.sql.util.kotlin.mybatis3` - contains utlities specifically to simplify MyBatis3 based clients

Using the support in these packages, it is possible to create reusable Kotlin classes, interfaces, and extension methods that mimic the code created by MyBatis Generator for Java - but code that is more idiomatic for Kotlin.

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

This object is a singleton containing the `SqlTable` and `SqlColumn` objects that map to the database table.

## Kotlin Mappers for MyBatis3
If you create a Kotlin mapper interface that includes both abstract and non-abstract methods, MyBatis will be confused and throw errors. By default Kotlin does not create Java default methods in an interface. For this reason, Kotlin mapper interfaces should only contain the actual MyBatis mapper abstract interface methods. What would normally be coded as default or static methods in a mapper interface should be coded as extension methods in Kotlin. For example, a simple MyBatis mapper could be coded like this:

```kotlin
@Mapper
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

And then extensions could be added to make a shortcut method as follows:

```kotlin
private val columnList = listOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)

fun PersonMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Person, completer)
```

The extension method shows the use of the `SelectCompleter` type alias. This is a DSL extension supplied with the library. We will detail its use below. For now see that the extension method can be used in client code as follows:

```kotlin
val rows = mapper.select {
    where(id, isLessThan(100))
    or (employed, isTrue()) {
        and (occupation, isEqualTo("Developer"))
    }
    orderBy(id)
}
```

This shows that the Kotlin support enables a more idiomatic Kotlin DSL.
 
## Count Method Support

A count query is a specialized select - it returns a single column - typically a long - and supports joins and a where clause.

Count method support enables the creation of methods that execute a count query allowing a user to specify a where clause at runtime, but abstracting away all other details.

To use this support, we envision creating two methods - one standard mapper method, and one extension method. The first method is the standard MyBatis Dynamic SQL method that will execute a select:

```kotlin
@Mapper
interface PersonMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun count(selectStatement: SelectStatementProvider): Long
}
```

This is a standard method for MyBatis Dynamic SQL that executes a query and returns a `Long`. The second method should be an extension maethod. It will reuse the abstract method and supply everything needed to build the select statement except the where clause:

```kotlin
fun PersonMapper.count(completer: CountCompleter) =
    count(this::count, Person, completer)
```

This method shows the use of `CountCompleter` which is a Kotlin typealias for a function with a receiver that will allow a user to supply a where clause. This also shows use of the Kotlin `count` method which is supplied by the library. That method will build and execute the select count statement with the supplied where clause. Clients can use the method as follows:

```kotlin
val rows = mapper.count {
    where(occupation, isNull()) {
        and(employed, isFalse())
    }
}
```

There is also an extention method that can be used to count all rows in a table:

```kotlin
val rows = mapper.count { allRows() }
```

## Delete Method Support

Delete method support enables the creation of methods that execute a delete statement allowing a user to specify a where clause at runtime, but abstracting away all other details.

To use this support, we envision creating two methods - one standard mapper method, and one extension method. The first method is the standard MyBatis Dynamic SQL method that will execute a delete:

```kotlin
@Mapper
interface PersonMapper {
    @DeleteProvider(type = SqlProviderAdapter::class, method = "delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int
}
```

This is a standard method for MyBatis Dynamic SQL that executes a delete and returns an `Int` - the number of rows deleted. The second method should be an extension method. It will reuse the abstract method and supply everything needed to build the delete statement except the where clause:

```kotlin
fun PersonMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Person, completer)
```

This method shows the use of `DeleteCompleter` which is a Kotlin typealias for a function with a receiver that will allow a user to supply a where clause. This also shows use of the Kotlin `deleteFrom` method which is supplied by the library. That method will build and execute the delete statement with the supplied where clause. Clients can use the method as follows:

```kotlin
val rows = mapper.delete {
    where(occupation, isNull())
}
```

There is an extension method that can be used to delete all rows in a table:

```kotlin
val rows = mapper.delete { allRows() }
```

## Insert Method Support

Insert method support enables the removal of some of the boilerplate code from insert methods in a mapper interfaces.

To use this support, we envision creating several methods - two standard mapper methods, and other extension methods. The standard mapper methods are standard MyBatis Dynamic SQL methods that will execute a delete:

```kotlin
@Mapper
interface PersonMapper {
    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    fun insert(insertStatement: InsertStatementProvider<PersonRecord>): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "insertMultiple")
    fun insertMultiple(insertStatement: MultiRowInsertStatementProvider<PersonRecord>): Int
}
```

These methods can be used to implement simplified insert methods with Kotlin extension methods:

```kotlin
fun PersonMapper.insert(record: PersonRecord) =
    insert(this::insert, record, Person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.insertMultiple(vararg records: PersonRecord) =
    insertMultiple(records.toList())

fun PersonMapper.insertMultiple(records: Collection<PersonRecord>) =
    insertMultiple(this::insertMultiple, records, Person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.insertSelective(record: PersonRecord) =
    insert(this::insert, record, Person) {
        map(id).toPropertyWhenPresent("id", record::id)
        map(firstName).toPropertyWhenPresent("firstName", record::firstName)
        map(lastName).toPropertyWhenPresent("lastName", record::lastName)
        map(birthDate).toPropertyWhenPresent("birthDate", record::birthDate)
        map(employed).toPropertyWhenPresent("employed", record::employed)
        map(occupation).toPropertyWhenPresent("occupation", record::occupation)
        map(addressId).toPropertyWhenPresent("addressId", record::addressId)
    }
```

Note these methods use Kotlin utility methods named `insert` and `insertMultiple`. Both methods accept a function with a receiver that will allow column mappings. The methods will build and execute insert statements.= with the supplied column mappings.

Clients use these methods as follows:

```kotlin
// single insert...
val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
val rows = mapper.insert(record)

// multiple insert...
val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)
val rows = mapper.insertMultiple(record1, record2)
```

## Select Method Support

Select method support enables the creation of methods that execute a query allowing a user to specify a where clause and/or an order by clause and/or pagination clauses at runtime, but abstracting away all other details.

To use this support, we envision creating several methods - two standard mapper methods, and other extension methods. The standard mapper methods are standard MyBatis Dynamic SQL methods that will execute a select:

```kotlin
@Mapper
interface PersonMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(id = "PersonResult", value = [
        Result(column = "A_ID", property = "id", jdbcType = JdbcType.INTEGER, id = true),
        Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
        Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR,
                typeHandler = LastNameTypeHandler::class),
        Result(column = "birth_date", property = "birthDate", jdbcType = JdbcType.DATE),
        Result(column = "employed", property = "employed", jdbcType = JdbcType.VARCHAR,
                typeHandler = YesNoTypeHandler::class),
        Result(column = "occupation", property = "occupation", jdbcType = JdbcType.VARCHAR),
        Result(column = "address_id", property = "addressId", jdbcType = JdbcType.INTEGER)])
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonRecord>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("PersonResult")
    fun selectOne(selectStatement: SelectStatementProvider): PersonRecord?
}
```

These methods can be used to create simplified select methods with Kotlin extension methods:

```kotlin
private val columnList = listOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)

fun PersonMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Person, completer)

fun PersonMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Person, completer)

fun PersonMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Person, completer)
```

These methods show the use of `SelectCompleter` which is a which is a Kotlin typealias for a function with a receiver that will allow a user to supply a where clause. The `selectMany` method can be used to implement generalized select methods where a user can specify a where clause and/or an order by clause. Typically we recommend two of these methods - for select, and select distinct. The `selectOne` method is used to create a generalized select method where a user can specify a where clause. These methods also show the use of the built in Kotlin functions `selectDistinct`, `selectList`, and `selectOne`. These functions build and execute select statements, and help to avoid platform type issues in Kotlin. They enable the Kotlin compiler to correctly infer the result type (either `PersonRecord?` or `List<PersonRecord>` in this case). 

The general `selectOne` method can also be used to implement a `selectByPrimaryKey` method:

```kotlin
fun PersonMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where(id, isEqualTo(id_))
    }
```

Clients can use the methods as follows:

```kotlin
val rows = mapper.select {
    where(firstName, isIn("Fred", "Barney"))
    orderBy(id)
    limit(3)
}
```

There is a utility methods that will select all rows in a table:

```kotlin
val rows = mapper.select { allRows() }
```

The following query will select all rows in a specified order:

```kotlin
val rows = mapper.select {
    allRows()
    orderBy(lastName, firstName)
}
```

## Update Method Support

Update method support enables the creation of methods that execute an update allowing a user to specify SET clauses and/or a WHERE clause, but abstracting away all other details.

To use this support, we envision creating several methods - one standard mapper method, and other extension methods. The standard mapper method is a standard MyBatis Dynamic SQL methods that will execute an update:

```kotlin
@Mapper
interface PersonMapper {
    @UpdateProvider(type = SqlProviderAdapter::class, method = "update")
    fun update(updateStatement: UpdateStatementProvider): Int
}
```

This is a standard method for MyBatis Dynamic SQL that executes an update and returns an `int` - the number of rows updated. The extension methods will reuse this method and supply everything needed to build the update statement except the values and the where clause:

```kotlin
fun PersonMapper.update(completer: UpdateCompleter) =
    update(this::update, Person, completer)
```

This extension method shows the use of `UpdateCompleter` which is a Kotlin typealias for a function with a receiver that will allow a user to supply values and a where clause. This also shows use of the Kotlin `update` method which is supplied by the library. That method will build and execute the update statement with the supplied values and where clause. Clients can use the method as follows:

```kotlin
val rows = mapper.update {
    set(occupation).equalTo("Programmer")
    where(id, isEqualTo(100))
}
```

All rows in a table can be updated by simply omitting the where clause:

```kotlin
val rows = mapper.update {
    set(occupation).equalTo("Programmer")
}
```

It is also possible to write a utility method that will set values. For example:

```kotlin
fun UpdateDSL<UpdateModel>.updateSelectiveColumns(record: PersonRecord) =
    apply {
        set(id).equalToWhenPresent(record::id)
        set(firstName).equalToWhenPresent(record::firstName)
        set(lastName).equalToWhenPresent(record::lastName)
        set(birthDate).equalToWhenPresent(record::birthDate)
        set(employed).equalToWhenPresent(record::employed)
        set(occupation).equalToWhenPresent(record::occupation)
        set(addressId).equalToWhenPresent(record::addressId)
    }
```

This method will selectively set values if corresponding fields in a record are non null. This method can be used as follows:

```kotlin
val rows = mapper.update {
    updateSelectiveColumns(updateRecord)
    where(id, isEqualTo(100))
}
```
## Join Support

There are extension functions that support building a reusable select method based on a join. In this way, you can create the start of the select statement (the column list and join specifications) and allow the user to supply where clauses and other parts of a select statement. For example, you could code a mapper extension method like this:

```kotlin
fun PersonWithAddressMapper.select(completer: SelectCompleter): List<PersonWithAddress> {
    val start = select(columnList).from(Person, "p") {
        join(Address, "a") {
            on(Person.addressId, equalTo(Address.id))
        }
    }
    return selectList(this::selectMany, start, completer)
}
```

This method creates the start of a select statement with a join, and accepts user input to complete the statement. This shows use of and overloaded `selectList` method that accepts the start of a select statement and a completer. Like other select methods, this method can be used as follows:

```kotlin
val records = mapper.select {
    where(id, isLessThan(100))
    limit(5)
}
```
