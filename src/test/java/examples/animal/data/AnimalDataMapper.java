package examples.animal.data;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.mybatis.qbe.mybatis3.UpdateParameter;
import org.mybatis.qbe.sql.insert.render.RenderedInsertSupport;
import org.mybatis.qbe.sql.where.render.RenderedWhereClause;

public interface AnimalDataMapper {

    @Select({
        "select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight",
        "from AnimalData a",
        "${whereClause}"
    })
    List<AnimalData> selectByExample(RenderedWhereClause renderedWhereClause);

    @Delete({
        "delete from AnimalData",
        "${whereClause}"
    })
    int deleteByExample(RenderedWhereClause renderedWhereClause);

    @Update({
        "update AnimalData",
        "${setClause}",
        "${whereClause}"
    })
    int updateByExample(UpdateParameter updateParameter);
    
    @Insert({
        "insert into AnimalData",
        "${fieldsPhrase}",
        "${valuesPhrase}"
    })
    int insert(RenderedInsertSupport insertSupport);
    
    @SelectProvider(type=AnimalDataSqlProvider.class, method="selectByExample")
    List<AnimalData> selectByExampleWithProvider(RenderedWhereClause renderedWhereClause);

    @DeleteProvider(type=AnimalDataSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(RenderedWhereClause renderedWhereClause);
}
