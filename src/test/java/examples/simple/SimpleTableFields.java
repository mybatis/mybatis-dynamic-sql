package examples.simple;

import static org.mybatis.qbe.sql.insert.render.InsertSupportShortcut.insertSelective;
import static org.mybatis.qbe.sql.insert.render.InsertSupportShortcut.insertValue;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.insert.render.InsertSupport;

public interface SimpleTableFields {
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).withAlias("a");
    MyBatis3Field<String> firstName = MyBatis3Field.of("first_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<String> lastName = MyBatis3Field.of("last_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<Date> birthDate = MyBatis3Field.of("birth_date", JDBCType.DATE).withAlias("a");
    MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR).withAlias("a");
    
    static InsertSupport buildInsert(SimpleTableRecord record) {
        return insertValue(id, record.getId())
                .andValue(firstName, record.getFirstName())
                .andValue(lastName, record.getLastName())
                .andValue(birthDate, record.getBirthDate())
                .andValue(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }

    static InsertSupport buildInsertSelective(SimpleTableRecord record) {
        return insertSelective()
                .withValueIfPresent(id, record.getId())
                .withValueIfPresent(firstName, record.getFirstName())
                .withValueIfPresent(lastName, record.getLastName())
                .withValueIfPresent(birthDate, record.getBirthDate())
                .withValueIfPresent(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }
}
