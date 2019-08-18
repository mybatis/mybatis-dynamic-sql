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

## Executing Select Statements
The Spring Named Parameter JDBC template expects an SQL statement with parameter markers in the Spring format, and a set of matched parameters.  MyBatis Dynamic SQL will generate both.  The parameters returned from the generated SQL statement can be wrapped in a Spring `MapSqlParameterSource`.  Spring also expects you to provide a row mapper for creating the returned objects.  The following code shows a complete example:

```java
    NamedParameterJdbcTemplate template = getTemplate();

    SelectStatementProvider selectStatement = select(id, firstName, lastName, fullName)
            .from(generatedAlways)
            .where(id, isGreaterThan(3))
            .orderBy(id.descending())
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
    SqlParameterSource namedParameters = new MapSqlParameterSource(selectStatement.getParameters());
    List<GeneratedAlwaysRecord> records = template.query(selectStatement.getSelectStatement(), namedParameters,
            new RowMapper<GeneratedAlwaysRecord>(){
                @Override
                public GeneratedAlwaysRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
                    record.setId(rs.getInt(1));
                    record.setFirstName(rs.getString(2));
                    record.setLastName(rs.getString(3));
                    record.setFullName(rs.getString(4));
                    return record;
                }
            });
```

## Executing Insert Statements
Insert statements are a bit different - MyBatis Dynamic SQL generates a properly formatted SQL string for Spring, but instead of a map of parameters, the parameter mappings are created for the inserted record itself.  So the parameters for the Spring template are created by a `BeanPropertySqlParameterSource`.  Generated keys in Spring are supported with a `GeneratedKeyHolder`.  The following is a complete example:

```java
    NamedParameterJdbcTemplate template = getTemplate();

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

## Executing Batch Inserts
Batch insert support in Spring is a bit different than batch support in MyBatis3 and Spring does not support returning generated keys from a batch insert.  The following is a complete example of a batch insert (note the use of `SqlParameterSourceUtils` to create an array of parameter sources from an array of input records):

```java
    NamedParameterJdbcTemplate template = getTemplate();

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

## Executing Delete and Update Statements
Updates and deletes use the `MapSqlParameterSource` as with select statements, but use the `update` method in the template.  For example:

```java
    NamedParameterJdbcTemplate template = getTemplate();

    UpdateStatementProvider updateStatement = update(generatedAlways)
            .set(firstName).equalToStringConstant("Rob")
            .where(id,  isIn(1, 5, 22))
            .build()
            .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
    SqlParameterSource parameterSource = new MapSqlParameterSource(updateStatement.getParameters());
        
    int rows = template.update(updateStatement.getUpdateStatement(), parameterSource);
```
