# Spring Support
The library supports generating SQL that is compatible with the Spring Framework's named parameter JDBC templates.

The SQL statement objects are created in exactly the same way as for MyBatis - only the rendering strategy changes.  For example:

```java
    SelectStatementProvider selectStatement = select(id, firstName, lastName, fullName)
            .from(generatedAlways)
            .where(id, isGreaterThan(3))
            .orderBy(id.descending())
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
```

The generated SQL statement providers are compatible with Spring's `NamedParameterJdbcTemplate` in all cases. The only challenge comes with presenting statement parameters to Spring in the correct manner. To make this easier, the library provides a utility class `org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions` that executes statements properly in all cases and hides the complexity of rendering statements and formatting parameters. All the examples below will show usage both with and without the utility class.

## Type Converters for Spring

Spring JDBC templates do not have the equivalent of a type handler in MyBatis3. This is generally not a problem in processing results because you can build type conversions into your row handler. If you were manually creating the parameter map that is used as input to a Spring template you could perform a type conversion there too. But when you use MyBatis Dynamic SQL, the parameters are generated by the library, so you do not have the opportunity to perform type conversions directly.

To address this issue, the library provides a parameter type converter that can be used to perform a type conversion before parameters are placed in a parameter map.

For example, suppose we want to use a `Boolean` in Java to represent the value of a flag, but in the database the corresponding field is a `CHAR` field that expects values "true" or "false". This can be accomplished by using a `ParameterTypeConverter`. First create the converter as follows:

```java
public class TrueFalseParameterConverter implements ParameterTypeConverter<Boolean, String> {
    @Override
    public String convert(Boolean source) {
        return source == null ? null : source ? "true" : "false";
    }
}
```

The type converter is compatible with Spring's existing Converter interface. Associate the type converter with a SqlColumn as follows:

```java
...
    public final SqlColumn<Boolean> employed = column("employed", JDBCType.VARCHAR)
            .withParameterTypeConverter(new TrueFalseParameterConverter());
...
```

MyBatis Dynamic SQL will now call the converter function before corresponding parameters are placed into the generated parameter map. The converter will be called in the following cases:

1. With a general insert statement when using the `set(...).toValue(...)` or `set(...).toValueWhenPresent(...)` mappings
1. With an update statement when using the `set(...).equalTo(...)` or `set(...).equalToWhenPresent(...)` mappings
1. With where clauses in any statement type that contain conditions referencing the field

## Executing Select Statements
The Spring Named Parameter JDBC template expects an SQL statement with parameter markers in the Spring format, and a set of matched parameters.  MyBatis Dynamic SQL will generate both.  The parameters returned from the generated SQL statement can be wrapped in a Spring `MapSqlParameterSource`.  Spring also expects you to provide a row mapper for creating the returned objects.

The following code shows a complete example without the utility class:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    SelectStatementProvider selectStatement = select(id, firstName, lastName, fullName)
            .from(generatedAlways)
            .where(id, isGreaterThan(3))
            .orderBy(id.descending())
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    SqlParameterSource namedParameters = new MapSqlParameterSource(selectStatement.getParameters());
    List<GeneratedAlwaysRecord> records = template.query(selectStatement.getSelectStatement(), namedParameters,
        (rs, rowNum) -> {
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(rs.getInt(1));
            record.setFirstName(rs.getString(2));
            record.setLastName(rs.getString(3));
            record.setFullName(rs.getString(4));
            return record;
        });
```

The following code shows a complete example with the utility class:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    Buildable<SelectModel> selectStatement = select(id, firstName, lastName, fullName)
            .from(generatedAlways)
            .where(id, isGreaterThan(3))
            .orderBy(id.descending());

    List<GeneratedAlwaysRecord> records = extensions.selectList(selectStatement,
        (rs, rowNum) -> {
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(rs.getInt(1));
            record.setFirstName(rs.getString(2));
            record.setLastName(rs.getString(3));
            record.setFullName(rs.getString(4));
            return record;
        });
```

