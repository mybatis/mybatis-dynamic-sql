# Database Object Representation
MyBatis Dynamic SQL works with Java objects that represent relational tables or views.

## Table or View Representation

The class `org.mybatis.dynamic.sql.SqlTable` is used to represent a table or view in a database. An `SqlTable` holds a
name, and a collection of `SqlColumn` objects that represent the columns in a table or view. A subclass of `SqlTable` -
`AliasableSqlTable` should be used in cases where you want to specify a table alias that should be used in all cases,
or if you need to change the table name at runtime.

A table or view name in SQL has three parts:

1. The catalog - which is optional and is rarely used outside of Microsoft SQL Server. If unspecified the default
   catalog will be used - and many databases only have one catalog
2. The schema - which is optional but is very often specified. If unspecified, the default schema will be used
3. The table name - which is required

Typical examples of names are as follows:

- `"dbo..bar"` - a name with a catalog (dbo) and a table name (bar). This is typical for SQL Server
- `"foo.bar"` - a name with a schema (foo) and a table name (bar). This is typical in many databases when you want to
  access tables that are not in the default schema
- `"bar"` - a name with just a table name (bar). This will access a table or view in the default catalog and schema for
  a connection

In MyBatis Dynamic SQL, the full name of the table should be supplied on the constructor of the table object.
If a table name needs to change at runtime (say for sharding support), then use the `withName` method on
`AliasableSqlTable` to create an instance with the new name.

We recommend using the base class `AliasableSqlTable` in all cases as it provides the most flexibility. The
`SqlTable` class remains in the library for compatibility with older code only.

For example:

```java
import org.mybatis.dynamic.sql.AliasableSqlTable;

public class MyTable extends AliasableSqlTable<MyTable> {
    public MyTable() {
        super("MyTable", MyTable::new);
    }
}
```

Or

```java
public class MyTable extends AliasableSqlTable<MyTable> {
    public MyTable() {
        super("MySchema.MyTable", MyTable::new);
    }
}
```

You can change a table name:

```java
public class MyTable extends AliasableSqlTable<MyTable> {
    public MyTable() {
        super("Schema1.MyTable", MyTable::new);
    }
}

MyTable schema1Table = new MyTable();
MyTable schema2Table = schema1Table.withName("Schema2.MyTable");
```

## Aliased Tables

In join queries, it is usually a good practice to specify table aliases. The `select` statement includes
support for specifying table aliases in each query in a way that looks like natural SQL. For example:

```java
    SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
            .from(orderMaster, "om")
            .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
            .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
            .where(orderMaster.orderId, isEqualTo(2))
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

In a query like this, the library will automatically append the table alias to the column name when the query is rendered.
Internally, the alias for a column is determined by looking up the associated table in a HashMap maintained within the
query model. If you do not specify a table alias, the library will automatically append the table name in join queries.

Unfortunately, this strategy fails for self-joins. It can also get confusing when there are sub-queries. Imagine a
query like this:

```java
    SelectStatementProvider selectStatement = select(user.userId, user.userName, user.parentId)
            .from(user, "u1")
            .join(user, "u2").on(user.userId, equalTo(user.parentId))
            .where(user.userId, isEqualTo(4))
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

In this query it is not clear which instance of the `user` table is used for each column, and there will only be entry in the
HashMap for the `user` table - so only one of the aliases specified in the select statement will be in effect.
There are two ways to deal with this problem.

The first is to simply create another instance of the User SqlTable object. With this method it is very clear which column
belongs to which instance of the table and the library can easily calculate aliases:

```java
    User user1 = new User();
    User user2 = new User();
    SelectStatementProvider selectStatement = select(user1.userId, user1.userName, user1.parentId)
            .from(user1, "u1")
            .join(user2, "u2").on(user1.userId, equalTo(user2.parentId))
            .where(user2.userId, isEqualTo(4))
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

Starting with version 1.3.1, there is new method where the alias can be specified in the table object itself. This allows
you to move the aliases out of the `select` statement.

```java
    User user1 = user.withAlias("u1");
    User user2 = user.withAlias("u2");

    SelectStatementProvider selectStatement = select(user1.userId, user1.userName, user1.parentId)
            .from(user1)
            .join(user2).on(user1.userId, equalTo(user2.parentId))
            .where(user2.userId, isEqualTo(4))
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

To enable this support, your table objects should extend `org.mybatis.dynamic.sql.AliasableSqlTable` rather than
`org.mybatis.dynamic.sql.SqlTable` as follows:

```java
    public static final class User extends AliasableSqlTable<User> {
        public final SqlColumn<Integer> userId = column("user_id", JDBCType.INTEGER);
        public final SqlColumn<String> userName = column("user_name", JDBCType.VARCHAR);
        public final SqlColumn<Integer> parentId = column("parent_id", JDBCType.INTEGER);

        public User() {
            super("User", User::new);
        }
    }
```

If you use an aliased table object, and also specify an alias in the `select` statement, the alias from the `select`
statement will override the alias in the table object.

## Column Representation

The class `org.mybatis.dynamic.sql.SqlColumn` is used to represent a column in a table or view. An `SqlColumn` is always
associated with a `SqlTable`. In its most basic form, the `SqlColumn` class holds a name and a reference to the
`SqlTable` it is associated with. The table reference is required so that table aliases can be applied to columns in the
rendering phase.

The `SqlColumn` will be rendered in SQL based on the `RenderingStrategy` applied to the SQL statement. Typically the
rendering strategy generates a string that represents a parameter marker in whatever SQL engine you are using. For
example, MyBatis3 parameter markers are formatted as "#{some_attribute}". By default, all columns are rendered with the
same strategy. The library supplies rendering strategies that are appropriate for several SQL execution engines
including MyBatis3 and Spring JDBC template.

In some cases it is necessary to override the rendering strategy for a particular column - so the `SqlColumn` class
supports specifying a rendering strategy for a column that will override the rendering strategy applied to a statement.
A good example of this use case is with PostgreSQL. In that database it is required to add the string "::jsonb" to a
prepared statement parameter marker when inserting or updating JSON fields, but not for other fields. A column based
rendering strategy enables this.

The `SqlColumn` class has additional optional attributes that are useful for SQL rendering - especially in MyBatis3.
These include:

* The `java.sql.JDBCType` of the column. This will be rendered into the MyBatis3 compatible parameter marker - which
  helps with picking type handlers and also inserting or updating null capable fields
* A String containing a type handler - either a type handler alias or the fully qualified type of a type handler. This
  will be rendered into the MyBatis3 compatible parameter marker

If you are not using MyBatis3, then you do not need to specify the JDBC Type or type handler as those attributes are
ignored by other rendering strategies.

Finally, the `SqlColumn` class has methods to designate a column alias or sort order for use in different SQL
statements.

We recommend a usage pattern for creating table and column objects that provides quite a bit of flexibility for usage.
See the [Quick Start](quickStart.html) page for a complete example.
