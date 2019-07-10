# Database Object Representation
MyBatis Dynamic SQL works with Java objects that represent relational tables or views.

## Table or View Representation

The class `org.mybatis.dynamic.sql.SqlTable` is used to represent a table or view in a database. An `SqlTable` holds a name, and a collection of `SqlColumn` objects that represent the columns in a table or view.

A table or view name in SQL has three parts:

1. The catalog - which is optional and is rarely used outside of Microsoft SQL Server. If unspecified the default catalog will be used - and many databases only have one catalog
1. The schema - which is optional but is very often specified. If unspecified the default schema will be used
1. The table name - which is required

Typical examples of names are as follows:

- `"dbo..bar"` - a name with a catalog (dbo) and a table name (bar). This is typical for SQL Server
- `"foo.bar"` - a name with a schema (foo) and a table name (bar). This is typical in many databases when you want to access tables that are not in the default schema
- `"bar"` - a name with just a table name (bar). This will access a table or view in the default catalog and schema for a connection

In MyBatis Dynamic SQL, the table or view name can be specified in different ways:

1. The name can be a constant String
1. The name can be calculated at runtime based on a catalog and/or schema supplier functions and a constant table name
1. The name can be calculated at runtime with a name supplier function

### Constant Names

Constant names are used when you use the `SqlTable` constructor with a single String argument. For example:

```java
public class MyTable extends SqlTable {
    public MyTable() {
        super("MyTable");
    }
}
```

Or

```java
public class MyTable extends SqlTable {
    public MyTable() {
        super("MySchema.MyTable");
    }
}
```

### Dynamic Catalog and/or Schema Names
MyBatis Dynamic SQL allows you to dynamically specify a catalog and/or schema. This is useful for applications where the schema may change for different users or environments, or if you are using different schemas to shard the database. Dynamic names are used when you use a `SqlTable` constructor that accepts one or more `java.util.function.Supplier` arguments.

For example, suppose you wanted to change the schema based on the value of a system property. You could write a class like this:

```java
public class SchemaSupplier {
    public static final String schema_property = "schemaToUse";

    public static Optional<String> schemaPropertyReader() {
        return Optional.ofNullable(System.getProperty(schema_property));
    }
}
```

This class has a static method `schemaPropertyReader` that will return an `Optional<String>` containing the value of a system property. You could then reference this method in the constructor of the `SqlTable` like this:

```java
public static final class User extends SqlTable {
    public User() {
        super(SchemaSupplier::schemaPropertyReader, "User");
    }
}
```

Whenever the table is referenced for rendering SQL, the name will be calculated based on the current value of the system property.

There are two constructors that can be used for dynamic names:

1. A constructor that accepts `Supplier<Optional<String>>` for the schema, and `String` for the name. This constructor assumes that the catalog is always empty or not used
1. A constructor that accepts `Supplier<Optional<String>>` for the catalog, `Supplier<Optional<String>>` for the schema, and `String` for the name

If you are using Microsoft SQL Server and want to use a dynamic catalog name and ignore the schema, then you should use the second constructor like this:

```java
public static final class User extends SqlTable {
    public User() {
        super(CatalogSupplier::catalogPropertyReader, Optional::empty, "User");
    }
}
```

The following table shows how the name is calculated in all combinations of suppliers:

Catalog Supplier Value | Schema Supplier Value | Name | Fully Qualified Name
---|---|---|---
"MyCatalog" | "MySchema" | "MyTable" | "MyCatalog.MySchema.MyTable"
&lt;empty&gt; | "MySchema" | "MyTable" | "MySchema.MyTable"
"MyCatalog" | &lt;empty&gt; | "MyTable" | "MyCatalog..MyTable"
&lt;empty&gt; | &lt;empty&gt; | "MyTable" | "MyTable"


### Fully Dynamic Names
MyBatis Dynamic SQL allows you to dynamically specify a full table name. This is useful for applications where the database is sharded with different tables representing different shards of the whole. Dynamic names are used when you use a `SqlTable` constructor that accepts a single `java.util.function.Supplier` argument.

Note that this functionality should only be used for tables that have different names, but are otherwise identical.

For example, suppose you wanted to change the name based on the value of a system property. You could write a class like this:

```java
public class NameSupplier {
    public static final String name_property = "nameToUse";

    public static String namePropertyReader() {
        return System.getProperty(name_property);
    }
}
```

This class has a static method `namePropertyReader` that will return an `String` containing the value of a system property. You could then reference this method in the constructor of the `SqlTable` like this:

```java
public static final class User extends SqlTable {
    public User() {
        super(NameSupplier::namePropertyReader);
    }
}
```

Whenever the table is referenced for rendering SQL, the name will be calculated based on the current value of the system property.



## Column Representation

The class `org.mybatis.dynamic.sql.SqlColumn` is used to represent a column in a table or view. An `SqlColumn` is always associated with a `SqlTable`. In it's most basic form, the `SqlColumn` class holds a name and a reference to the `SqlTable` it is associated with.

The `SqlColumn` class has additional optional attributes that are useful for SQL rendering - especially in MyBatis3. These include:

* The `java.sql.JDBCType` of the column. This will be rendered into the MyBatis3 compatible parameter marker - which helps with picking type handlers and also inserting or updating null capable fields
* A String containing a type handler - either a type handler alias or the fully qualified type of a type handler. This will be rendered into the MyBatis3 compatible parameter marker

If you are not using MyBatis3, then you will not need to specify the JDBC Type or type handler.

Finally, the `SqlColumn` class has methods to designate a column alias or sort order for use in different SQL statements.

We recommend a usage pattern for creating table and column objects that provides quite a bit of flexibility for usage. See the [Quick Start](quickStart.html) page for a complete example.
