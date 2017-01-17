/**
 *    Copyright 2016-2017 the original author or authors.
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
package examples.generated.always.spring;

import static org.mybatis.dynamic.sql.SqlConditions.isEqualTo;
import static org.mybatis.dynamic.sql.insert.InsertSupportBuilder.insert;
import static org.mybatis.dynamic.sql.select.SelectSupportBuilder.select;
import static org.mybatis.dynamic.sql.update.UpdateSupportBuilder.update;

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.SpringNamedParameterColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.InsertSupport;
import org.mybatis.dynamic.sql.select.SelectSupportBuilder.SelectSupportBuildStep2;
import org.mybatis.dynamic.sql.update.UpdateSupport;
import org.mybatis.dynamic.sql.update.UpdateSupportBuilder.UpdateSupportBuildStep1;

public interface GeneratedAlwaysFields {
    SqlTable generatedAlways = SqlTable.of("GeneratedAlways").withAlias("a");
    SpringNamedParameterColumn<Integer> id = SpringNamedParameterColumn.of("id", JDBCType.INTEGER).inTable(generatedAlways).withAlias("A_ID");
    SpringNamedParameterColumn<String> firstName = SpringNamedParameterColumn.of("first_name", JDBCType.VARCHAR).inTable(generatedAlways);
    SpringNamedParameterColumn<String> lastName = SpringNamedParameterColumn.of("last_name", JDBCType.VARCHAR).inTable(generatedAlways);
    SpringNamedParameterColumn<String> fullName = SpringNamedParameterColumn.of("full_name", JDBCType.VARCHAR).inTable(generatedAlways);
    
    static InsertSupport<GeneratedAlwaysRecord> buildInsertSupport(GeneratedAlwaysRecord record) {
        return insert(record)
                .into(generatedAlways)
                .withColumnMapping(id, "id", record.getId())
                .withColumnMapping(firstName, "firstName", record.getFirstName())
                .withColumnMapping(lastName, "lastName", record.getLastName())
                .build();
    }

    static InsertSupport<GeneratedAlwaysRecord> buildInsertSelectiveSupport(GeneratedAlwaysRecord record) {
        return insert(record)
                .into(generatedAlways)
                .withColumnMapping(id, "id", Optional.ofNullable(record.getId()))
                .withColumnMapping(firstName, "firstName", Optional.ofNullable(record.getFirstName()))
                .withColumnMapping(lastName, "lastName", Optional.ofNullable(record.getLastName()))
                .build();
    }
    
    static UpdateSupport buildUpdateByPrimaryKeySupport(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName())
                .where(id, isEqualTo(record.getId()))
                .build();
    }

    static UpdateSupport buildUpdateByPrimaryKeySelectiveSupport(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(firstName, Optional.ofNullable(record.getFirstName()))
                .set(lastName, Optional.ofNullable(record.getLastName()))
                .where(id, isEqualTo(record.getId()))
                .build();
    }

    static UpdateSupportBuildStep1 updateByExample(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(id, record.getId())
                .set(firstName, record.getFirstName())
                .set(lastName, record.getLastName());
    }

    static UpdateSupportBuildStep1 updateByExampleSelective(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(id, Optional.ofNullable(record.getId()))
                .set(firstName, Optional.ofNullable(record.getFirstName()))
                .set(lastName, Optional.ofNullable(record.getLastName()));
    }

    static SelectSupportBuildStep2 selectByExample() {
        return select(id, firstName, lastName, fullName)
            .from(generatedAlways);
    }
}
