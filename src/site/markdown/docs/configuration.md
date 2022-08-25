# Configuration of the Library

This page will detail the behaviors of MyBatis Dynamic SQL that can be modified.
Configuration is available with version 1.4.1 and later of the library.

The library can be configured globally - which will change the behavior for all statements - or each individual statement
can be configured. There are sensible defaults for all configuration values, so configuration is not strictly necessary.
If you want to change any of the default behaviors of the library, then the information on this page will help.

Prior to version 1.4.1 of the library, there was no configurable behavior in the library. In version 1.4.1 we changed
the default behavior of the library to throw an exception if a where clause fails to render. We did this out of an
abundance of caution because the optional conditions in a where clause could lead to a statement that affected all
rows in a table (for example, a delete statement could inadvertently delete all rows in a table). If you want the library
to function as it did before version 1.4.1 , then you can change the global configuration as shown below.

## Global Configuration

On first use the library will initialize the global configuration. The global configuration can be specified via a property
file named `mybatis-dynamic-sql.properties` in the root of the classpath. If you wish to use a different file name,
you can specify the file name as a JVM property named `mybatis-dynamic-sql.configurationFile`. Note that the global
configuration is created one time and shared for every statement in the same JVM.

The configuration file is a standard Java properties file. The possible values are detailed in the next section.

## Global Configuration Properties

| Property                       | Default | Meaning                                                                                                                                                                                                                                                                                               |
|--------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| nonRenderingWhereClauseAllowed | false   | If a where clause is specified, but fails to render, then the library will throw a `NonRenderingWhereClauseException` by default. If you set this value to true, then no exception will be thrown. This could enable statements to be rendered without where clauses that affect all rows in a table. |

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

