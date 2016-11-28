/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.simple;

import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;
import static org.mybatis.qbe.sql.update.UpdateSupportBuilder.updateSupport;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.update.UpdateSupportBuilder.SetBuilder;

public interface SimpleTableFields {
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).withAlias("a");
    MyBatis3Field<String> firstName = MyBatis3Field.of("first_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<String> lastName = MyBatis3Field.of("last_name", JDBCType.VARCHAR).withAlias("a");
    MyBatis3Field<Date> birthDate = MyBatis3Field.of("birth_date", JDBCType.DATE).withAlias("a");
    MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR).withAlias("a");
    
    static InsertSupport buildInsertSupport(SimpleTableRecord record) {
        return insertSupport()
                .withValue(id, record.getId())
                .withValue(firstName, record.getFirstName())
                .withValue(lastName, record.getLastName())
                .withValue(birthDate, record.getBirthDate())
                .withValue(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }

    static InsertSupport buildInsertSelectiveSupport(SimpleTableRecord record) {
        return insertSupport()
                .withValueIfPresent(id, record.getId())
                .withValueIfPresent(firstName, record.getFirstName())
                .withValueIfPresent(lastName, record.getLastName())
                .withValueIfPresent(birthDate, record.getBirthDate())
                .withValueIfPresent(occupation, record.getOccupation())
                .buildIgnoringAlias();
    }
    
    static UpdateSupport buildUpdateByPrimaryKeySupport(SimpleTableRecord record) {
        return updateSupport()
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName())
                .set(birthDate, record.getBirthDate())
                .set(occupation, record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .buildIgnoringAlias();
    }

    static UpdateSupport buildUpdateByPrimaryKeySelectiveSupport(SimpleTableRecord record) {
        return updateSupport()
                .setIfPresent(firstName, record.getFirstName())
                .setIfPresent(lastName, record.getLastName())
                .setIfPresent(birthDate, record.getBirthDate())
                .setIfPresent(occupation, record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .buildIgnoringAlias();
    }

    static SetBuilder updateByExample(SimpleTableRecord record) {
        return updateSupport()
                .set(id, record.getId())
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName())
                .set(birthDate, record.getBirthDate())
                .set(occupation, record.getOccupation());
    }

    static SetBuilder updateByExampleSelective(SimpleTableRecord record) {
        return updateSupport()
                .setIfPresent(id, record.getId())
                .setIfPresent(firstName, record.getFirstName())
                .setIfPresent(lastName, record.getLastName())
                .setIfPresent(birthDate, record.getBirthDate())
                .setIfPresent(occupation, record.getOccupation());
    }
}
