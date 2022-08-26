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

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.function.AbstractUniTypeFunction;

public class Sum<T> extends AbstractUniTypeFunction<T, Sum<T>> {

    private Sum(BindableColumn<T> column) {
        super(column);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "sum(" + column.renderWithTableAlias(tableAliasCalculator) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Sum<T> copy() {
        return new Sum<>(column);
    }

    public static <T> Sum<T> of(BindableColumn<T> column) {
        return new Sum<>(column);
    }
}
