# MyBatis Dynamic SQL Quick Start

Working with MyBatis Dynamic SQL requires the following steps:

1. Create table and column objects
2. (For MyBatis3) Create mappers (XML or Java Based)
3. Write and use SQL

For the purposes of this discussion, we will show using the library to perform CRUD operations on this table:

```sql
create table SimpleTable (
   id int not null,
   first_name varchar(30) not null,
   last_name varchar(30) not null,
   birth_date date not null, 
   employed varchar(3) not null,
   occupation varchar(30) null,
   primary key(id)
);
```

## Defining Tables and Columns

The class `org.mybatis.dynamic.sql.SqlTable` is used to define a table. A table definition includes
the actual name of the table (including schema or catalog if appropriate). A table alias can be applied in a
select statement if desired.  Your table should be defined by extending the `SqlTable` class.

The class `org.mybatis.dynamic.sql.SqlColumn` is used to define columns for use in the library.
SqlColumns should be created using the builder methods in SqlTable.
A column definition includes:

1. The Java type
2. The actual column name (an alias can be applied in a select statement)
3. The JDBC type
4. (optional) The name of a type handler to use in MyBatis if the default type handler is not desired

We suggest the following usage pattern to give maximum flexibility.  This pattern will allow you to use your
table and columns in a "qualified" or "un-qualified" manner that looks like natural SQL. For example, in the
following a column could be referred to as `firstName` or `simpleTable.firstName`.

```java
package examples.simple;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class SimpleTableDynamicSqlSupport {
    public static final SimpleTable simpleTable = new SimpleTable();
    public static final SqlColumn<Integer> id = simpleTable.id;
    public static final SqlColumn<String> firstName = simpleTable.firstName;
    public static final SqlColumn<String> lastName = simpleTable.lastName;
    public static final SqlColumn<Date> birthDate = simpleTable.birthDate;
    public static final SqlColumn<Boolean> employed = simpleTable.employed;
    public static final SqlColumn<String> occupation = simpleTable.occupation;

    public static final class SimpleTable extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR);
        public final SqlColumn<String> lastName = column("last_name", JDBCType.VARCHAR);
        public final SqlColumn<Date> birthDate = column("birth_date", JDBCType.DATE);
        public final SqlColumn<Boolean> employed = column("employed", JDBCType.VARCHAR, "examples.simple.YesNoTypeHandler");
        public final SqlColumn<String> occupation = column("occupation", JDBCType.VARCHAR);

        public SimpleTable() {
            super("SimpleTable");
        }
    }
}
```

## Creating MyBatis3 Mappers
The library will create classes that will be used as input to a MyBatis mapper.  These classes include the generated SQL, as well as a parameter set that will match the generated SQL.  Both are required by MyBatis.  It is intended that these objects be the one and only parameter to a MyBatis mapper method.

The library can be used with both XML and annotated mappers, but we recommend using MyBatis' annotated mapper support in all cases.  The only case where XML is required is when you code a JOIN statement - in that case you will need to define your result map in XML due to limitations of the MyBatis annotations in supporting joins.

For example, a mapper might look like this:

```java
package examples.simple;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

@Mapper
public interface SimpleTableAnnotatedMapper {

    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    int insert(InsertStatementProvider<SimpleTableRecord> insertStatement);

    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

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

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SimpleTableResult")
    SimpleTableRecord selectOne(SelectStatementProvider selectStatement);

    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);
}
```

## Executing SQL with MyBatis3
In a DAO or service class, you can use the generated statement as input to your mapper methods.  Here's
an example from `examples.simple.SimpleTableAnnotatedMapperTest`:

```java
    @Test
    public void testSelectByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            
            SelectStatementProvider selectStatement = select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
                    .from(simpleTable)
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<SimpleTableRecord> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(3);
        }
    }
```
