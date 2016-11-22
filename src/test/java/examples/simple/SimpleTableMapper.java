package examples.simple;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.qbe.sql.where.render.WhereSupport;

public interface SimpleTableMapper {
    // methods in XML
    List<SimpleTable> selectByExample(WhereSupport whereSupport);
    int deleteByExample(WhereSupport whereSupport);
    
    // methods in select providers
    @SelectProvider(type=SimpleTableSqlProvider.class, method="selectByExample")
    List<SimpleTable> selectByExampleWithProvider(WhereSupport whereSupport);

    @DeleteProvider(type=SimpleTableSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(WhereSupport whereSupport);
}