The utility class also includes a `selectOne` method that returns an `Optional`. An example is shown below:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    Buildable<SelectModel> selectStatement = select(id, firstName, lastName, fullName)
            .from(generatedAlways)
            .where(id, isEqualTo(3));

    Optional<GeneratedAlwaysRecord> record = extensions.selectOne(selectStatement,
        (rs, rowNum) -> {
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(rs.getInt(1));
            record.setFirstName(rs.getString(2));
            record.setLastName(rs.getString(3));
            record.setFullName(rs.getString(4));
            return record;
        });
```

## Executing Insert Statements

The library generates several types of insert statements. See the [Insert Statements](insert.html) page for details.

Spring supports retrieval of generated keys for many types of inserts. This library has support for generated key retrieval where it is supported by Spring.

### Executing General Insert Statements
General insert statements do not require a POJO object matching a table row. Following is a complete example:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    GeneralInsertStatementProvider insertStatement = insertInto(generatedAlways)
            .set(id).toValue(100)
            .set(firstName).toValue("Bob")
            .set(lastName).toValue("Jones")
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    int rows = template.update(insertStatement.getInsertStatement(), insertStatement.getParameters());
```

If you want to retrieve generated keys for a general insert statement the steps are similar except that you must wrap the parameters in a `MapSqlParameterSource` object and use a `GeneratedKeyHolder`. Following is a complete example of this usage:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    GeneralInsertStatementProvider insertStatement = insertInto(generatedAlways)
            .set(id).toValue(100)
            .set(firstName).toValue("Bob")
            .set(lastName).toValue("Jones")
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    MapSqlParameterSource parameterSource = new MapSqlParameterSource(insertStatement.getParameters());
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
    String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
            .set(id).toValue(100)
            .set(firstName).toValue("Bob")
            .set(lastName).toValue("Jones");

    // no generated key retrieval
    int rows = extensions.generalInsert(insertStatement);

    // retrieve generated keys
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int rows = extensions.generalInsert(insertStatement, keyHolder);
```

### Executing Single Record Insert Statements
Insert record statements are a bit different - MyBatis Dynamic SQL generates a properly formatted SQL string for Spring, but instead of a map of parameters, the parameter mappings are created for the inserted record itself.  So the parameters for the Spring template are created by a `BeanPropertySqlParameterSource`.  Generated keys in Spring are supported with a `GeneratedKeyHolder`.  The following is a complete example:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");

    InsertStatementProvider<GeneratedAlwaysRecord> insertStatement = insert(record)
            .into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(insertStatement.getRecord());
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
    String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");

    Buildable<InsertModel<GeneratedAlwaysRecord>> insertStatement = insert(record)
            .into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName");

    // no generated key retrieval
    int rows = extensions.insert(insertStatement);

    // retrieve generated keys
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int rows = extensions.insert(insertStatement, keyHolder);
```

### Multi-Row Inserts
A multi-row insert is a single insert statement with multiple VALUES clauses. This can be a convenient way in insert a small number of records into a table with a single statement. Note however that a multi-row insert is not suitable for large bulk inserts as it is possible to exceed the limit of prepared statement parameters with a large number of records. For that use case, use a batch insert (see below).

