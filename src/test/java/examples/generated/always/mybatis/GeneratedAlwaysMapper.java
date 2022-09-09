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

import static examples.generated.always.mybatis.GeneratedAlwaysDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

import examples.generated.always.GeneratedAlwaysRecord;

public interface GeneratedAlwaysMapper extends CommonUpdateMapper {
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="gaResults", value={
        @Result(property="id", column="id", id=true),
        @Result(property="firstName", column="first_name"),
        @Result(property="lastName", column="last_name"),
        @Result(property="fullName", column="full_name")
    })
    List<GeneratedAlwaysRecord> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("gaResults")
    Optional<GeneratedAlwaysRecord> selectOne(SelectStatementProvider selectStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true, keyProperty="row.fullName")
    int insert(InsertStatementProvider<GeneratedAlwaysRecord> insertStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultipleWithGeneratedKeys")
    @Options(useGeneratedKeys=true, keyProperty="records.fullName")
    int insertMultiple(@Param("insertStatement") String statement, @Param("records") List<GeneratedAlwaysRecord> records);

    BasicColumn[] selectList =
            BasicColumn.columnList(id, firstName, lastName, fullName);

    default Optional<GeneratedAlwaysRecord> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, generatedAlways, completer);
    }

    default List<GeneratedAlwaysRecord> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, generatedAlways, completer);
    }

    default Optional<GeneratedAlwaysRecord> selectByPrimaryKey(Integer _id) {
        return selectOne(c -> c.where(id, isEqualTo(_id)));
    }

    default int insert(GeneratedAlwaysRecord record) {
        return MyBatis3Utils.insert(this::insert, record, generatedAlways, c ->
            c.map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
        );
    }

    default int insertMultiple(GeneratedAlwaysRecord...records) {
        return insertMultiple(Arrays.asList(records));
    }

    default int insertMultiple(Collection<GeneratedAlwaysRecord> records) {
        return MyBatis3Utils.insertMultipleWithGeneratedKeys(this::insertMultiple, records, generatedAlways, c ->
                c.map(id).toProperty("id")
                        .map(firstName).toProperty("firstName")
                        .map(lastName).toProperty("lastName")
        );
    }

    default int insertSelective(GeneratedAlwaysRecord record) {
        return MyBatis3Utils.insert(this::insert, record, generatedAlways, c ->
                c.map(id).toPropertyWhenPresent("id", record::getId)
                        .map(firstName).toPropertyWhenPresent("firstName", record::getFirstName)
                        .map(lastName).toPropertyWhenPresent("lastName", record::getLastName)
        );
    }

    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, generatedAlways, completer);
    }

    default int updateByPrimaryKey(GeneratedAlwaysRecord record) {
        return update(c ->
                c.set(firstName).equalTo(record::getFirstName)
                .set(lastName).equalTo(record::getLastName)
                .where(id, isEqualTo(record::getId))
        );
    }

    default int updateByPrimaryKeySelective(GeneratedAlwaysRecord record) {
        return update(c ->
                c.set(firstName).equalToWhenPresent(record::getFirstName)
                        .set(lastName).equalToWhenPresent(record::getLastName)
                        .where(id, isEqualTo(record::getId))
        );
    }

    static UpdateDSL<UpdateModel> updateSelectiveColumns(GeneratedAlwaysRecord record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(record::getId)
                .set(firstName).equalToWhenPresent(record::getFirstName)
                .set(lastName).equalToWhenPresent(record::getLastName);
    }
}
