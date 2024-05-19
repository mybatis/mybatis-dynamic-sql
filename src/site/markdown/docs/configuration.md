# Configuration of the Library

This page will detail the behaviors of MyBatis Dynamic SQL that can be modified.
Configuration is available with version 1.4.1 and later of the library.

The library can be configured globally - which will change the behavior for all statements - or each individual statement
can be configured. There are sensible defaults for all configuration values, so configuration is not strictly necessary.
If you want to change any of the default behaviors of the library, then the information on this page will help.

## Global Configuration

On first use the library will initialize the global configuration. The global configuration can be specified via a property
file named `mybatis-dynamic-sql.properties` in the root of the classpath. If you wish to use a different file name,
you can specify the file name as a JVM property named `mybatis-dynamic-sql.configurationFile`. Note that the global
configuration is created one time and shared for every statement in the same JVM.

The configuration file is a standard Java properties file. The possible values are detailed in the next section.

## Global Configuration Properties

| Property                           | Default | Available in Version | Meaning                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|------------------------------------|---------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| nonRenderingWhereClauseAllowed     | false   | 1.4.1+               | If a where clause is specified, but fails to render, then the library will throw a `NonRenderingWhereClauseException` by default. If you set this value to true, then no exception will be thrown. This could enable statements to be rendered without where clauses that affect all rows in a table.                                                                                                                                                          |

## Statement Configuration

If the global configuration is not acceptable for any individual statement, you can also configure the statement in the
DSL. Consider the following statement:

```java
DeleteStatementProvider deleteStatement = deleteFrom(animalData)
    .where(id, isIn(null, 22, null).filter(Objects::nonNull).filter(i -> i != 22))
    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
    .build()
    .render(RenderingStrategies.MYBATIS3);
```

In this case, the `isIn` condition has filters that will remove all values from the condition. In that case, the
condition will not render and, subsequently, the where clause will not render. This means that the generated delete
statement would delete all rows in the table. By default, the global configuration will block this statement from
rendering and throw a `NonRenderingWhereClauseException`. If for some reason you would like to allow a statement
like this to be rendered, then you can allow it as shown above with the `configureStatement` method.

The Kotlin DSL contains the same function:

```kotlin
val deleteStatement = deleteFrom(person) {
    where { id isEqualToWhenPresent null }
    configureStatement { isNonRenderingWhereClauseAllowed = true }
}
```

## Configuration Scope with Select Statements

Select statements can stand alone, or they can be embedded within other statements. For example, the library supports
writing insert statements with an embedded select, or select statements that contain other select statements for sub
queries. The select DSLs (both Java and Kotlin) appear to allow you to specify statement configuration on embedded
select statements, but this is not supported in point of fact. Statement configuration must ALWAYS be specified on the
outermost statement. Any configuration specified on embedded select statements will be ignored. We realize this could be
confusing! But we've made this decision hoping to minimize code duplication and maximize consistency.

So the best practice is to ALWAYS specify the statement configuration as the LAST call to the DSL before calling
`build`, or before ending a Kotlin lambda.

The following Kotlin code snippet shows this in action...

```kotlin
val insertStatement = insertSelect {
    into(person)
    select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
        from(person)
        where { id isGreaterThanOrEqualToWhenPresent null }
        // the following will be ignored in favor of the enclosing statement configuration...
        configureStatement { isNonRenderingWhereClauseAllowed = false }
    }
    configureStatement { isNonRenderingWhereClauseAllowed = true }
}
```

The inner `configureStatement` call will be ignored in this case, only the `configureStatement` call scoped to the
insert statement itself will be in effect.
