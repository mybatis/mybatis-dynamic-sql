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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.where.WhereModel;

public class SelectModel {
    private boolean isDistinct;
    private List<SelectListItem> selectList = new ArrayList<>();
    private SqlTable table;
    private Optional<JoinModel> joinModel;
    private Map<SqlTable, String> tableAliases = new HashMap<>();
    private Optional<WhereModel> whereModel;
    private Optional<List<SqlColumn<?>>> orderByColumns;

    private SelectModel(Builder builder) {
        isDistinct = builder.isDistinct;
        selectList.addAll(builder.selectList);
        table = builder.table;
        joinModel = Optional.ofNullable(builder.joinModel);
        tableAliases.putAll(builder.tableAliases);
        whereModel = Optional.ofNullable(builder.whereModel);
        orderByColumns = Optional.ofNullable(builder.orderByColumns);
    }
    
    public boolean isDistinct() {
        return isDistinct;
    }
    
    public <R> Stream<R> mapColumns(Function<SelectListItem, R> mapper) {
        return selectList.stream().map(mapper);
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<String> tableAlias(Optional<SqlTable> table) {
        return table.map(tableAliases::get);
    }
    
    public Map<SqlTable, String> tableAliases() {
        return tableAliases;
    }

    public Optional<WhereModel> whereModel() {
        return whereModel;
    }
    
    public Optional<JoinModel> joinModel() {
        return joinModel;
    }
    
    public <R> Optional<Stream<R>> mapOrderByColumns(Function<SqlColumn<?>, R> mapper) {
        return orderByColumns.map(sc -> sc.stream().map(mapper));
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        return SelectRenderer.of(this).render(renderingStrategy);
    }
    
    public static class Builder {
        private boolean isDistinct;
        private List<SelectListItem> selectList = new ArrayList<>();
        private SqlTable table;
        private Map<SqlTable, String> tableAliases = new HashMap<>();
        private WhereModel whereModel;
        private List<SqlColumn<?>> orderByColumns;
        private JoinModel joinModel;
        
        public Builder(SqlTable table) {
            this.table = table;
        }
        
        public Builder isDistinct(boolean isDistinct) {
            this.isDistinct = isDistinct;
            return this;
        }

        public Builder withColumns(List<SelectListItem> selectList) {
            this.selectList.addAll(selectList);
            return this;
        }

        public Builder withTableAliases(Map<SqlTable, String> tableAliases) {
            this.tableAliases.putAll(tableAliases);
            return this;
        }
        
        public Builder withWhereModel(WhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }

        public Builder withOrderByColumns(List<SqlColumn<?>> columns) {
            orderByColumns = columns;
            return this;
        }
        
        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }
        
        public SelectModel build() {
            return new SelectModel(this);
        }
    }
}
