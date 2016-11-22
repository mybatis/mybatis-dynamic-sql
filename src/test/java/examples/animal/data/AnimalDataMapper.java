package examples.animal.data;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.qbe.sql.render.RenderedWhereClause;

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

    @SelectProvider(type=AnimalDataSqlProvider.class, method="selectByExample")
    List<AnimalData> selectByExampleWithProvider(RenderedWhereClause renderedWhereClause);

    @DeleteProvider(type=AnimalDataSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(RenderedWhereClause renderedWhereClause);
}
