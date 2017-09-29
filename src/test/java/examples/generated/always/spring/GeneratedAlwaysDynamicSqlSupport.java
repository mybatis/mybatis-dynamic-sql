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

import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModelBuilder.SelectSupportAfterFromBuilder;
import org.mybatis.dynamic.sql.update.UpdateModelBuilder;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;

public class GeneratedAlwaysDynamicSqlSupport {
    public static final GeneratedAlways generatedAlways = new GeneratedAlways();
    public static final SqlColumn<Integer> id = generatedAlways.id;
    public static final SqlColumn<String> firstName = generatedAlways.firstName;
    public static final SqlColumn<String> lastName = generatedAlways.lastName;
    public static final SqlColumn<String> fullName = generatedAlways.fullName;
    
    public static class GeneratedAlways extends SqlTable {
        public GeneratedAlways() {
            super("GeneratedAlways");
        }

        public SqlColumn<Integer> id = column("id", JDBCType.INTEGER).withAlias("A_ID");
        public SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR);
        public SqlColumn<String> lastName = column("last_name", JDBCType.VARCHAR);
        public SqlColumn<String> fullName = column("full_name", JDBCType.VARCHAR);
    }
    
    public static InsertSupport<GeneratedAlwaysRecord> buildInsertSupport(GeneratedAlwaysRecord record) {
        return insert(record)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
    }

    public static InsertSupport<GeneratedAlwaysRecord> buildInsertSelectiveSupport(GeneratedAlwaysRecord record) {
        return insert(record)
                .into(generatedAlways)
                .map(id).toPropertyWhenPresent("id")
                .map(firstName).toPropertyWhenPresent("firstName")
                .map(lastName).toPropertyWhenPresent("lastName")
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
    }
    
    public static UpdateSupport buildUpdateByPrimaryKeySupport(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(firstName).equalTo(record.getFirstName())
                .set(lastName).equalTo(record.getLastName())
                .where(id, isEqualTo(record.getId()))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
    }

    public static UpdateSupport buildUpdateByPrimaryKeySelectiveSupport(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(firstName).equalToWhenPresent(record.getFirstName())
                .set(lastName).equalToWhenPresent(record.getLastName())
                .where(id, isEqualTo(record.getId()))
                .build()
                .render(RenderingStrategy.SPRING_NAMED_PARAMETER);
    }

    public static UpdateModelBuilder updateByExample(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(id).equalTo(record.getId())
                .set(firstName).equalTo(record.getFirstName())
                .set(lastName).equalTo(record.getLastName());
    }

    public static UpdateModelBuilder updateByExampleSelective(GeneratedAlwaysRecord record) {
        return update(generatedAlways)
                .set(id).equalToWhenPresent(record.getId())
                .set(firstName).equalToWhenPresent(record.getFirstName())
                .set(lastName).equalToWhenPresent(record.getLastName());
    }

    public static SelectSupportAfterFromBuilder selectByExample() {
        return select(id, firstName, lastName, fullName)
            .from(generatedAlways, "a");
    }
}
