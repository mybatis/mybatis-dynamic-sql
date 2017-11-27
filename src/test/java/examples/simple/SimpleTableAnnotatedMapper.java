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

import static examples.simple.SimpleTableDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;

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
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.MyBatis3DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatement;
import org.mybatis.dynamic.sql.insert.render.InsertStatement;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModel;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatement;
import org.mybatis.dynamic.sql.update.MyBatis3UpdateModel;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatement;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

@Mapper
public interface SimpleTableAnnotatedMapper {
    
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    int insert(InsertStatement<SimpleTableRecord> insertStatement);

    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatement updateStatement);
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SimpleTableResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectStatement selectStatement);
    
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SimpleTableResult")
    SimpleTableRecord selectOne(SelectStatement selectStatement);
    
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatement deleteStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatement selectStatement);
    
    default QueryExpressionDSL<MyBatis3SelectModel<Long>>.QueryExpressionAfterFrom countByExample() {
        return SelectDSL.select(this::count, SqlBuilder.count())
            .from(simpleTable);
    }
    
    default DeleteDSL<MyBatis3DeleteModel> deleteByExample() {
        return DeleteDSL.deleteFrom(simpleTable, this::delete);
    }
    
    default int deleteByPrimaryKey(Integer id_) {
        return deleteByExample()
                .where(id, isEqualTo(id_))
                .build()
                .execute();
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

    default int insertSelective(SimpleTableRecord record) {
        return insert(SqlBuilder.insert(record)
                .into(simpleTable)
                .map(id).toPropertyWhenPresent("id")
                .map(firstName).toPropertyWhenPresent("firstName")
                .map(lastName).toPropertyWhenPresent("lastName")
                .map(birthDate).toPropertyWhenPresent("birthDate")
                .map(employed).toPropertyWhenPresent("employed")
                .map(occupation).toPropertyWhenPresent("occupation")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }
    
    default QueryExpressionDSL<MyBatis3SelectModel<List<SimpleTableRecord>>>.QueryExpressionAfterFrom selectByExample() {
        return SelectDSL.select(this::selectMany, id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable);
    }
    
    default SimpleTableRecord selectByPrimaryKey(Integer id_) {
        return selectOne(SqlBuilder.select(id.as("A_ID"), firstName, lastName, birthDate, employed, occupation)
            .from(simpleTable)
            .where(id, isEqualTo(id_))
            .build()
            .render(RenderingStrategy.MYBATIS3));
    }
    
    default UpdateDSL<MyBatis3UpdateModel> updateByExample(SimpleTableRecord record) {
        return UpdateDSL.update(this::update, simpleTable)
                .set(id).equalTo(record.getId())
                .set(firstName).equalTo(record.getFirstName())
                .set(lastName).equalTo(record.getLastName())
                .set(birthDate).equalTo(record.getBirthDate())
                .set(employed).equalTo(record.getEmployed())
                .set(occupation).equalTo(record.getOccupation());
    }

    default UpdateDSL<MyBatis3UpdateModel> updateByExampleSelective(SimpleTableRecord record) {
        return UpdateDSL.update(this::update, simpleTable)
                .set(id).equalToWhenPresent(record.getId())
                .set(firstName).equalToWhenPresent(record.getFirstName())
                .set(lastName).equalToWhenPresent(record.getLastName())
                .set(birthDate).equalToWhenPresent(record.getBirthDate())
                .set(employed).equalToWhenPresent(record.getEmployed())
                .set(occupation).equalToWhenPresent(record.getOccupation());
    }

    default int updateByPrimaryKey(SimpleTableRecord record) {
        return updateByExample(record)
                .where(id, isEqualTo(record.getId()))
                .build()
                .execute();
    }

    default int updateByPrimaryKeySelective(SimpleTableRecord record) {
        return updateByExampleSelective(record)
                .where(id, isEqualTo(record.getId()))
                .build()
                .execute();
    }
}
