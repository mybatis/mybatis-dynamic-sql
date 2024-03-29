# Insert Statements
The library will generate a variety of INSERT statements:

1. An insert for a single row
1. An insert for multiple rows with a single statement
1. An insert for multiple rows with a JDBC batch
1. A general insert statement
1. An insert with a select statement

## Single Row Insert
A single record insert is a statement that inserts a single record into a table.  This statement is configured differently than other statements in the library so that MyBatis' support for generated keys will work properly.  To use the statement, you must first create an object that will map to the database row, then map object attributes to fields in the database.  For example:

```java
...
    SimpleTableRecord row = new SimpleTableRecord();
    row.setId(100);
    row.setFirstName("Joe");
    row.setLastName("Jones");
    row.setBirthDate(new Date());
    row.setEmployed(true);
    row.setOccupation("Developer");

    InsertStatementProvider<SimpleTableRecord> insertStatement = insert(row)
            .into(simpleTable)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .map(birthDate).toProperty("birthDate")
            .map(employed).toProperty("employed")
            .map(occupation).toProperty("occupation")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    int rows = mapper.insert(insertStatement);
...
```

Notice the `map` method.  It is used to map a database column to an attribute of the record to insert.  There are several different mappings available:

1. `map(column).toNull()` will insert a null into a column
2. `map(column).toConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement exactly as entered
3. `map(column).toStringConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement surrounded by single quote marks (as an SQL String)
4. `map(column).toProperty(property)` will insert a value from the record into a column.  The value of the property will be bound to the SQL statement as a prepared statement parameter
5. `map(column).toPropertyWhenPresent(property, Supplier<?> valueSupplier)` will insert a value from the record into a column if the value is non-null.  The value of the property will be bound to the SQL statement as a prepared statement parameter.  This is used to generate a "selective" insert as defined in MyBatis Generator.
6. `map(column).toRow()` will insert the record itself into a column. This is appropriate when the "record" is a simple class like Integer or String.

### Annotated Mapper for Single Row Insert Statements
The InsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the insert method should look like this (with @Options added for generated values if necessary):

```java
import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    int insert(InsertStatementProvider<SimpleTableRecord> insertStatement);
...

```

### XML Mapper for Single Row Insert Statements
We do not recommend using an XML mapper for insert statements, but if you want to do so the InsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the insert method should look like this in the Java interface:

```java
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;

...
    int insert(InsertStatementProvider<SimpleTableRecord> insertStatement);
...

```

The XML element should look like this (with attributes added for generated values if necessary):

```xml
  <insert id="insert">
    ${insertStatement}
  </insert>
```

### Generated Values
MyBatis supports returning generated values from a single row insert, or a batch insert.  In either case, it is simply a matter of configuring the insert mapper method appropriately.  For example, to retrieve the value of a calculated column configure your mapper method like this:

```java
...
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true, keyProperty="row.fullName")
    int insert(InsertStatementProvider<GeneratedAlwaysRecord> insertStatement);
...
```

The important thing is that the `keyProperty` is set correctly.  It should always be in the form `row.<attribute>` where `<attribute>` is the attribute of the record class that should be updated with the generated value.

## Multiple Row Insert Support
A multiple row insert is a single insert statement that inserts multiple rows into a table. This can be a convenient way to insert a few rows into a table, but it has some limitations:

1. Since it is a single SQL statement, you could generate quite a lot of prepared statement parameters. For example, suppose you wanted to insert 1000 records into a table, and each record had 5 fields. With a multiple row insert you would generate a SQL statement with 5000 parameters. There are limits to the number of parameters allowed in a JDBC prepared statement - and this kind of insert could easily exceed those limits. If you want to insert many records, you should probably use a JDBC batch insert instead (see below)
1. The performance of a giant insert statement may be less than you expect. If you have many records to insert, it will almost always be more efficient to use a JDBC batch insert (see below). With a batch insert, the JDBC driver can do some optimization that is not possible with a single large statement
1. Retrieving generated values with multiple row inserts can be a challenge. MyBatis currently has some limitations related to retrieving generated keys in multiple row inserts that require special considerations (see below)

Nevertheless, there are use cases for a multiple row insert - especially when you just want to insert a few records in a table and don't need to retrieve generated keys. In those situations, a multiple row insert will be an easy solution.

A multiple row insert statement looks like this:

```java
    try (SqlSession session = sqlSessionFactory.openSession()) {
        GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
        List<GeneratedAlwaysRecord> records = getRecordsToInsert(); // not shown

        MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiRowInsert = insertMultiple(records)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        int rows = mapper.insertMultiple(multiRowInsert);
    }
```

### Annotated Mapper for Multiple Row Insert Statements
The MultiRowInsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the insert method should look like this:

```java
import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultiple")
    int insertMultiple(MultiRowInsertStatementProvider<SimpleTableRecord> insertStatement);
...

```

### XML Mapper for Multiple Row Insert Statements
We do not recommend using an XML mapper for insert statements, but if you want to do so the MultiRowInsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the insert method should look like this in the Java interface:

```java
import org.mybatis.dynamic.sql.insert.render.MultiInsertStatementProvider;

...
    int insertMultiple(MultiRowInsertStatementProvider<SimpleTableRecord> insertStatement);
...

```

The XML element should look like this:

```xml
  <insert id="insertMultiple">
    ${insertStatement}
  </insert>
```

### Generated Values
MyBatis supports returning generated values from a multiple row insert statement with some limitations. The main
limitation is that MyBatis does not support nested lists in parameter objects. Unfortunately, the
`MultiRowInsertStatementProvider` relies on a nested List. It is likely this limitation in MyBatis will be removed at
some point in the future, so stay tuned.

