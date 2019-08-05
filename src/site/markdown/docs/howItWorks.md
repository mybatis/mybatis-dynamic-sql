# How it Works

MyBatis does four main things:

1. It executes SQL in a safe way and abstracts away all the intricacies of JDBC
2. It maps parameter objects to JDBC prepared statement parameters
3. It maps rows in JDBC result sets to objects
4. It can generate dynamic SQL with special tags in XML, or through the use of various templating engines

This library takes full advantage of the first three capabilities in MyBatis and essentially becomes another
templating engine for generating dynamic SQL.

For example, MyBatis can execute an SQL string formatted like this:

```sql
  select id, description from table_codes where id = #{id,jdbcType=INTEGER} 
```

This is standard SQL with a MyBatis twist - the parameter notation `#{id,jdbcType=INTEGER}`
tells MyBatis to take the `id` property of a parameter object and use it as a JDBC prepared statement
parameter.

Now suppose we have two Java classes like this:

```java
public class TableCode {
  private Integer id;
  private String description;
  ... getters/setters
}

public class Parameter {
  private String sql = "select id, description from table_codes where id = #{id,jdbcType=INTEGER}";
  private Integer id;

  public Parameter(Integer id) {
    this.id = id;
  }
  
  public Integer getId() {
    return id;
  }
  
  public String getSql() {
    return sql;
  }
}
```
These classes can be used in conjunction with a MyBatis mapper like this:

```java
public interface Mapper {

  @Select({
    "${sql}"
  })
  TableCode getTableCode(Parameter parameter);
}
```

Using this mapper with MyBatis looks like this:

```java
  try(SqlSession sqlSession = sqlSessionFactory.openSession()) {
    Mapper mapper = sqlSession.getMapper(Mapper.class);
    Parameter parameter = new Parameter(2);
    TableCode tableCode = mapper.getTableCode(parameter);
    assertThat(tableCode.getId()).isEqualTo(2);
  }
```

The parameter object has a property called `sql`. That SQL string will be prepared as a JDBC prepared statement
in MyBatis. The SQL string also references a property called `id`. That property - from the same parameter object -
will be used as the value of the prepared statement parameter.

So the main idea is this - this library builds a version of the `Parameter` class shown above. The class includes
the full SQL statement to execute, formatted for MyBatis, and any parameters referenced by the statement.
There are different versions of these classes for the different types of SQL statements. But in every case, the class
is designed to be the one single parameter for a MyBatis mapper method.

## What About SQL Injection?

It is true that mappers written this way are open to SQL injection. But this is also true of using any of the
various SQL provider classes in MyBatis (`@SelectProvider`, etc.) So you must be careful that these types of mappers are not exposed to any general user input.

If you follow these practices, you will lower the risk of SQL injection:

1. Always use MyBatis annotated mappers
2. Use the `SqlProviderAdapter` utility class in conjunction with the MyBatis provider annotations (`@InsertProvider`, `@SelectProvider`, etc.)
