/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public abstract class AbstractSelectListItem<T extends AbstractSelectListItem<T>> implements SelectListItem {
    protected SqlColumn<?> column;
    protected Optional<String> alias = Optional.empty();

    protected AbstractSelectListItem(SqlColumn<?> column) {
        this.column = Objects.requireNonNull(column);
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    @Override
    public String applyTableAliasToName(TableAliasCalculator tableAliasCalculator) {
        return render(column.applyTableAliasToName(tableAliasCalculator));
    }
    
    public T as(String alias) {
        T copy = copy();
        copy.alias = Optional.of(alias);
        return copy;
    }

    protected abstract T copy();
    
    /**
     * Return the rendered string for the select list.
     * 
     * @param columnName the calculated column name.  It will have the table alias already applied
     *   if applicable.
     * @return
     */
    protected abstract String render(String columnName);
}