Nevertheless, you can configure a mapper that will work with the `MultiRowInsertStatementProvider` as created by this
library. The main idea is to decompose the statement from the parameter map and send them as separate parameters to the
MyBatis mapper. For example:

```java
...
    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultipleWithGeneratedKeys")
    @Options(useGeneratedKeys=true, keyProperty="records.fullName")
    int insertMultipleWithGeneratedKeys(String insertStatement, @Param("records") List<GeneratedAlwaysRecord> records);

    default int insertMultipleWithGeneratedKeys(MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiInsert) {
        return insertMultipleWithGeneratedKeys(multiInsert.getInsertStatement(), multiInsert.getRecords());
    }
...
```

The first method above shows the actual MyBatis mapper method. Note the use of the `@Options` annotation to specify
that we expect generated values. Further, note that the `keyProperty` is set to `records.fullName` - in this case,
`fullName` is a property of the objects in the `records` List. The library supplied adapter method will simply
return the `insertStatement` as supplied in the method call. The adapter method requires that there be one, and only
one, String parameter in the method call, and it assumes that this one String parameter is the SQL insert statement.
The parameter can have any name and can be specified in any position in the method's parameter list.
The `@Param` annotation is not required for the insert statement. However, it may be specified if you so desire.

The second method above decomposes the `MultiRowInsertStatementProvider` and calls the first method.

## Batch Insert Support
A batch insert is a collection of statements that can be used to execute a JDBC batch.  A batch is the preferred method of doing bulk inserts with JDBC.  The basic idea is that you configure the connection for a batch insert, then execute the same statement multiple times, with different values for each inserted record.  MyBatis has a nice abstraction of JDBC batches that works well with statements generated from this library.  A batch insert looks like this:

```java
...
    try(SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
        SimpleTableMapper mapper = session.getMapper(SimpleTableMapper.class);
        List<SimpleTableRecord> records = getRecordsToInsert(); // not shown

        BatchInsert<SimpleTableRecord> batchInsert = insert(records)
                .into(simpleTable)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        batchInsert.insertStatements().forEach(mapper::insert);

        session.commit();
    }
...
```

It is important to open a MyBatis session by setting the executor type to BATCH.  The records are inserted on the commit.  You can call commit multiple times if you want to do intermediate commits.

Notice that the same mapper method that is used to insert a single record is now executed multiple times.  The `map` methods are the same with the exception that the `toPropertyWhenPresent` mapping is not supported for batch inserts.

## General Insert Statement
A general insert is used to build arbitrary insert statements. The general insert does not require a separate record object to hold values for the statement - any value can be passed into the statement. This version of the insert is not convenient for retrieving generated keys with MyBatis - for that use case we recommend the "single record insert". However the general insert is perfectly acceptable for Spring JDBC template or MyBatis inserts that do not return generated keys. For example

```java
    GeneralInsertStatementProvider insertStatement = insertInto(animalData)
            .set(id).toValue(101)
            .set(animalName).toStringConstant("Fred")
            .set(brainWeight).toConstant("2.2")
            .set(bodyWeight).toValue(4.5)
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

Notice the `set` method.  It is used to set the value for a database column.  There are several different possibilities:

1. `set(column).toNull()` will insert a null into a column
2. `set(column).toConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement exactly as entered
3. `set(column).toStringConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement surrounded by single quote marks (as an SQL String)
4. `set(column).toValue(value)` will insert a value into a column.  The value of the property will be bound to the SQL statement as a prepared statement parameter
5. `set(column).toValueWhenPresent(property, Supplier<?> valueSupplier)` will insert a value into a column if the value is non-null.  The value of the property will be bound to the SQL statement as a prepared statement parameter.

### Annotated Mapper for General Insert Statements
The GeneralInsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the insert method should look like this:

```java
import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @InsertProvider(type=SqlProviderAdapter.class, method="generalInsert")
    int generalInsert(GeneralInsertStatementProvider insertStatement);
...

```

### XML Mapper for General Insert Statements
We do not recommend using an XML mapper for insert statements, but if you want to do so the GeneralInsertStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the insert method should look like this in the Java interface:

```java
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;

...
    int generalInsert(GeneralInsertStatementProvider insertStatement);
...

```

The XML element should look like this:

```xml
  <insert id="generalInsert">
    ${insertStatement}
  </insert>
```


## Insert with Select
An insert select is an SQL insert statement the inserts the results of a select statement.  For example:

```java
    InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
            .withColumnList(id, animalName, bodyWeight, brainWeight)
            .withSelectStatement(
                select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isLessThan(22)))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    int rows = mapper.insertSelect(insertSelectStatement);
```
The column list is optional and can be removed if the selected columns match the layout of the table.

### Annotated Mapper for Insert Select Statements
The InsertSelectStatementProvider object can be used as a parameter to a MyBatis mapper method directly.  If you
are using an annotated mapper, the insert method should look like this:

```java
import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

...
    @InsertProvider(type=SqlProviderAdapter.class, method="insertSelect")
    int insertSelect(InsertSelectStatementProvider insertSelectStatement);
...
```

Note that MyBatis does not support overloaded mapper method names, so the name of the method should be different than the single record insert in a mapper.

### XML Mapper for Insert Select Statements
We do not recommend using an XML mapper for insert statements, but if you want to do so the InsertSelectStatementProvider object can be used as a parameter to a MyBatis mapper method directly.

If you are using an XML mapper, the insert method should look like this in the Java interface:

```java
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;

...
    int insertSelect(InsertSelectStatementProvider insertSelectStatement);
...

```

The XML element should look like this:

```xml
  <insert id="insertSelect">
    ${insertStatement}
  </insert>
```
