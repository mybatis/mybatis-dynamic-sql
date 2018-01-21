/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.select.function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class Upper extends AbstractFunction<String, Upper> {
    
    private Upper(BindableColumn<String> column) {
        super(column);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "upper(" //$NON-NLS-1$
                + column.renderWithTableAlias(tableAliasCalculator)
                + ")"; //$NON-NLS-1$
    }

    @Override
    protected Upper copy() {
        return new Upper(column);
    }

    public static Upper of(BindableColumn<String> column) {
        return new Upper(column);
    }
}
