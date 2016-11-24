package examples.simple;

import java.util.List;

import org.mybatis.qbe.sql.insert.render.InsertSupport;
import org.mybatis.qbe.sql.where.render.WhereSupport;

public interface SimpleTableXmlMapper {
    List<SimpleTableRecord> selectByExample(WhereSupport whereSupport);
    int deleteByExample(WhereSupport whereSupport);
    int insert(InsertSupport insertSupport);
}
