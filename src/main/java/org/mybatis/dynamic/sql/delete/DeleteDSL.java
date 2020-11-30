/*
 *    Copyright 2016-2020 the original author or authors.
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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;
import org.mybatis.dynamic.sql.where.AbstractWhereSupport;
import org.mybatis.dynamic.sql.where.WhereModel;

public class DeleteDSL<R> extends AbstractWhereSupport<DeleteDSL<R>.DeleteWhereBuilder> implements Buildable<R> {

    private final Function<DeleteModel, R> adapterFunction;
    private final SqlTable table;
    private final DeleteWhereBuilder whereBuilder = new DeleteWhereBuilder();

    private DeleteDSL(SqlTable table, Function<DeleteModel, R> adapterFunction) {
        this.table = Objects.requireNonNull(table);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    @Override
    public DeleteWhereBuilder where() {
        return whereBuilder;
    }

    /**
     * WARNING! Calling this method could result in an delete statement that deletes
     * all rows in a table.
     *
     * @return the model class
     */
    @NotNull
    @Override
    public R build() {
        DeleteModel deleteModel = DeleteModel.withTable(table)
                .withWhereModel(whereBuilder.buildWhereModel())
                .build();
        return adapterFunction.apply(deleteModel);
    }

    public static <R> DeleteDSL<R> deleteFrom(Function<DeleteModel, R> adapterFunction, SqlTable table) {
        return new DeleteDSL<>(table, adapterFunction);
    }

    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return deleteFrom(Function.identity(), table);
    }

    public class DeleteWhereBuilder extends AbstractWhereDSL<DeleteWhereBuilder> implements Buildable<R> {

        private DeleteWhereBuilder() {
            super();
        }

        @NotNull
        @Override
        public R build() {
            return DeleteDSL.this.build();
        }

        @Override
        protected DeleteWhereBuilder getThis() {
            return this;
        }

        @Override
        protected WhereModel buildWhereModel() {
            return super.internalBuild();
        }
    }
}
