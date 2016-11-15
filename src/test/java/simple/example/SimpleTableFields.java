package simple.example;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.qbe.field.Field;

public interface SimpleTableFields {
    Field<Integer> id = Field.of("id", JDBCType.INTEGER).withAlias("a");
    Field<String> firstName = Field.of("first_name", JDBCType.VARCHAR).withAlias("a");
    Field<String> lastName = Field.of("last_name", JDBCType.VARCHAR).withAlias("a");
    Field<Date> birthDate = Field.of("birth_date", JDBCType.DATE).withAlias("a");
    Field<String> occupation = Field.of("occupation", JDBCType.VARCHAR).withAlias("a");
}
