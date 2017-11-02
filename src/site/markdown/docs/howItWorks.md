# How it Works

MyBatis does four main things:

1. It executes SQL in a safe way and abstracts away all the intricacies of JDBC
2. It maps parameter objects to JDBC prepared statement parameters
3. It maps JDBC result sets to objects
4. It can generate dynamic SQL with special tags in XML, or through the use of various templating engines

This library takes full advantage of the first three capabilities in MyBatis and essentialy becomes another
templating engine for generating dynamic SQL.

For example, MyBatis can execute an SQL string formatted like this:

```sql
  select id, description from some_table where id = #{id,jdbcType=INTEGER} 
```

This is standard SQL with a MyBatis twist - the parameter notation ```#{id,jdbcType=INTEGER}```
tells MyBatis to take the ```id``` property of a parameter object and set it to a JDBC prepared statement
parameter. 
