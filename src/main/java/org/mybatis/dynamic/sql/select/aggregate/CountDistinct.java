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
package org.mybatis.dynamic.sql.select.aggregate;

import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class CountDistinct extends AbstractCount {

    private final BasicColumn column;

    private CountDistinct(BasicColumn column) {
        this.column = Objects.requireNonNull(column);
    }

    private CountDistinct(BasicColumn column, String alias) {
        super(alias);
        this.column = Objects.requireNonNull(column);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "count(distinct " + column.renderWithTableAlias(tableAliasCalculator) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public CountDistinct as(String alias) {
        return new CountDistinct(column, alias);
    }

    public static CountDistinct of(BasicColumn column) {
        return new CountDistinct(column);
    }
}
