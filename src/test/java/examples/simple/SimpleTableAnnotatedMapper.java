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

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.delete.render.DeleteProvider;
import org.mybatis.dynamic.sql.insert.render.InsertProvider;
import org.mybatis.dynamic.sql.select.render.SelectProvider;
import org.mybatis.dynamic.sql.update.render.UpdateProvider;

@Mapper
public interface SimpleTableAnnotatedMapper {
    
    @Insert({
        "${fullInsertStatement}"
    })
    int insert(InsertProvider<SimpleTableRecord> insertProvider);

    @Update({
        "${fullUpdateStatement}"
    })
    int update(UpdateProvider updateProvider);
    
    @Select({
        "${fullSelectStatement}"
    })
    @Results(id="SimpleTableResult", value= {
            @Result(column="A_ID", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="employed", property="employed", jdbcType=JdbcType.VARCHAR, typeHandler=YesNoTypeHandler.class),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectMany(SelectProvider selectProvider);
    
    @Select({
        "${fullSelectStatement}"
    })
    @ResultMap("SimpleTableResult")
    SimpleTableRecord selectOne(SelectProvider selectProvider);
    
    @Delete({
        "${fullDeleteStatement}"
    })
    int delete(DeleteProvider deleteProvider);

    @Select({
        "${fullSelectStatement}"
    })
    long count(SelectProvider selectProvider);
}
