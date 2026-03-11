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
import org.mybatis.dynamic.sql.update.UpdateModel;

public class UpdateDSL extends AbstractUpdateDSL<UpdateModel, UpdateDSL> {
    private UpdateDSL(SqlTable table, @Nullable String tableAlias) {
        super(table, tableAlias);
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    @Override
    public UpdateModel build() {
        return buildUpdateModel();
    }

    @Override
    protected UpdateDSL getThis() {
        return this;
    }

    public static UpdateDSL update(SqlTable table, String tableAlias) {
        return new UpdateDSL(table, tableAlias);
    }

    public static UpdateDSL update(SqlTable table) {
        return new UpdateDSL(table, null);
    }
}
