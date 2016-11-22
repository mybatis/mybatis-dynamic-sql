package examples.simple;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.qbe.mybatis3.MyBatis3Field;

public interface SimpleTableFields {
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).withAlias("a");
    MyBatis3Field<String> firstName = MyBatis3Field.of("first_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<String> lastName = MyBatis3Field.of("last_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<Date> birthDate = MyBatis3Field.of("birth_date", JDBCType.DATE).withAlias("a");
    MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR).withAlias("a");
}
