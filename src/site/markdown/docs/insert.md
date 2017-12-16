# Insert Statements
The library will generate a variety of INSERT statements:

1. An insert for a single record
2. An insert for a batch of records
3. An insert with a select statement 

## Single Record Insert
A single record insert is a statement that inserts a single record into a table.  This statement is configured differently than other statements in the library so that MyBatis' support for generated keys will work properly.  To use the statement, you must first create an object that will map to the database row, then map object attributes to fields in the database.  For example:

```java
...
    SimpleTableRecord record = new SimpleTableRecord();
    record.setId(100);
    record.setFirstName("Joe");
    record.setLastName("Jones");
    record.setBirthDate(new Date());
    record.setEmployed(true);
    record.setOccupation("Developer");

    InsertStatementProvider<SimpleTableRecord> insertStatement = insert(record)
            .into(simpleTable)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .map(birthDate).toProperty("birthDate")
            .map(employed).toProperty("employed")
            .map(occupation).toProperty("occupation")
            .build()
            .render(RenderingStrategy.MYBATIS3);

    int rows = mapper.insert(insertStatement);
...
```

Notice the `map` method.  It is used to map a database column to an attribute of the record to insert.  There are several different mappings available:

1. `map(column).toNull()` will insert a null into a column
2. `map(column).toConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement exactly as entered
3. `map(column).toStringConstant(constant_value)` will insert a constant into a column.  The constant_value will be written into the generated insert statement surrounded by single quote marks (as an SQL String)
4. `map(column).toProperty(property)` will insert a value from the record into a column.  The value of the property will be bound to the SQL statement as a prepared statement parameter
5. `map(column).toPropertyWhenPresent(property, Supplier<?> valueSupplier)` will insert a value from the record into a column if the value is non-null.  The value of the property will be bound to the SQL statement as a prepared statement parameter.  This is used to generate a "selective" insert as defined in MyBatis Generator.

### Annotated Mapper for Single Record Insert Statements
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

### XML Mapper for Single Record Insert Statements
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
  </delete>
```

### Generated Values
MyBatis supports returning generated values from a single record insert, or a batch insert.  In either case, it is simply a matter of configuring the insert mapper method appropriately.  For example, to retrieve the value of a calculated column configure your mapper method like this:

```java
...
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true, keyProperty="record.fullName")
    int insert(InsertStatementProvider<GeneratedAlwaysRecord> insertStatement);
...
```

The important thing is that the `keyProperty` is set correctly.  It should always be in the form `record.<attribute>` where `<attribute>` is the attribute of the record class that should be updated with the generated value.

## Batch Insert Support
A batch insert is a collection of statements that can be used to execute a JDBC batch.  A batch is the preferred method of doing bulk inserts with JDBC.  The basic idea is that you configure the connection for a batch insert, then execute the same statement multiple times, with different values for each inserted record.  MyBatis has a nice abstraction of JDBC batches that works well with statements generated from this library.  A batch insert looks like this:

```java
...
    SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
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
                .render(RenderingStrategy.MYBATIS3);

        batchInsert.insertStatements().stream().forEach(mapper::insert);

        session.commit();
    } finally {
        session.close();
    }
...
```

It is important to open a MyBatis session by setting the executor type to BATCH.  The records are inserted on the commit.  You can call commit multiple times if you want to do intermediate commits.

Notice that the same mapper method that is used to insert a single record is now executed multiple times.  The `map` methods are the same with the exception that the `toPropertyWhenPresent` mapping is not supported for batch inserts. 

## Insert with Select
An insert select is an SQL insert statement the inserts the results of a select.  For example:

```java
    InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
            .withColumnList(id, animalName, bodyWeight, brainWeight)
            .withSelectStatement(
                select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isLessThan(22)))
            .build()
            .render(RenderingStrategy.MYBATIS3);

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
  </delete>
```
