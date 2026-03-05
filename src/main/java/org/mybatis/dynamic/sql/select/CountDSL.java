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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.dsl.AbstractCountDSL;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long value. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @param <R> the type of model built by this Builder. Typically, SelectModel.
 *
 * @author Jeff Butler
 */
public class CountDSL<R> extends AbstractCountDSL<R, CountDSL<R>> {
    private final Function<SelectModel, R> adapterFunction;

    private CountDSL(Builder<R> builder) {
        super(Objects.requireNonNull(builder.column));
        adapterFunction = Objects.requireNonNull(builder.adapterFunction);
    }

    @Override
    public R build() {
        return buildSelectModel().map(adapterFunction);
    }

    @Override
    protected CountDSL<R> getThis() {
        return this;
    }

    public static CountDSL<SelectModel> countFrom(SqlTable table) {
        return countFrom(Function.identity(), table);
    }

    public static CountDSL<SelectModel> countFrom(SqlTable table, String tableAlias) {
        return new Builder<SelectModel>()
                .withAdapterFunction(Function.identity())
                .withColumn(SqlBuilder.count())
                .build()
                .from(table, tableAlias);
    }

    public static <R> CountDSL<R> countFrom(Function<SelectModel, R> adapterFunction, SqlTable table) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.count())
                .build()
                .from(table);
    }

    public static CountDSL<SelectModel> count(BasicColumn column) {
        return count(Function.identity(), column);
    }

    public static <R> CountDSL<R> count(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.count(column))
                .build();
    }

    public static CountDSL<SelectModel> countDistinct(BasicColumn column) {
        return countDistinct(Function.identity(), column);
    }

    public static <R> CountDSL<R> countDistinct(Function<SelectModel, R> adapterFunction, BasicColumn column) {
        return new Builder<R>()
                .withAdapterFunction(adapterFunction)
                .withColumn(SqlBuilder.countDistinct(column))
                .build();
    }

    public static class Builder<R> {
        private @Nullable BasicColumn column;
        private @Nullable Function<SelectModel, R> adapterFunction;

        public Builder<R> withColumn(BasicColumn column) {
            this.column = column;
            return this;
        }

        public Builder<R> withAdapterFunction(Function<SelectModel, R> adapterFunction) {
            this.adapterFunction = adapterFunction;
            return this;
        }

        public CountDSL<R> build() {
            return new CountDSL<>(this);
        }
    }
}
