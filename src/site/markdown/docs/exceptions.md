# Exceptions Thrown by the Library

The library will throw runtime exceptions in a variety of cases - most often when invalid SQL is detected.

All exceptions are derived from `org.mybatis.dynamic.sql.exception.DynamicSqlException` which is, in turn,
derived from `java.lang.RuntimeException`.

The most important exceptions to think about are `InvalidSQLException` and `NonRenderingWhereClauseException`. We
provide details about those exceptions below.

## Invalid SQL Detection

The library makes an effort to prevent the generation of invalid SQL. If invalid SQL is detected, the library will
throw `InvalidSQLException` or one of it's derived exceptions. Invalid SQL can happen in different ways:

1. Misuse of the DSL. For example, the DSL allows you to build an update statement with no "set" clauses.
   Even though technically allowed by the DSL, this would produce invalid SQL.
2. Misuse of the Kotlin DSL. The Kotlin DSL provides a lot of flexibility for building statements and looks very close
   to native SQL, but that flexibility can be misused. For example, the Kotlin DSL would allow you to write an insert
   statement without an "into" clause.
3. More common is a case when using the optional mappings in an insert or update statement. It is possible
   that all mappings would fail to render which would produce invalid SQL. For example, in a general insert statement
   you could specify many "set column to value when present" mappings that all had null values. All mappings would fail
   to render in that case which would cause invalid SQL.

All of these exceptions can be avoided through proper use of the DSL and validation of input values.

## Non Rendering Where Clauses

Most conditions in a where clause provide optionality - they have `filter` methods that can cause the condition to be
dropped from the where clause. If all the conditions in a where clause fail to render, then the where clause itself is
dropped from the rendered SQL. This can be dangerous in that it can cause a statement to be generated that affects all
rows in a table. For example, all rows could be deleted. As of version 1.4.1, the library will throw a 
`NonRenderingWhereClauseException` in this case out of an abundance of caution. This behavior can be overridden
through either global configuration, or by configuring individual statements to allow for where clauses to be dropped.

The important idea is that there are legitimate cases when it is reasonable to allow a where clause to not render, but
the decision to allow that should be very intentional. See the "Configuration of the Library" page for further details.

The exception will only be thrown if a where clause is coded but fails to render. If you do not code a where clause in
a statement, then we assume that you intend for all rows to be affected.

## Exception Details

Details of the different exceptions follows:

| Exception                                                            | Causes                                                                                                                                                                                                                                                                            |
|----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `org.mybatis.dynamic.sql.exception.DuplicateTableAliasException`     | Thrown if you attempt to join more than one table with the same alias in a select statement                                                                                                                                                                                       |
| `org.mybatis.dynamic.sql.exception.DynamicSQLException`              | Thrown when other more specific exceptions are not appropriate. One example is when reading a configuration property file causes an IOException. This is a rare occurrence.                                                                                                       |
| `org.mybatis.dynamic.sql.exception.InvalidSQLException`              | Thrown if invalid SQL is detected. The most common causes are when all the optional column mappings in an insert or update statement fail to render.                                                                                                                              |
| `org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException` | Thrown if all conditions in a where clause fail to render - which will cause the where clause to be dropped from the rendered SQL. This could cause a statement to inadvertently affect all rows in a table. This behavior can be changed with global or statement configuration. |
| `org.mybatis.dynamic.sql.util.kotlin.KInvalidSqlException`           | Thrown if invalid SQL is detected when using the Kotlin DSL. This exception is for specific misuses of the Kotlin DSL. It is derived from `InvalidSQLException` which can also occur when using the Kotlin DSL.                                                                   |
