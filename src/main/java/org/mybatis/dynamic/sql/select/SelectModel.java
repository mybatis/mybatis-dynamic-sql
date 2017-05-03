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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.WhereModel;

public class SelectModel {
    private boolean isDistinct;
    private List<SqlColumn<?>> columns = new ArrayList<>();
    private SqlTable table;
    private WhereModel whereModel;
    private List<SqlColumn<?>> orderByColumns = new ArrayList<>();

    private SelectModel() {
        super();
    }
    
    public boolean isDistinct() {
        return isDistinct;
    }
    
    public Stream<SqlColumn<?>> columns() {
        return columns.stream();
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<WhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }
    
    public Stream<SqlColumn<?>> orderByColumns() {
        return orderByColumns.stream();
    }
    
    public static class Builder {
        private SelectModel selectModel = new SelectModel();
        
        public Builder isDistinct(boolean isDistinct) {
            selectModel.isDistinct = isDistinct;
            return this;
        }

        public Builder withColumns(SqlColumn<?>[] columns) {
            selectModel.columns.addAll(Arrays.asList(columns));
            return this;
        }

        public Builder withTable(SqlTable table) {
            selectModel.table = table;
            return this;
        }

        public Builder withWhereModel(WhereModel whereModel) {
            selectModel.whereModel = whereModel;
            return this;
        }

        public Builder withOrderByColumns(SqlColumn<?>[] columns) {
            if (columns != null) {
                selectModel.orderByColumns.addAll(Arrays.asList(columns));
            }
            return this;
        }
        
        public SelectModel build() {
            return selectModel;
        }
    }
}
