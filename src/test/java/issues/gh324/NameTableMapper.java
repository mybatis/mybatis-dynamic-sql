/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package issues.gh324;

import static issues.gh324.NameTableDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.*;

@CacheNamespace(implementation = ObservableCache.class)
public interface NameTableMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<NameRecord>, CommonUpdateMapper {
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="NameTableResult", value={
            @Result(column="id", property="id", id=true),
            @Result(column="name", property="name")
    })
    List<NameRecord> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("NameTableResult")
    Optional<NameRecord> selectOne(SelectStatementProvider selectStatement);

    BasicColumn[] selectList = BasicColumn.columnList(id, name);

    default Optional<NameRecord> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, nameTable, completer);
    }

    default Optional<NameRecord> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
                c.where(id, isEqualTo(id_))
        );
    }

    default int insert(NameRecord record) {
        return MyBatis3Utils.insert(this::insert, record, nameTable, c ->
                c.map(id).toProperty("id")
                        .map(name).toProperty("name")
        );
    }

    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, nameTable, completer);
    }

    default int updateByPrimaryKey(NameRecord record) {
        return update(c ->
                c.set(name).equalTo(record::getName)
                        .where(id, isEqualTo(record::getId))
        );
    }

    default int deleteAll() {
        return MyBatis3Utils.deleteFrom(this::delete, nameTable, DeleteDSLCompleter.allRows());
    }
}
