# Specialized Support for MyBatis3
Most of the examples shown on this site are for usage with MyBatis3 - even though the library does support other SQL runtimes like Spring JDBC templates. But the library does have some additional specialized support for MyBatis3 beyond what is shown in the other examples.

This support is added to the DELETE, SELECT, and UPDATE statement generators and enables the creating of reusable "by example" methods as delivered in MyBatis Generator.  These methods can provide some boilerplate code for the setup of the statement (a column list and table name for example), and allow the user to specify a where clause.

With version 1.1.3, specialized interfaces were added that can further simplify client code.

For example, it is possible to write a mapper interface like this:

```java
import static examples.simple.SimpleTableDynamicSqlSupport.*;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectByExampleHelper;

@Mapper
public interface SimpleTableMapper {
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SimpleTableResult", value= {
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectStatementProvider selectStatement);
    
    default List<SimpleTableRecord> selectByExample(MyBatis3SelectByExampleHelper<SimpleTableRecord> helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::selectMany, id, firstName, lastName, birthDate, employed, occupation)
                .from(simpleTable))
                .build()
                .execute();
    }
}
```

Notice the `selectByExample` method - it specifies the column list and table name and accepts a lambda expression that can be used to build the WHERE clause.  It also reuses the `selectMany` mapper method.

The code is used like this:

```java
    List<SimpleTableRecord> rows = mapper.selectByExample(q ->
            q.where(id, isEqualTo(1))
            .or(occupation, isNull()));
```


It is expected that MyBatis Generator will generate code that looks like this.


## Prior Support
Prior to version 1.1.3, it was also possible to write reusable methods, but they were a bit inconsistent with other helper methods.  

For example, it is possible to write a mapper interface like this:

```java
import static examples.simple.SimpleTableDynamicSqlSupport.*;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

@Mapper
public interface SimpleTableMapper {
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SimpleTableResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectStatementProvider selectStatement);
    
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<SimpleTableRecord>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable);
    }
}
```

Notice the `selectByExample` method - it specifies the column list and table name and returns the intermediate builder that can be used to finish the WHERE clause.  It also reuses the `selectMany` mapper method.  Mapper methods built using this added support all finish with an `execute` method that builds the statement and executes the mapper method.

The code is used like this:

```java
    List<SimpleTableRecord> rows = mapper.selectByExample()
            .where(id, isEqualTo(1))
            .or(occupation, isNull())
            .build()
            .execute();
```
