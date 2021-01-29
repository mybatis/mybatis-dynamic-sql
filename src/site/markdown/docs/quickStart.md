# MyBatis Dynamic SQL Quick Start

Working with MyBatis Dynamic SQL requires the following steps:

1. Create table and column objects
2. (For MyBatis3) Create mappers (XML or Java Based)
3. Write and use SQL

For the purposes of this discussion, we will show using the library to perform CRUD operations on this table:

```sql
create table Person (
    id int not null,
    first_name varchar(30) not null,
    last_name varchar(30) not null,
    birth_date date not null,
    employed varchar(3) not null,
    occupation varchar(30) null,
    address_id int not null,
    primary key(id)
);
```

We will also create a simple Java class to represent a row in the table:

```java
package examples.simple;

import java.util.Date;

public class PersonRecord {
    private Integer id;
    private String firstName;
    private LastName lastName;
    private Date birthDate;
    private Boolean employed;
    private String occupation;
    private Integer addressId;
    
    // getters and setters omitted
}
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
table and column names in a "qualified" or "un-qualified" manner that looks like natural SQL. For example, in the
following a column could be referred to as `firstName` or `person.firstName`.

```java
package examples.simple;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class PersonDynamicSqlSupport {
    public static final Person person = new Person();
    public static final SqlColumn<Integer> id = person.id;
    public static final SqlColumn<String> firstName = person.firstName;
    public static final SqlColumn<LastName> lastName = person.lastName;
    public static final SqlColumn<Date> birthDate = person.birthDate;
    public static final SqlColumn<Boolean> employed = person.employed;
    public static final SqlColumn<String> occupation = person.occupation;
    public static final SqlColumn<Integer> addressId = person.addressId;

    public static final class Person extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR);
        public final SqlColumn<LastName> lastName = column("last_name", JDBCType.VARCHAR, "examples.simple.LastNameTypeHandler");
        public final SqlColumn<Date> birthDate = column("birth_date", JDBCType.DATE);
        public final SqlColumn<Boolean> employed = column("employed", JDBCType.VARCHAR, "examples.simple.YesNoTypeHandler");
        public final SqlColumn<String> occupation = column("occupation", JDBCType.VARCHAR);
        public final SqlColumn<Integer> addressId = column("address_id", JDBCType.INTEGER);

        public Person() {
            super("Person");
        }
    }
}
```

## Creating MyBatis3 Mappers
The library will create classes that will be used as input to a MyBatis mapper.  These classes include the generated
SQL, as well as a parameter set that will match the generated SQL.  Both are required by MyBatis.  It is intended that
these objects be the one and only parameter to a MyBatis mapper method.

The library can be used with both XML and annotated mappers, but we recommend using MyBatis' annotated mapper support in
all cases.  The only case where XML is required is when you code a JOIN statement - in that case you will need to define
your result map in XML due to limitations of the MyBatis annotations in supporting joins.

For example, a mapper might look like this:

```java
package examples.simple;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

@Mapper
public interface PersonMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<PersonRecord>, CommonUpdateMapper {

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Results(id = "PersonResult", value = {
            @Result(column = "A_ID", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR, typeHandler = LastNameTypeHandler.class),
            @Result(column = "birth_date", property = "birthDate", jdbcType = JdbcType.DATE),
            @Result(column = "employed", property = "employed", jdbcType = JdbcType.VARCHAR, typeHandler = YesNoTypeHandler.class),
            @Result(column = "occupation", property = "occupation", jdbcType = JdbcType.VARCHAR),
            @Result(column = "address_id", property = "addressId", jdbcType = JdbcType.INTEGER)
    })
    List<PersonRecord> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultMap("PersonResult")
    Optional<PersonRecord> selectOne(SelectStatementProvider selectStatement);
}
```

This mapper implements full CRUD functionality for the table. The base interfaces `CommonCountMapper`,
`CommonDeleteMapper`, etc. provide insert, update, delete, and count capabilities. Only the select methods must be
written because of the custom result map.

Note that the `CommonInsertMapper` interface will not properly return the generated key if one is produced by the insert.
If you need generated key support, see the documentation page for INSERT statements for details on how to implement
such support.

## Executing SQL with MyBatis3
In a service class, you can use the generated statement as input to your mapper methods.  Here are some
examples from `examples.simple.PersonMapperTest`:

```java
@Test
void testGeneralSelect() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
        PersonMapper mapper = session.getMapper(PersonMapper.class);

        SelectStatementProvider selectStatement = select(id.as("A_ID"), firstName, lastName, birthDate, employed,
            occupation, addressId)
        .from(person)
        .where(id, isEqualTo(1))
        .or(occupation, isNull())
        .build()
        .render(RenderingStrategies.MYBATIS3);

        List<PersonRecord> rows = mapper.selectMany(selectStatement);
        assertThat(rows).hasSize(3);
    }
}

@Test
void testGeneralDelete() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
        PersonMapper mapper = session.getMapper(PersonMapper.class);

        DeleteStatementProvider deleteStatement = deleteFrom(person)
        .where(occupation, isNull())
        .build()
        .render(RenderingStrategies.MYBATIS3);

        int rows = mapper.delete(deleteStatement);
        assertThat(rows).isEqualTo(2);
    }
}
```

If you use MyBatis Generator, the generator will create several additional utility methods in a mapper like this that
will improve its usefulness. You can see a full example of the type of code created by MyBatis generator by looking
at the full example at https://github.com/mybatis/mybatis-dynamic-sql/tree/master/src/test/java/examples/simple
