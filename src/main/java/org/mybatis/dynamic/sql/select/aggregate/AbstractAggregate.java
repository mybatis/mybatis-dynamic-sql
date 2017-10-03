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
package org.mybatis.dynamic.sql.select.aggregate;

import java.util.Optional;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public abstract class AbstractAggregate<S, T extends AbstractAggregate<S, T>> implements SelectListItem {
    protected SqlColumn<S> column;
    protected Optional<String> alias = Optional.empty();

    protected AbstractAggregate(SqlColumn<S> column) {
        this.column = column;
    }

    @Override
    public Optional<SqlTable> table() {
        return column.table();
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    @Override
    public String nameIncludingTableAlias(Optional<String> tableAlias) {
        return render(column.nameIncludingTableAlias(tableAlias));
    }

    public T as(String alias) {
        T copy = copy();
        copy.alias = Optional.of(alias);
        return copy;
    }

    protected abstract T copy();
    protected abstract String render(String columnName);
}
