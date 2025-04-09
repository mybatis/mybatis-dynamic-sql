/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.simple;

import static examples.simple.PersonDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualToWhenPresent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 *
 * Note: this is the canonical mapper with the new style methods
 * and represents the desired output for MyBatis Generator
 *
 */
@Mapper
public interface PersonMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<PersonRecord>, CommonUpdateMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Arg(column="A_ID", jdbcType=JdbcType.INTEGER, id=true, javaType = Integer.class)
    @Arg(column="first_name", jdbcType=JdbcType.VARCHAR, javaType = String.class)
    @Arg(column="last_name", jdbcType=JdbcType.VARCHAR, typeHandler=LastNameTypeHandler.class, javaType = LastName.class)
    @Arg(column="birth_date", jdbcType=JdbcType.DATE, javaType = Date.class)
    @Arg(column="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class, javaType = Boolean.class)
    @Arg(column="occupation", jdbcType=JdbcType.VARCHAR, javaType = String.class)
    @Arg(column="address_id", jdbcType=JdbcType.INTEGER, javaType = Integer.class)
    List<PersonRecord> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Arg(column="A_ID", jdbcType=JdbcType.INTEGER, id=true, javaType = Integer.class)
    @Arg(column="first_name", jdbcType=JdbcType.VARCHAR, javaType = String.class)
    @Arg(column="last_name", jdbcType=JdbcType.VARCHAR, typeHandler=LastNameTypeHandler.class, javaType = LastName.class)
    @Arg(column="birth_date", jdbcType=JdbcType.DATE, javaType = Date.class)
    @Arg(column="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class, javaType = Boolean.class)
    @Arg(column="occupation", jdbcType=JdbcType.VARCHAR, javaType = String.class)
    @Arg(column="address_id", jdbcType=JdbcType.INTEGER, javaType = Integer.class)
    Optional<PersonRecord> selectOne(SelectStatementProvider selectStatement);

    BasicColumn[] selectList =
            BasicColumn.columnList(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId);

    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, person, completer);
    }

    default long count(BasicColumn column, CountDSLCompleter completer) {
        return MyBatis3Utils.count(this::count, column, person, completer);
    }

    default long countDistinct(BasicColumn column, CountDSLCompleter completer) {
        return MyBatis3Utils.countDistinct(this::count, column, person, completer);
    }

    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, person, completer);
    }

    default int deleteByPrimaryKey(Integer recordId) {
        return delete(c ->
            c.where(id, isEqualTo(recordId))
        );
    }

    default int generalInsert(UnaryOperator<GeneralInsertDSL> completer) {
        return MyBatis3Utils.generalInsert(this::generalInsert, person, completer);
    }

    default int insert(PersonRecord row) {
        return MyBatis3Utils.insert(this::insert, row, person, c ->
            c.map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .map(birthDate).toProperty("birthDate")
            .map(employed).toProperty("employed")
            .map(occupation).toProperty("occupation")
            .map(addressId).toProperty("addressId")
        );
    }

    default int insertMultiple(PersonRecord...records) {
        return insertMultiple(Arrays.asList(records));
    }

    default int insertMultiple(Collection<PersonRecord> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, person, c ->
            c.map(id).toProperty("id")
            .map(firstName).toProperty("firstName")
            .map(lastName).toProperty("lastName")
            .map(birthDate).toProperty("birthDate")
            .map(employed).toProperty("employed")
            .map(occupation).toProperty("occupation")
            .map(addressId).toProperty("addressId")
        );
    }

    default int insertSelective(PersonRecord row) {
        return MyBatis3Utils.insert(this::insert, row, person, c ->
            c.map(id).toPropertyWhenPresent("id", row::id)
            .map(firstName).toPropertyWhenPresent("firstName", row::firstName)
            .map(lastName).toPropertyWhenPresent("lastName", row::lastName)
            .map(birthDate).toPropertyWhenPresent("birthDate", row::birthDate)
            .map(employed).toPropertyWhenPresent("employed", row::employed)
            .map(occupation).toPropertyWhenPresent("occupation", row::occupation)
            .map(addressId).toPropertyWhenPresent("addressId", row::addressId)
        );
    }

    default Optional<PersonRecord> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, person, completer);
    }

    default List<PersonRecord> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, person, completer);
    }

    default List<PersonRecord> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, person, completer);
    }

    default Optional<PersonRecord> selectByPrimaryKey(Integer recordId) {
        return selectOne(c ->
            c.where(id, isEqualTo(recordId))
        );
    }

    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, person, completer);
    }

    static UpdateDSL<UpdateModel> updateAllColumns(PersonRecord row,
            UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToOrNull(row::id)
                .set(firstName).equalToOrNull(row::firstName)
                .set(lastName).equalToOrNull(row::lastName)
                .set(birthDate).equalToOrNull(row::birthDate)
                .set(employed).equalToOrNull(row::employed)
                .set(occupation).equalToOrNull(row::occupation)
                .set(addressId).equalToOrNull(row::addressId);
    }

    static UpdateDSL<UpdateModel> updateSelectiveColumns(PersonRecord row,
            UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::id)
                .set(firstName).equalToWhenPresent(row::firstName)
                .set(lastName).equalToWhenPresent(row::lastName)
                .set(birthDate).equalToWhenPresent(row::birthDate)
                .set(employed).equalToWhenPresent(row::employed)
                .set(occupation).equalToWhenPresent(row::occupation)
                .set(addressId).equalToWhenPresent(row::addressId);
    }

    default int updateByPrimaryKey(PersonRecord row) {
        return update(c ->
            c.set(firstName).equalToOrNull(row::firstName)
            .set(lastName).equalToOrNull(row::lastName)
            .set(birthDate).equalToOrNull(row::birthDate)
            .set(employed).equalToOrNull(row::employed)
            .set(occupation).equalToOrNull(row::occupation)
            .set(addressId).equalToOrNull(row::addressId)
            .where(id, isEqualToWhenPresent(row::id))
        );
    }

    default int updateByPrimaryKeySelective(PersonRecord row) {
        return update(c ->
            c.set(firstName).equalToWhenPresent(row::firstName)
            .set(lastName).equalToWhenPresent(row::lastName)
            .set(birthDate).equalToWhenPresent(row::birthDate)
            .set(employed).equalToWhenPresent(row::employed)
            .set(occupation).equalToWhenPresent(row::occupation)
            .set(addressId).equalToWhenPresent(row::addressId)
            .where(id, isEqualToWhenPresent(row::id))
        );
    }
}
