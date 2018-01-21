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

public class Substring extends AbstractFunction<String, Substring> {

    private int offset;
    private int length;
    
    private Substring(BindableColumn<String> column, int offset, int length) {
        super(column);
        this.offset = offset;
        this.length = length;
    }
    
    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "substring(" //$NON-NLS-1$
                + column.renderWithTableAlias(tableAliasCalculator)
                + ", " //$NON-NLS-1$
                + offset
                + ", " //$NON-NLS-1$
                + length
                + ")"; //$NON-NLS-1$
    }
    
    @Override
    protected Substring copy() {
        return new Substring(column, offset, length);
    }
    
    public static Substring of(BindableColumn<String> column, int offset, int length) {
        return new Substring(column, offset, length);
    }
}
