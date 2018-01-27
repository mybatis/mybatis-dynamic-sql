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
package org.mybatis.dynamic.sql.select.aggregate;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

/**
 * This class is the base class for aggregate functions.
 * 
 * @author Jeff Butler
 *
 * @param <T> the subclass type
 */
public abstract class AbstractAggregate<T extends AbstractAggregate<T>> implements BasicColumn {
    protected BasicColumn column;
    protected String alias;

    protected AbstractAggregate(BasicColumn column) {
        this.column = Objects.requireNonNull(column);
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return render(column.renderWithTableAlias(tableAliasCalculator));
    }
    
    @Override
    public T as(String alias) {
        T copy = copy();
        copy.alias = alias;
        return copy;
    }

    protected abstract T copy();
    
    /**
     * Calculate the rendered string for the select list.
     * 
     * @param columnName the calculated column name.  It will have the table alias already applied
     *     if applicable.
     * @return the rendered string for the select list
     */
    protected abstract String render(String columnName);
}
