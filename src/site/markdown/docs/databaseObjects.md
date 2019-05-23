# Database Object Representation
MyBatis Dynamic SQL works with Java objects that represent relational tables or views.

## Table or View Representation

The class `SqlTable` is used to represent a table or view in a database. An `SqlTable` holds a name, and a collection of `SqlColumn` objects that represent the columns in a table or view.

A fully qualified name has three parts:

1. The catalog - which is rarely used outside of Microsoft SQL Server
1. The schema - which is often specified, but may be left blank if you are operating on tables in the default schema
1. The name - which is required

Typical examples of fully qualified names are as follows:

- `"dbo..bar"` - a fully qualified name with a catalog (dbo) and a name (bar). This is typical for SQL Server
- `"foo.bar"` - a fully qualified name with a schema (foo) and a name (bar). This is typical in many databases when you want to access tables that are not in the default schema
- `"bar"` - a fully qualified name with just a name (bar). This will access a table or view in the default catalog and schema for a connection


In MyBatis Dynamic SQL, the fully qualified name can be specified in different ways:

1. The fully qualified table name can be a constant String
1. The fully qualified table name can be calculated at runtime based on a dynamic catalog and/or schema and a constant table name

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


### Dynamic Names
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



## Column Representation
