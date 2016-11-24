package examples.simple;

import static org.mybatis.qbe.sql.insert.render.InsertSupportShortcut.insert;
import static org.mybatis.qbe.sql.update.UpdateSupportShortcut.*;
import static org.mybatis.qbe.sql.where.SqlConditions.*;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.insert.render.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;

public interface SimpleTableFields {
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).withAlias("a");
    MyBatis3Field<String> firstName = MyBatis3Field.of("first_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<String> lastName = MyBatis3Field.of("last_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<Date> birthDate = MyBatis3Field.of("birth_date", JDBCType.DATE).withAlias("a");
    MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR).withAlias("a");
    
    static InsertSupport buildInsert(SimpleTableRecord record) {
        return insert()
                .withValue(id, record.getId())
                .withValue(firstName, record.getFirstName())
                .withValue(lastName, record.getLastName())
                .withValue(birthDate, record.getBirthDate())
                .withValue(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }

    static InsertSupport buildInsertSelective(SimpleTableRecord record) {
        return insert()
                .withValueIfPresent(id, record.getId())
                .withValueIfPresent(firstName, record.getFirstName())
                .withValueIfPresent(lastName, record.getLastName())
                .withValueIfPresent(birthDate, record.getBirthDate())
                .withValueIfPresent(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }
    
    static UpdateSupport buildUpdateByPrimaryKey(SimpleTableRecord record) {
        return update()
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName())
                .set(birthDate, record.getBirthDate())
                .set(occupation, record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .buildIgnoringAlias();
    }

    static UpdateSupport buildUpdateByPrimaryKeySelective(SimpleTableRecord record) {
        return update()
                .setIfPresent(firstName, record.getFirstName())
                .setIfPresent(lastName, record.getLastName())
                .setIfPresent(birthDate, record.getBirthDate())
                .setIfPresent(occupation, record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .buildIgnoringAlias();
    }
}
