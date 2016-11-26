package examples.animal.data;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface AnimalDataMapper {

    @Select({
        "select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight",
        "from AnimalData a",
        "${whereClause}"
    })
    List<AnimalData> selectByExample(WhereSupport whereSupport);

    @Delete({
        "delete from AnimalData",
        "${whereClause}"
    })
    int deleteByExample(WhereSupport whereSupport);

    @Update({
        "update AnimalData",
        "${setClause}",
        "${whereClause}"
    })
    int updateByExample(UpdateSupport updateSupport);
    
    @Insert({
        "insert into AnimalData",
        "${fieldsPhrase}",
        "${valuesPhrase}"
    })
    int insert(InsertSupport insertSupport);
    
    @SelectProvider(type=AnimalDataSqlProvider.class, method="selectByExample")
    List<AnimalData> selectByExampleWithProvider(WhereSupport whereSupport);

    @DeleteProvider(type=AnimalDataSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(WhereSupport whereSupport);
}
