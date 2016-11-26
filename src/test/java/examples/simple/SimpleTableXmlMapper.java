package examples.simple;

import java.util.List;

import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface SimpleTableXmlMapper {
    List<SimpleTableRecord> selectByExample(WhereSupport whereSupport);
    int deleteByExample(WhereSupport whereSupport);
    int insert(InsertSupport insertSupport);
    int update(UpdateSupport updateSupport);
    SimpleTableRecord selectByPrimaryKey(int id);
}
