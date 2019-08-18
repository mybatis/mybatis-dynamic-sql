/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.mybatis3;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.CompletableQuery;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

/**
 * Utility functions for building MyBatis3 mappers.
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3Utils {
    private MyBatis3Utils() {}

    public static long count(ToLongFunction<SelectStatementProvider> mapper,
            SqlTable table, MyBatis3SelectHelper helper) {
        return mapper.applyAsLong(
                helper.apply(SelectDSL.select(SqlBuilder.count()).from(table))
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    public static int deleteFrom(ToIntFunction<DeleteStatementProvider> mapper,
            SqlTable table, MyBatis3DeleteHelper helper) {
        return mapper.applyAsInt(
                helper.apply(DeleteDSL.deleteFrom(table))
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }
    
    public static <R> List<R> selectDistinct(Function<SelectStatementProvider, List<R>> mapper,
            BasicColumn[] selectList, SqlTable table, MyBatis3SelectHelper helper) {
        return selectDistinct(mapper, SelectDSL.selectDistinct(selectList).from(table), helper);
    }

    public static <R> List<R> selectDistinct(Function<SelectStatementProvider, List<R>> mapper,
            CompletableQuery<SelectModel> start, MyBatis3SelectHelper helper) {
        return mapper.apply(helper.apply(start).build().render(RenderingStrategy.MYBATIS3));
    }

    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
            BasicColumn[] selectList, SqlTable table, MyBatis3SelectHelper helper) {
        return selectList(mapper, SelectDSL.select(selectList).from(table), helper);
    }

    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
            CompletableQuery<SelectModel> start, MyBatis3SelectHelper helper) {
        return mapper.apply(helper.apply(start).build().render(RenderingStrategy.MYBATIS3));
    }

    public static <R> Optional<R> selectOne(Function<SelectStatementProvider, Optional<R>> mapper,
            BasicColumn[] selectList, SqlTable table, MyBatis3SelectHelper helper) {
        return selectOne(mapper, SelectDSL.select(selectList).from(table), helper);
    }

    public static <R> Optional<R> selectOne(Function<SelectStatementProvider, Optional<R>> mapper,
            CompletableQuery<SelectModel> start,
            MyBatis3SelectHelper helper) {
        return mapper.apply(helper.apply(start).build().render(RenderingStrategy.MYBATIS3));
    }

    public static int update(ToIntFunction<UpdateStatementProvider> mapper,
            SqlTable table, MyBatis3UpdateHelper helper) {
        return mapper.applyAsInt(
                helper.apply(UpdateDSL.update(table))
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }
}
