/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.util.mybatis3;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.dsl.CountDSL;
import org.mybatis.dynamic.sql.dsl.CountDSLCompleter;
import org.mybatis.dynamic.sql.dsl.DeleteDSL;
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.dsl.SelectDSL;
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter;
import org.mybatis.dynamic.sql.dsl.UpdateDSL;
import org.mybatis.dynamic.sql.dsl.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL;
import org.mybatis.dynamic.sql.insert.InsertDSL;
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

/**
 * Utility functions for building MyBatis3 mappers.
 *
 * @author Jeff Butler
 */
public class MyBatis3Utils {
    private MyBatis3Utils() {}

    //@Deprecated
    public static long count(ToLongFunction<SelectStatementProvider> mapper, BasicColumn column, SqlTable table,
                             org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return mapper.applyAsLong(count(column, table, completer));
    }

    //@Deprecated
    public static SelectStatementProvider count(BasicColumn column, SqlTable table,
                                                org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return countFrom(org.mybatis.dynamic.sql.select.CountDSL.count(column).from(table), completer);
    }

    public static long count(ToLongFunction<SelectStatementProvider> mapper, BasicColumn column, SqlTable table,
                             CountDSLCompleter completer) {
        return mapper.applyAsLong(count(column, table, completer));
    }

    public static SelectStatementProvider count(BasicColumn column, SqlTable table, CountDSLCompleter completer) {
        return countFrom(CountDSL.count(column).from(table), completer);
    }

    //@Deprecated
    public static long countDistinct(ToLongFunction<SelectStatementProvider> mapper, BasicColumn column, SqlTable table,
                                     org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return mapper.applyAsLong(countDistinct(column, table, completer));
    }

    //@Deprecated
    public static SelectStatementProvider countDistinct(BasicColumn column, SqlTable table,
                                                        org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return countFrom(org.mybatis.dynamic.sql.select.CountDSL.countDistinct(column).from(table), completer);
    }

    public static long countDistinct(ToLongFunction<SelectStatementProvider> mapper, BasicColumn column, SqlTable table,
                                     CountDSLCompleter completer) {
        return mapper.applyAsLong(countDistinct(column, table, completer));
    }

    public static SelectStatementProvider countDistinct(BasicColumn column, SqlTable table,
                                                        CountDSLCompleter completer) {
        return countFrom(CountDSL.countDistinct(column).from(table), completer);
    }

    //@Deprecated
    public static SelectStatementProvider countFrom(SqlTable table,
                                                    org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return countFrom(org.mybatis.dynamic.sql.select.CountDSL.countFrom(table), completer);
    }

    //@Deprecated
    public static long countFrom(ToLongFunction<SelectStatementProvider> mapper, SqlTable table,
                                 org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return mapper.applyAsLong(countFrom(table, completer));
    }

