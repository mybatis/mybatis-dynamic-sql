package examples.animal.data;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.qbe.sql.render.RenderedWhereClause;

public interface AnimalDataMapper {

    @SelectProvider(type=AnimalDataSqlProvider.class, method="selectByExample")
    List<AnimalData> selectByExample(RenderedWhereClause renderedWhereClause);

    @DeleteProvider(type=AnimalDataSqlProvider.class, method="deleteByExample")
    int deleteByExample(RenderedWhereClause renderedWhereClause);
}
