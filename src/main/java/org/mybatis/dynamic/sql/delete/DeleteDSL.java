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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.dsl.AbstractDeleteDSL;

public class DeleteDSL<R> extends AbstractDeleteDSL<R, DeleteDSL<R>> {

    private final Function<DeleteModel, R> adapterFunction;

    private DeleteDSL(SqlTable table, @Nullable String tableAlias, Function<DeleteModel, R> adapterFunction) {
        super(table, tableAlias);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }


    /**
     * WARNING! Calling this method could result in a delete statement that deletes
     * all rows in a table.
     *
     * @return the model class
     */
    @Override
    public R build() {
        return buildDeleteModel().map(adapterFunction);
    }

    @Override
    protected DeleteDSL<R> getThis() {
        return this;
    }

    public static <R> DeleteDSL<R> deleteFrom(Function<DeleteModel, R> adapterFunction, SqlTable table,
                                              @Nullable String tableAlias) {
        return new DeleteDSL<>(table, tableAlias, adapterFunction);
    }

    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table) {
        return deleteFrom(Function.identity(), table, null);
    }

    public static DeleteDSL<DeleteModel> deleteFrom(SqlTable table, String tableAlias) {
        return deleteFrom(Function.identity(), table, tableAlias);
    }
}
