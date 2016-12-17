/**
 *    Copyright 2016 the original author or authors.
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
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.select.SelectSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.where.WhereSupport;

@Mapper
public interface SimpleTableAnnotatedMapper {
    
    @Insert({
        "insert into simpletable",
        "${fieldsPhrase}",
        "${valuesPhrase}"
    })
    int insert(InsertSupport<SimpleTableRecord> insertSupport);

    @Update({
        "update simpletable",
        "${setClause}",
        "${whereClause}"
    })
    int update(UpdateSupport updateSupport);
    
    @Select({
        "select ${distinct} a.id, a.first_name, a.last_name, a.birth_date, a.occupation",
        "from simpletable a",
        "${whereClause}",
        "${orderByClause}"
    })
    @Results(id="SimpleTableResult", value= {
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    List<SimpleTableRecord> selectByExample(SelectSupport selectSupport);
    
    @Select({
        "select a.id, a.first_name, a.last_name, a.birth_date, a.occupation",
        "from simpletable a",
        "where a.id = #{value}"
    })
    @ResultMap("SimpleTableResult")
    SimpleTableRecord selectByPrimaryKey(int id);
    
    @Delete({
        "delete from simpletable",
        "${whereClause}"
    })
    int delete(WhereSupport whereSupport);

    @Select({
        "select count(*)",
        "from simpletable",
        "${whereClause}"
    })
    int countByExample(WhereSupport whereSupport);
}
