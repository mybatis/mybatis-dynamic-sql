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
package examples.column.comparison;

import java.util.List;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

public interface ColumnComparisonMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results({
        @Result(column="number1", property="number1", id=true),
        @Result(column="number2", property="number2", id=true)
    })
    List<ColumnComparisonRecord> selectMany(SelectStatementProvider selectStatement);

    default List<ColumnComparisonRecord> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, ColumnComparisonDynamicSqlSupport.columnList,
                ColumnComparisonDynamicSqlSupport.columnComparison, completer);
    }
}
