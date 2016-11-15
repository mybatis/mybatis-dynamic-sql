package simple.example;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public interface SimpleTableMapper {
    // methods in XML
    List<SimpleTable> selectByExample(WhereClauseAndParameters whereClauseAndParameters);
    int deleteByExample(WhereClauseAndParameters whereClauseAndParameters);
    
    // methods in select providers
    @SelectProvider(type=SimpleTableSqlProvider.class, method="selectByExample")
    List<SimpleTable> selectByExampleWithProvider(WhereClauseAndParameters whereClauseAndParameters);

    @DeleteProvider(type=SimpleTableSqlProvider.class, method="deleteByExample")
    int deleteByExampleWithProvider(WhereClauseAndParameters whereClauseAndParameters);
}
