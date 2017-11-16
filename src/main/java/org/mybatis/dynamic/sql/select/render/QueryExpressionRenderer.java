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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.QueryExpression;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class QueryExpressionRenderer {
    private QueryExpression queryExpression;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    
    private QueryExpressionRenderer(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
    }
    
    public RenderedQueryExpression render() {
        return new RenderedQueryExpression.Builder()
                .withConnector(queryExpression.connector())
                .isDistinct(queryExpression.isDistinct())
                .withColumnList(calculateColumnList())
                .withTableName(calculateTableName(queryExpression.table()))
                .withJoinClause(queryExpression.joinModel().map(this::renderJoin))
                .withWhereSupport(queryExpression.whereModel().map(this::renderWhere))
                .withGroupByClause(queryExpression.groupByModel().map(this::renderGroupBy))
                .build();
    }

    private String calculateColumnList() {
        return queryExpression.mapColumns(this::applyTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private String calculateTableName(SqlTable table) {
        return queryExpression.calculateTableNameIncludingAlias(table);
    }
    
    private String applyTableAndColumnAlias(SelectListItem selectListItem) {
        return selectListItem.applyTableAndColumnAliasToName(queryExpression.tableAliasCalculator());
    }
    
    private String renderJoin(JoinModel joinModel) {
        return new JoinRenderer.Builder()
                .withJoinModel(joinModel)
                .withQueryExpression(queryExpression)
                .build()
                .render();
    }
    
    private WhereSupport renderWhere(WhereModel whereModel) {
        return new WhereRenderer.Builder()
                .withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(queryExpression.tableAliasCalculator())
                .withSequence(sequence)
                .build()
                .render();
    }

    private String renderGroupBy(GroupByModel groupByModel) {
        return groupByModel.mapColumns(this::applyTableAlias)
                .collect(CustomCollectors.joining(", ", "group by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String applyTableAlias(SelectListItem column) {
        return column.applyTableAliasToName(queryExpression.tableAliasCalculator());
    }
    
    public static class Builder {
        private QueryExpression queryExpression;
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        
        public Builder withQueryExpression(QueryExpression queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public QueryExpressionRenderer build() {
            return new QueryExpressionRenderer(this);
        }
    }
}
