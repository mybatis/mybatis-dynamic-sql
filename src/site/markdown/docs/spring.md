# Spring Support
The library supports generating SQL that is compatible with the Spring Framework's named parameter JDBC templates.

The SQL statement objects are created in exactly the same way as for MyBatis - only the rendering strategy changes.  For example:

```java
    SelectStatement selectStatement = select(id.as("A_ID"), firstName, lastName, fullName)
            .from(generatedAlways, "a")
            .where(id, isGreaterThan(3))
            .orderBy(id.descending())
            .build()
            .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
```

## Executing Select Statements
TODO...

## Executing Insert Statements
TODO...

### Returning Generated Keys
TODO...

## Executing Batch Inserts
TODO...

## Executing Delete and Update Statements
TODO...
