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

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.SelectModel;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long value. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @author Jeff Butler
 */
public class CountDSL extends AbstractCountDSL<SelectModel, CountDSL> {
    private CountDSL(BasicColumn column) {
        super(column);
    }

    @Override
    protected CountDSL getThis() {
        return this;
    }

    @Override
    public SelectModel build() {
        return buildSelectModel();
    }

    public static CountDSL countFrom(SqlTable table) {
        return new CountDSL(SqlBuilder.count()).from(table);
    }

    public static CountDSL countFrom(SqlTable table, String tableAlias) {
        return new CountDSL(SqlBuilder.count()).from(table, tableAlias);
    }

    public static CountDSL count(BasicColumn column) {
        return new CountDSL(SqlBuilder.count(column));
    }

    public static CountDSL countDistinct(BasicColumn column) {
        return new CountDSL(SqlBuilder.countDistinct(column));
    }
}
