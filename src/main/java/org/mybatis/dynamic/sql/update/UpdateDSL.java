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
package org.mybatis.dynamic.sql.update;

import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.dsl.AbstractUpdateDSL;

public class UpdateDSL<R> extends AbstractUpdateDSL<R, UpdateDSL<R>> {

    private final Function<UpdateModel, R> adapterFunction;

    private UpdateDSL(SqlTable table, @Nullable String tableAlias, Function<UpdateModel, R> adapterFunction) {
        super(table, tableAlias);
        this.adapterFunction = Objects.requireNonNull(adapterFunction);
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    @Override
    public R build() {
        return buildUpdateModel().map(adapterFunction);
    }

    @Override
    protected UpdateDSL<R> getThis() {
        return this;
    }

    public static <R> UpdateDSL<R> update(Function<UpdateModel, R> adapterFunction, SqlTable table,
                                          @Nullable String tableAlias) {
        return new UpdateDSL<>(table, tableAlias, adapterFunction);
    }

    public static UpdateDSL<UpdateModel> update(SqlTable table) {
        return update(Function.identity(), table, null);
    }

    public static UpdateDSL<UpdateModel> update(SqlTable table, String tableAlias) {
        return update(Function.identity(), table, tableAlias);
    }
}
