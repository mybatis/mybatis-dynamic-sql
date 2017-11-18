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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.where.WhereModel;

public class QueryExpressionModel {
    private Optional<String> connector;
    private boolean isDistinct;
    private List<SelectListItem> selectList;
    private SqlTable table;
    private Optional<JoinModel> joinModel;
    private TableAliasCalculator tableAliasCalculator;
    private Optional<WhereModel> whereModel;
    private Optional<GroupByModel> groupByModel;

    private QueryExpressionModel(Builder builder) {
        connector = Optional.ofNullable(builder.connector);
        isDistinct = builder.isDistinct;
        selectList = Objects.requireNonNull(builder.selectList);
        table = Objects.requireNonNull(builder.table);
        joinModel = Optional.ofNullable(builder.joinModel);
        tableAliasCalculator = joinModel.map(jm -> GuaranteedTableAliasCalculator.of(builder.tableAliases))
                .orElse(TableAliasCalculator.of(builder.tableAliases));
        whereModel = Optional.ofNullable(builder.whereModel);
        groupByModel = Optional.ofNullable(builder.groupByModel);
    }
    
    public Optional<String> connector() {
        return connector;
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
    
    public TableAliasCalculator tableAliasCalculator() {
        return tableAliasCalculator;
    }

    public Optional<WhereModel> whereModel() {
        return whereModel;
    }
    
    public Optional<JoinModel> joinModel() {
        return joinModel;
    }
    
    public Optional<GroupByModel> groupByModel() {
        return groupByModel;
    }
    
    public String calculateTableNameIncludingAlias(SqlTable table) {
        return table.name()
                + spaceBefore(tableAliasCalculator.aliasForTable(table));
    }
    
    public static class Builder {
        private String connector;
        private boolean isDistinct;
        private List<SelectListItem> selectList = new ArrayList<>();
        private SqlTable table;
        private Map<SqlTable, String> tableAliases = new HashMap<>();
        private WhereModel whereModel;
        private JoinModel joinModel;
        private GroupByModel groupByModel;
        
        public Builder withConnector(String connector) {
            this.connector = connector;
            return this;
        }
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
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

        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }
        
        public Builder withGroupByModel(GroupByModel groupByModel) {
            this.groupByModel = groupByModel;
            return this;
        }
        
        public QueryExpressionModel build() {
            return new QueryExpressionModel(this);
        }
    }
}
