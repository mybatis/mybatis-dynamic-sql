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
package examples.generated.always.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import examples.generated.always.GeneratedAlwaysRecord;

public interface GeneratedAlwaysAnnotatedMapper {
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
    GeneratedAlwaysRecord selectByPrimaryKey(SelectStatementProvider selectStatement);
    
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true, keyProperty="record.fullName")
    int insert(InsertStatementProvider<GeneratedAlwaysRecord> insertStatement);
    
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);
    
    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultiple")
    int insertMultiple(MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiInsert);

    // This is kludgy. Currently MyBatis does not support nested lists in parameter objects
    // when returning generated keys.
    // So we need to do this silliness and decompose the multi row insert into its component parts
    // for the actual MyBatis call
    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true, keyProperty="records.fullName")
    int insertMultipleWithGeneratedKeys(@Param("insertStatement") String statement, @Param("records") List<GeneratedAlwaysRecord> records);

    default int insertMultipleWithGeneratedKeys(MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiInsert) {
        return insertMultipleWithGeneratedKeys(multiInsert.getInsertStatement(), multiInsert.getRecords());
    }
}
