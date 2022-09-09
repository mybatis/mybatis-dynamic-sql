/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.generated.always.mybatis;

import static examples.generated.always.mybatis.GeneratedAlwaysDynamicSqlSupport.firstName;
import static examples.generated.always.mybatis.GeneratedAlwaysDynamicSqlSupport.id;
import static examples.generated.always.mybatis.GeneratedAlwaysDynamicSqlSupport.lastName;
import static examples.generated.always.mybatis.PersonDynamicSqlSupport.person;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

import examples.generated.always.PersonRecord;

public interface PersonMapper {

    @InsertProvider(type = SqlProviderAdapter.class, method = "insertSelect")
    @Options(useGeneratedKeys = true, keyProperty = "parameters.id")
    int insertSelect(InsertSelectStatementProvider insertSelectStatement);

    @InsertProvider(type = SqlProviderAdapter.class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty = "row.id")
    int insert(InsertStatementProvider<PersonRecord> insertStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultipleWithGeneratedKeys")
    @Options(useGeneratedKeys = true, keyProperty = "records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<PersonRecord> records);

    default int insertMultiple(MultiRowInsertStatementProvider<PersonRecord> multiRowInsertStatement) {
        return insertMultiple(multiRowInsertStatement.getInsertStatement(), multiRowInsertStatement.getRecords());
    }

    @SelectProvider(type = SqlProviderAdapter.class, method="select")
    List<PersonRecord> selectMany(SelectStatementProvider selectStatement);

    // insertSelect when there are multiple generated keys expected
    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys = true, keyProperty = "keys.key")
    int insertSelectMultiple(@Param("insertStatement") String insertStatement, @Param("parameters") Map<String, Object> parameters,
            @Param("keys") GeneratedKeyList keys);

    default int insertSelect(InsertSelectStatementProvider insertSelectStatement, GeneratedKeyList keys) {
        return insertSelectMultiple(insertSelectStatement.getInsertStatement(), insertSelectStatement.getParameters(), keys);
    }

    default int insert(PersonRecord record) {
        return MyBatis3Utils.insert(this::insert, record, person, c ->
                c.map(firstName).toProperty("firstName")
                        .map(lastName).toProperty("lastName"));
    }

    default int insertMultiple(PersonRecord...records) {
        return insertMultiple(Arrays.asList(records));
    }

    default int insertMultiple(Collection<PersonRecord> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, person, c ->
                c.map(firstName).toProperty("firstName")
                        .map(lastName).toProperty("lastName"));
    }

    BasicColumn[] selectList =
            BasicColumn.columnList(id, firstName, lastName);

    default List<PersonRecord> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, person, completer);
    }
}
