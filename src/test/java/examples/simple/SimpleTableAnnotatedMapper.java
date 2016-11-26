package examples.simple;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface SimpleTableAnnotatedMapper {
    
    @Insert({
        "insert into simpletable",
        "${fieldsPhrase}",
        "${valuesPhrase}"
    })
    int insert(InsertSupport insertSupport);

    @Update({
        "update simpletable",
        "${setClause}",
        "${whereClause}"
    })
    int update(UpdateSupport updateSupport);
    
    @Select({
        "select a.id, a.first_name, a.last_name, a.birth_date, a.occupation",
        "from simpletable a",
        "${whereClause}"
    })
    @ResultMap("SimpleTableResult")
    List<SimpleTableRecord> selectByExample(WhereSupport whereSupport);
    
    @Select({
        "select a.id, a.first_name, a.last_name, a.birth_date, a.occupation",
        "from simpletable a",
        "where a.id = #{value}"
    })
    @Results(id="SimpleTableResult", value= {
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="first_name", property="firstName", jdbcType=JdbcType.VARCHAR),
            @Result(column="last_name", property="lastName", jdbcType=JdbcType.VARCHAR),
            @Result(column="birth_date", property="birthDate", jdbcType=JdbcType.DATE),
            @Result(column="occupation", property="occupation", jdbcType=JdbcType.VARCHAR)
    })
    SimpleTableRecord selectByPrimaryKey(int id);
    
    @Delete({
        "delete from simpletable",
        "${whereClause}"
    })
    int deleteByExample(WhereSupport whereSupport);
}
