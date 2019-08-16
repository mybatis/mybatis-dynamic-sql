/**
 *    Copyright 2016-2019 the original author or authors.
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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceAfter;
import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class QueryExpressionRenderer {
    private QueryExpressionModel queryExpression;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    
    private QueryExpressionRenderer(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
    }
    
    public FragmentAndParameters render() {
        return queryExpression.whereModel()
                .flatMap(this::renderWhereClause)
                .map(this::renderWithWhereClause)
                .orElseGet(this::renderWithoutWhereClause);
    }
    
    private FragmentAndParameters renderWithWhereClause(WhereClauseProvider whereClause) {
        return FragmentAndParameters.withFragment(calculateQueryExpression(whereClause))
                .withParameters(whereClause.getParameters())
                .build();
    }
    
    private FragmentAndParameters renderWithoutWhereClause() {
        return FragmentAndParameters.withFragment(calculateQueryExpression())
                .build();
    }

    private String calculateQueryExpression() {
        return calculateQueryExpressionStart()
                + spaceBefore(queryExpression.groupByModel().map(this::renderGroupBy));
    }

    private String calculateQueryExpression(WhereClauseProvider whereClause) {
        return calculateQueryExpressionStart()
                + spaceBefore(whereClause.getWhereClause())
                + spaceBefore(queryExpression.groupByModel().map(this::renderGroupBy));
    }

    private String calculateQueryExpressionStart() {
        return spaceAfter(queryExpression.connector())
                + "select " //$NON-NLS-1$
                + (queryExpression.isDistinct() ? "distinct " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + calculateColumnList()
                + " from " //$NON-NLS-1$
                + calculateTableName(queryExpression.table())
                + spaceBefore(queryExpression.joinModel().map(this::renderJoin));
    }
    
    private String calculateColumnList() {
        return queryExpression.mapColumns(this::applyTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private String calculateTableName(SqlTable table) {
        return queryExpression.calculateTableNameIncludingAlias(table);
    }
    
    private String applyTableAndColumnAlias(BasicColumn selectListItem) {
        return selectListItem.renderWithTableAndColumnAlias(queryExpression.tableAliasCalculator());
    }
    
    private String renderJoin(JoinModel joinModel) {
        return JoinRenderer.withJoinModel(joinModel)
                .withQueryExpression(queryExpression)
                .build()
                .render();
    }
    
    private Optional<WhereClauseProvider> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
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
    
    private String applyTableAlias(BasicColumn column) {
        return column.renderWithTableAlias(queryExpression.tableAliasCalculator());
    }
    
    public static Builder withQueryExpression(QueryExpressionModel model) {
        return new Builder().withQueryExpression(model);
    }
    
    public static class Builder {
        private QueryExpressionModel queryExpression;
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        
        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
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
