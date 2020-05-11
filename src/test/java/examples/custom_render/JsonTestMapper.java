package examples.custom_render;

import static examples.custom_render.JsonTestDynamicSqlSupport.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

public interface JsonTestMapper {
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    List<Map<String, Object>> generalSelect(SelectStatementProvider selectStatement);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Results(id = "JsonTestResult", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "description", property = "description"),
            @Result(column = "info", property = "info")
    })
    List<JsonTest> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultMap("JsonTestResult")
    Optional<JsonTest> selectOne(SelectStatementProvider selectStatement);

    @DeleteProvider(type = SqlProviderAdapter.class, method = "delete")
    int delete(DeleteStatementProvider deleteStatement);

    @UpdateProvider(type = SqlProviderAdapter.class, method = "update")
    int update(UpdateStatementProvider updateStatement);

    @InsertProvider(type = SqlProviderAdapter.class, method = "insert")
    int insert(InsertStatementProvider<JsonTest> insertStatement);
    
    @InsertProvider(type=SqlProviderAdapter.class, method="insertMultiple")
    int insertMultiple(MultiRowInsertStatementProvider<JsonTest> insertStatement);

    BasicColumn[] selectList =
            BasicColumn.columnList(id, description, info);

    default int insert(JsonTest record) {
        return MyBatis3Utils.insert(this::insert, record, jsonTest, c ->
           c.map(id).toProperty("id")
           .map(description).toProperty("description")
           .map(info).toProperty("info")
        );
    }
    
    default int insertMultiple(JsonTest...records) {
        return insertMultiple(Arrays.asList(records));
    }

    default int insertMultiple(Collection<JsonTest> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, jsonTest, c ->
            c.map(id).toProperty("id")
            .map(description).toProperty("description")
            .map(info).toProperty("info")
        );
    }

    default List<JsonTest> selectMany(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, jsonTest, completer);
    }
    
    default Optional<JsonTest> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, jsonTest, completer);
    }
    
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, jsonTest, completer);
    }
    
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, jsonTest, completer);
    }
}