With multi-row insert statements MyBatis Dynamic SQL generates a properly formatted SQL string for Spring. Instead of a map of parameters, the multiple records are stored in the generated provider object and the parameter mappings are created for the generated provider itself. The parameters for the Spring template are created by a `BeanPropertySqlParameterSource`.  Generated keys in Spring are supported with a `GeneratedKeyHolder`.  The following is a complete example:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    List<GeneratedAlwaysRecord> records = new ArrayList<>();
    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");
    records.add(record);

    record = new GeneratedAlwaysRecord();
    record.setId(101);
    record.setFirstName("Jim");
    record.setLastName("Smith");
    records.add(record);

    MultiRowInsertStatementProvider<GeneratedAlwaysRecord> insertStatement = insertMultiple(records).into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(insertStatement);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
    String firstGeneratedKey = (String) keyHolder.getKeyList().get(0).get("FULL_NAME");
    String secondGeneratedKey = (String) keyHolder.getKeyList().get(1).get("FULL_NAME");
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    List<GeneratedAlwaysRecord> records = new ArrayList<>();
    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");
    records.add(record);

    record = new GeneratedAlwaysRecord();
    record.setId(101);
    record.setFirstName("Jim");
    record.setLastName("Smith");
    records.add(record);

    Buildable<MultiRowInsertModel<GeneratedAlwaysRecord>> insertStatement = insertMultiple(records).into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName");

    // no generated key retrieval
    int rows = extensions.insertMultiple(insertStatement);

    // retrieve generated keys
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int rows = extensions.insertMultiple(insertStatement, keyHolder);
```

### Executing Batch Inserts
A JDBC batch insert is an efficient way to perform a bulk insert. It does not have the limitations of a multi-row insert and may perform better too. Spring does not support returning generated keys from a batch insert.  The following is a complete example of a batch insert (note the use of `SqlParameterSourceUtils` to create an array of parameter sources from an array of input records):

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    List<GeneratedAlwaysRecord> records = new ArrayList<>();
    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");
    records.add(record);

    record = new GeneratedAlwaysRecord();
    record.setId(101);
    record.setFirstName("Jim");
    record.setLastName("Smith");
    records.add(record);

    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(records.toArray());

    BatchInsert<GeneratedAlwaysRecord> batchInsert = insert(records)
            .into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    int[] updateCounts = template.batchUpdate(batchInsert.getInsertStatementSQL(), batch);
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    List<GeneratedAlwaysRecord> records = new ArrayList<>();
    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
    record.setId(100);
    record.setFirstName("Bob");
    record.setLastName("Jones");
    records.add(record);

    record = new GeneratedAlwaysRecord();
    record.setId(101);
    record.setFirstName("Jim");
    record.setLastName("Smith");
    records.add(record);

    Buildable<BatchInsertModel<GeneratedAlwaysRecord>> insertStatement = insertBatch(records)
            .into(generatedAlways)
            .map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName");

    int[] updateCounts = extensions.insertBatch(insertStatement);
```

## Executing Delete Statements
Delete statements use the `MapSqlParameterSource` as with select statements, but use the `update` method in the template.  For example:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    DeleteStatementProvider deleteStatement = deleteFrom(generatedAlways)
            .where(id,  isLessThan(3))
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    SqlParameterSource parameterSource = new MapSqlParameterSource(deleteStatement.getParameters());

    int rows = template.update(deleteStatement.getDeleteStatement(), parameterSource);
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    Buildable<DeleteModel> deleteStatement = deleteFrom(generatedAlways)
            .where(id,  isLessThan(3));

    int rows = extensions.delete(deleteStatement);
```

## Executing Update Statements
Update statements use the `MapSqlParameterSource` as with select statements, but use the `update` method in the template.  For example:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown

    UpdateStatementProvider updateStatement = update(generatedAlways)
            .set(firstName).equalToStringConstant("Rob")
            .where(id, isIn(1, 5, 22))
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

    SqlParameterSource parameterSource = new MapSqlParameterSource(updateStatement.getParameters());

    int rows = template.update(updateStatement.getUpdateStatement(), parameterSource);
```

This can be simplified by using the utility class as follows:

```java
    NamedParameterJdbcTemplate template = getTemplate();  // not shown
    NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

    Buildable<UpdateModel> updateStatement = update(generatedAlways)
            .set(firstName).equalToStringConstant("Rob")
            .where(id, isIn(1, 5, 22));

    int rows = extensions.update(updateStatement);
```
