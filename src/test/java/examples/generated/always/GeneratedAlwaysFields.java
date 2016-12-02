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
package examples.generated.always;

import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;
import static org.mybatis.qbe.sql.update.UpdateSupportBuilder.updateSupport;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.update.UpdateSupportBuilder.SetBuilder;

public interface GeneratedAlwaysFields {
    SqlTable generatedAlways = SqlTable.of("GeneratedAlways").withAlias("a");
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).inTable(generatedAlways);
    MyBatis3Field<String> firstName = MyBatis3Field.of("first_name", JDBCType.VARCHAR).inTable(generatedAlways);
    MyBatis3Field<String> lastName = MyBatis3Field.of("last_name", JDBCType.VARCHAR).inTable(generatedAlways);
    MyBatis3Field<String> fullName = MyBatis3Field.of("full_name", JDBCType.VARCHAR).inTable(generatedAlways);
    
    static InsertSupport<GeneratedAlwaysRecord> buildInsertSupport(GeneratedAlwaysRecord record) {
        return insertSupport(record)
                .withFieldMapping(id, "id", GeneratedAlwaysRecord::getId)
                .withFieldMapping(firstName, "firstName", GeneratedAlwaysRecord::getFirstName)
                .withFieldMapping(lastName, "lastName", GeneratedAlwaysRecord::getLastName)
                .buildFullInsert();
    }

    static InsertSupport<GeneratedAlwaysRecord> buildInsertSelectiveSupport(GeneratedAlwaysRecord record) {
        return insertSupport(record)
                .withFieldMapping(id, "id", GeneratedAlwaysRecord::getId)
                .withFieldMapping(firstName, "firstName", GeneratedAlwaysRecord::getFirstName)
                .withFieldMapping(lastName, "lastName", GeneratedAlwaysRecord::getLastName)
                .buildSelectiveInsert();
    }
    
    static UpdateSupport buildUpdateByPrimaryKeySupport(GeneratedAlwaysRecord record) {
        return updateSupport()
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName())
                .where(id, isEqualTo(record.getId()))
                .build();
    }

    static UpdateSupport buildUpdateByPrimaryKeySelectiveSupport(GeneratedAlwaysRecord record) {
        return updateSupport()
                .set(firstName, Optional.ofNullable(record.getFirstName()))
                .set(lastName, Optional.ofNullable(record.getLastName()))
                .where(id, isEqualTo(record.getId()))
                .build();
    }

    static SetBuilder updateByExample(GeneratedAlwaysRecord record) {
        return updateSupport()
                .set(id, record.getId())
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName());
    }

    static SetBuilder updateByExampleSelective(GeneratedAlwaysRecord record) {
        return updateSupport()
                .set(id, Optional.ofNullable(record.getId()))
                .set(firstName, Optional.ofNullable(record.getFirstName()))
                .set(lastName, Optional.ofNullable(record.getLastName()));
    }
}
