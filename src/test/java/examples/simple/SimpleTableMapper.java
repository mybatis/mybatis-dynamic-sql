package examples.simple;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.qbe.sql.where.render.RenderedWhereClause;

public interface SimpleTableMapper {
    // methods in XML
    List<SimpleTable> selectByExample(RenderedWhereClause renderedWhereClause);
    int deleteByExample(RenderedWhereClause renderedWhereClause);
    
    // methods in select providers
    @SelectProvider(type=SimpleTableSqlProvider.class, method="selectByExample")
    List<SimpleTable> selectByExampleWithProvider(RenderedWhereClause renderedWhereClause);

    @DeleteProvider(type=SimpleTableSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(RenderedWhereClause renderedWhereClause);
}
