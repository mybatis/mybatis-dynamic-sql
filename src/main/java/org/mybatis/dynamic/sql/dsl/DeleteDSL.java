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
package org.mybatis.dynamic.sql.dsl;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.DeleteModel;

public class DeleteDSL extends AbstractDeleteDSL<DeleteModel, DeleteDSL> {

    private DeleteDSL(SqlTable table, @Nullable String tableAlias) {
        super(table, tableAlias);
    }

    /**
     * WARNING! Calling this method could result in a delete statement that deletes
     * all rows in a table.
     *
     * @return the model class
     */
    @Override
    public DeleteModel build() {
        return buildDeleteModel();
    }

    @Override
    protected DeleteDSL getThis() {
        return this;
    }

    public static DeleteDSL deleteFrom(SqlTable table, String tableAlias) {
        return new DeleteDSL(table, tableAlias);
    }

    public static DeleteDSL deleteFrom(SqlTable table) {
        return new DeleteDSL(table, null);
    }
}