    //@Deprecated
    public static SelectStatementProvider countFrom(org.mybatis.dynamic.sql.select.CountDSL<SelectModel> start,
                                                    org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return completer.apply(start)
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    //@Deprecated
    public static long countFrom(ToLongFunction<SelectStatementProvider> mapper,
                                 org.mybatis.dynamic.sql.select.CountDSL<SelectModel> start,
                                 org.mybatis.dynamic.sql.select.CountDSLCompleter completer) {
        return mapper.applyAsLong(countFrom(start, completer));
    }

    public static SelectStatementProvider countFrom(SqlTable table, CountDSLCompleter completer) {
        return countFrom(CountDSL.countFrom(table), completer);
    }

    public static long countFrom(ToLongFunction<SelectStatementProvider> mapper, SqlTable table,
                                 CountDSLCompleter completer) {
        return mapper.applyAsLong(countFrom(table, completer));
    }

    public static SelectStatementProvider countFrom(CountDSL start, CountDSLCompleter completer) {
        return completer.apply(start)
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static long countFrom(ToLongFunction<SelectStatementProvider> mapper,
                                 CountDSL start, CountDSLCompleter completer) {
        return mapper.applyAsLong(countFrom(start, completer));
    }

    //@Deprecated
    public static DeleteStatementProvider deleteFrom(SqlTable table,
                                                     org.mybatis.dynamic.sql.delete.DeleteDSLCompleter completer) {
        return completer.apply(org.mybatis.dynamic.sql.delete.DeleteDSL.deleteFrom(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    //@Deprecated
    public static int deleteFrom(ToIntFunction<DeleteStatementProvider> mapper, SqlTable table,
                                 org.mybatis.dynamic.sql.delete.DeleteDSLCompleter completer) {
        return mapper.applyAsInt(deleteFrom(table, completer));
    }

    public static DeleteStatementProvider deleteFrom(SqlTable table, DeleteDSLCompleter completer) {
        return completer.apply(DeleteDSL.deleteFrom(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static int deleteFrom(ToIntFunction<DeleteStatementProvider> mapper, SqlTable table,
                                 DeleteDSLCompleter completer) {
        return mapper.applyAsInt(deleteFrom(table, completer));
    }

    public static <R> InsertStatementProvider<R> insert(R row, SqlTable table, UnaryOperator<InsertDSL<R>> completer) {
        return completer.apply(SqlBuilder.insert(row).into(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static <R> int insert(ToIntFunction<InsertStatementProvider<R>> mapper, R row, SqlTable table,
                                 UnaryOperator<InsertDSL<R>> completer) {
        return mapper.applyAsInt(insert(row, table, completer));
    }

    public static GeneralInsertStatementProvider generalInsert(SqlTable table,
                                                               UnaryOperator<GeneralInsertDSL> completer) {
        return completer.apply(GeneralInsertDSL.insertInto(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static int generalInsert(ToIntFunction<GeneralInsertStatementProvider> mapper,
                                    SqlTable table,
                                    UnaryOperator<GeneralInsertDSL> completer) {
        return mapper.applyAsInt(generalInsert(table, completer));
    }

    public static <R> MultiRowInsertStatementProvider<R> insertMultiple(Collection<R> records,
                                                                        SqlTable table,
                                                                        UnaryOperator<MultiRowInsertDSL<R>> completer) {
        return completer.apply(SqlBuilder.insertMultiple(records).into(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static <R> int insertMultiple(ToIntFunction<MultiRowInsertStatementProvider<R>> mapper,
                                         Collection<R> records,
                                         SqlTable table,
                                         UnaryOperator<MultiRowInsertDSL<R>> completer) {
        return mapper.applyAsInt(insertMultiple(records, table, completer));
    }

    public static <R> int insertMultipleWithGeneratedKeys(ToIntBiFunction<String, List<R>> mapper,
                                                          Collection<R> records,
                                                          SqlTable table,
                                                          UnaryOperator<MultiRowInsertDSL<R>> completer) {
        MultiRowInsertStatementProvider<R> provider = insertMultiple(records, table, completer);
        return mapper.applyAsInt(provider.getInsertStatement(), provider.getRecords());
    }

    //@Deprecated
    public static SelectStatementProvider select(BasicColumn[] selectList,
                                                 SqlTable table,
                                                 org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return select(org.mybatis.dynamic.sql.select.SelectDSL.select(selectList).from(table), completer);
    }

    //@Deprecated
    public static SelectStatementProvider select(QueryExpressionDSL<SelectModel> start,
                                                 org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return completer.apply(start)
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static SelectStatementProvider select(BasicColumn[] selectList, SqlTable table,
                                                 SelectDSLCompleter completer) {
        return select(SelectDSL.select(selectList).from(table), completer);
    }

    public static SelectStatementProvider select(SelectDSL start, SelectDSLCompleter completer) {
        return completer.apply(start)
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    //@Deprecated
    public static SelectStatementProvider selectDistinct(BasicColumn[] selectList, SqlTable table,
                                                         org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return select(org.mybatis.dynamic.sql.select.SelectDSL.selectDistinct(selectList).from(table), completer);
    }

    //@Deprecated
    public static <R> List<R> selectDistinct(Function<SelectStatementProvider, List<R>> mapper,
                                             BasicColumn[] selectList,
                                             SqlTable table,
                                             org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return mapper.apply(selectDistinct(selectList, table, completer));
    }

    public static SelectStatementProvider selectDistinct(BasicColumn[] selectList, SqlTable table,
                                                         SelectDSLCompleter completer) {
        return select(SelectDSL.selectDistinct(selectList).from(table), completer);
    }

    public static <R> List<R> selectDistinct(Function<SelectStatementProvider, List<R>> mapper,
                                             BasicColumn[] selectList, SqlTable table, SelectDSLCompleter completer) {
        return mapper.apply(selectDistinct(selectList, table, completer));
    }

    //@Deprecated
    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
            BasicColumn[] selectList, SqlTable table, org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return mapper.apply(select(selectList, table, completer));
    }

    //@Deprecated
    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
                                         QueryExpressionDSL<SelectModel> start,
                                         org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return mapper.apply(select(start, completer));
    }

    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
                                         BasicColumn[] selectList, SqlTable table, SelectDSLCompleter completer) {
        return mapper.apply(select(selectList, table, completer));
    }

    public static <R> List<R> selectList(Function<SelectStatementProvider, List<R>> mapper,
                                         SelectDSL start, SelectDSLCompleter completer) {
        return mapper.apply(select(start, completer));
    }

    //@Deprecated
    public static <R> R selectOne(Function<SelectStatementProvider, R> mapper,
                                  BasicColumn[] selectList,
                                  SqlTable table,
                                  org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return mapper.apply(select(selectList, table, completer));
    }

    //@Deprecated
    public static <R> R selectOne(Function<SelectStatementProvider, R> mapper,
                                  QueryExpressionDSL<SelectModel> start,
                                  org.mybatis.dynamic.sql.select.SelectDSLCompleter completer) {
        return mapper.apply(select(start, completer));
    }

    public static <R> R selectOne(Function<SelectStatementProvider, R> mapper,
                                  BasicColumn[] selectList, SqlTable table, SelectDSLCompleter completer) {
        return mapper.apply(select(selectList, table, completer));
    }

    public static <R> R selectOne(Function<SelectStatementProvider, R> mapper,
                                  SelectDSL start, SelectDSLCompleter completer) {
        return mapper.apply(select(start, completer));
    }

    //@Deprecated
    public static UpdateStatementProvider update(SqlTable table,
                                                 org.mybatis.dynamic.sql.update.UpdateDSLCompleter completer) {
        return completer.apply(org.mybatis.dynamic.sql.update.UpdateDSL.update(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    //@Deprecated
    public static int update(ToIntFunction<UpdateStatementProvider> mapper,
                             SqlTable table,
                             org.mybatis.dynamic.sql.update.UpdateDSLCompleter completer) {
        return mapper.applyAsInt(update(table, completer));
    }

    public static UpdateStatementProvider update(SqlTable table, UpdateDSLCompleter completer) {
        return completer.apply(UpdateDSL.update(table))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    public static int update(ToIntFunction<UpdateStatementProvider> mapper,
                             SqlTable table, UpdateDSLCompleter completer) {
        return mapper.applyAsInt(update(table, completer));
    }
}
