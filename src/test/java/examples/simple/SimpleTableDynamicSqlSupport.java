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
package examples.simple;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteSupport;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.QueryExpressionAfterFromBuilder;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;

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
    
    public static InsertSupport<SimpleTableRecord> buildFullInsertSupport(SimpleTableRecord record) {
        return insert(record)
                .into(simpleTable)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategy.MYBATIS3);
    }

    public static InsertSupport<SimpleTableRecord> buildSelectiveInsertSupport(SimpleTableRecord record) {
        return insert(record)
                .into(simpleTable)
                .map(id).toPropertyWhenPresent("id")
                .map(firstName).toPropertyWhenPresent("firstName")
                .map(lastName).toPropertyWhenPresent("lastName")
                .map(birthDate).toPropertyWhenPresent("birthDate")
                .map(employed).toPropertyWhenPresent("employed")
                .map(occupation).toPropertyWhenPresent("occupation")
                .build()
                .render(RenderingStrategy.MYBATIS3);
    }
    
    public static UpdateSupport buildFullUpdateByPrimaryKeySupport(SimpleTableRecord record) {
        return update(simpleTable)
                .set(firstName).equalTo(record.getFirstName())
                .set(lastName).equalTo(record.getLastName())
                .set(birthDate).equalTo(record.getBirthDate())
                .set(employed).equalTo(record.getEmployed())
                .set(occupation).equalTo(record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);
    }

    public static UpdateSupport buildSelectiveUpdateByPrimaryKeySupport(SimpleTableRecord record) {
        return update(simpleTable)
                .set(firstName).equalToWhenPresent(record.getFirstName())
                .set(lastName).equalToWhenPresent(record.getLastName())
                .set(birthDate).equalToWhenPresent(record.getBirthDate())
                .set(employed).equalToWhenPresent(record.getEmployed())
                .set(occupation).equalToWhenPresent(record.getOccupation())
                .where(id, isEqualTo(record.getId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);
    }

    public static UpdateDSL updateByExample(SimpleTableRecord record) {
        return update(simpleTable)
                .set(id).equalTo(record.getId())
                .set(firstName).equalTo(record.getFirstName())
                .set(lastName).equalTo(record.getLastName())
                .set(birthDate).equalTo(record.getBirthDate())
                .set(employed).equalTo(record.getEmployed())
                .set(occupation).equalTo(record.getOccupation());
    }

    public static UpdateDSL updateByExampleSelective(SimpleTableRecord record) {
        return update(simpleTable)
                .set(id).equalToWhenPresent(record.getId())
                .set(firstName).equalToWhenPresent(record.getFirstName())
                .set(lastName).equalToWhenPresent(record.getLastName())
                .set(birthDate).equalToWhenPresent(record.getBirthDate())
                .set(employed).equalToWhenPresent(record.getEmployed())
                .set(occupation).equalToWhenPresent(record.getOccupation());
    }

    public static DeleteSupport buildDeleteByPrimaryKeySupport(Integer id_) {
        return deleteFrom(simpleTable)
                .where(id, isEqualTo(id_))
                .build()
                .render(RenderingStrategy.MYBATIS3);
    }
    
    public static QueryExpressionAfterFromBuilder selectByExample() {
        return select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable);
    }

    public static SelectSupport buildSelectByPrimaryKeySupport(Integer id_) {
        return select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable)
            .where(id, isEqualTo(id_))
            .build()
            .render(RenderingStrategy.MYBATIS3);
    }
}
