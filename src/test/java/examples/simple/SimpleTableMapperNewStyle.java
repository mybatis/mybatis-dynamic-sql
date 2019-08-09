/**
 *    Copyright 2016-2019 the original author or authors.
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

import static examples.simple.SimpleTableDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3CountHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3DeleteHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectListHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectOneHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3UpdateHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3UpdateModelToIntAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

/**
 * 
 * Note: this is the canonical mapper with the new style methods
 * and represents the desired output for MyBatis Generator 
 *
 */
@Mapper
public interface SimpleTableMapperNewStyle {
    
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    int insert(InsertStatementProvider<SimpleTableRecord> insertStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultiple")
    int insertMultiple(MultiRowInsertStatementProvider<SimpleTableRecord> insertStatement);

    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SimpleTableResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR, typeHandler=LastNameTypeHandler.class),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectStatementProvider selectStatement);
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SimpleTableResult")
    Optional<SimpleTableRecord> selectOne(SelectStatementProvider selectStatement);
    
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);
    
    default long count(MyBatis3CountHelper helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(simpleTable))
                .build()
                .execute();
    }

    default int delete(MyBatis3DeleteHelper helper) {
        return helper.apply(MyBatis3Utils.deleteFrom(this::delete, simpleTable))
                .build()
                .execute();
    }
    
    default int deleteByPrimaryKey(Integer id_) {
        return delete(h ->
            h.where(id, isEqualTo(id_))
        );
    }

    default int insert(SimpleTableRecord record) {
        return insert(SqlBuilder.insert(record)
                .into(simpleTable)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    default int insertMultiple(List<SimpleTableRecord> records) {
        return insertMultiple(SqlBuilder.insertMultiple(records)
                .into(simpleTable)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employed")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    default int insertSelective(SimpleTableRecord record) {
        return insert(SqlBuilder.insert(record)
                .into(simpleTable)
                .map(id).toPropertyWhenPresent("id", record::getId)
                .map(firstName).toPropertyWhenPresent("firstName", record::getFirstName)
                .map(lastName).toPropertyWhenPresent("lastName", record::getLastName)
                .map(birthDate).toPropertyWhenPresent("birthDate", record::getBirthDate)
                .map(employed).toPropertyWhenPresent("employed", record::getEmployed)
                .map(occupation).toPropertyWhenPresent("occupation", record::getOccupation)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }
    
    default Optional<SimpleTableRecord> selectOne(MyBatis3SelectOneHelper<SimpleTableRecord> helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::selectOne, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
                .from(simpleTable))
                .build()
                .execute();
    }
    
    default List<SimpleTableRecord> select(MyBatis3SelectListHelper<SimpleTableRecord> helper) {
        return helper.apply(SelectDSL.selectWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
                .from(simpleTable))
                .build()
                .execute();
    }
    
    default List<SimpleTableRecord> selectDistinct(MyBatis3SelectListHelper<SimpleTableRecord> helper) {
        return helper.apply(SelectDSL.selectDistinctWithMapper(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
                .from(simpleTable))
                .build()
                .execute();
    }
    
    default Optional<SimpleTableRecord> selectByPrimaryKey(Integer id_) {
        return selectOne(h -> 
            h.where(id, isEqualTo(id_))
        );
    }

    default int update(MyBatis3UpdateHelper helper) {
        return helper.apply(MyBatis3Utils.update(this::update, simpleTable))
                .build()
                .execute();
    }
    
    static UpdateDSL<MyBatis3UpdateModelToIntAdapter> setAll(SimpleTableRecord record,
            UpdateDSL<MyBatis3UpdateModelToIntAdapter> dsl) {
        return dsl.set(id).equalTo(record::getId)
                .set(firstName).equalTo(record::getFirstName)
                .set(lastName).equalTo(record::getLastName)
                .set(birthDate).equalTo(record::getBirthDate)
                .set(employed).equalTo(record::getEmployed)
                .set(occupation).equalTo(record::getOccupation);
    }
    
    static UpdateDSL<MyBatis3UpdateModelToIntAdapter> setSelective(SimpleTableRecord record,
            UpdateDSL<MyBatis3UpdateModelToIntAdapter> dsl) {
        return dsl.set(id).equalToWhenPresent(record::getId)
                .set(firstName).equalToWhenPresent(record::getFirstName)
                .set(lastName).equalToWhenPresent(record::getLastName)
                .set(birthDate).equalToWhenPresent(record::getBirthDate)
                .set(employed).equalToWhenPresent(record::getEmployed)
                .set(occupation).equalToWhenPresent(record::getOccupation);
    }
    
    default int updateByPrimaryKey(SimpleTableRecord record) {
        return update(h ->
            h.set(firstName).equalTo(record::getFirstName)
            .set(lastName).equalTo(record::getLastName)
            .set(birthDate).equalTo(record::getBirthDate)
            .set(employed).equalTo(record::getEmployed)
            .set(occupation).equalTo(record::getOccupation)
            .where(id, isEqualTo(record::getId))
        );
    }

    default int updateByPrimaryKeySelective(SimpleTableRecord record) {
        return update(h ->
            h.set(firstName).equalToWhenPresent(record::getFirstName)
            .set(lastName).equalToWhenPresent(record::getLastName)
            .set(birthDate).equalToWhenPresent(record::getBirthDate)
            .set(employed).equalToWhenPresent(record::getEmployed)
            .set(occupation).equalToWhenPresent(record::getOccupation)
            .where(id, isEqualTo(record::getId))
        );
    }
}
