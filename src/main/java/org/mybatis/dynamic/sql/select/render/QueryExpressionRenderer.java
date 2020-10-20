/*
 *    Copyright 2016-2020 the original author or authors.
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
import org.mybatis.dynamic.sql.TableExpression;
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
    private final QueryExpressionModel queryExpression;
    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;
    private final TableExpressionRenderer tableExpressionRenderer;

    private QueryExpressionRenderer(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
        tableExpressionRenderer = new TableExpressionRenderer.Builder()
                .withTableAliasCalculator(queryExpression.tableAliasCalculator())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build();
    }

    public FragmentAndParameters render() {
        FragmentAndParameters answer = calculateQueryExpressionStart();
        answer = addJoinClause(answer);
        answer = addWhereClause(answer);
        answer = addGroupByClause(answer);
        return answer;
    }

    private FragmentAndParameters calculateQueryExpressionStart() {
        String start = spaceAfter(queryExpression.connector())
                + "select " //$NON-NLS-1$
                + (queryExpression.isDistinct() ? "distinct " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + calculateColumnList()
                + " from "; //$NON-NLS-1$

        FragmentAndParameters renderedTable = renderTableExpression(queryExpression.table());
        start += renderedTable.fragment();

        return FragmentAndParameters.withFragment(start)
                .withParameters(renderedTable.parameters())
                .build();
    }

    private String calculateColumnList() {
        return queryExpression.mapColumns(this::applyTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private String applyTableAndColumnAlias(BasicColumn selectListItem) {
        return selectListItem.renderWithTableAndColumnAlias(queryExpression.tableAliasCalculator());
    }

    private FragmentAndParameters renderTableExpression(TableExpression table) {
        return table.accept(tableExpressionRenderer);
    }

    private FragmentAndParameters addJoinClause(FragmentAndParameters partial) {
        return queryExpression.joinModel()
                .map(this::renderJoin)
                .map(fp -> partial.add(spaceBefore(fp.fragment()), fp.parameters()))
                .orElse(partial);
    }

    private FragmentAndParameters renderJoin(JoinModel joinModel) {
        return JoinRenderer.withJoinModel(joinModel)
                .withQueryExpression(queryExpression)
                .withTableExpressionRenderer(tableExpressionRenderer)
                .build()
                .render();
    }

    private FragmentAndParameters addWhereClause(FragmentAndParameters partial) {
        return queryExpression.whereModel()
                .flatMap(this::renderWhereClause)
                .map(wc -> partial.add(spaceBefore(wc.getWhereClause()), wc.getParameters()))
                .orElse(partial);
    }

    private Optional<WhereClauseProvider> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(queryExpression.tableAliasCalculator())
                .withSequence(sequence)
                .build()
                .render();
    }

    private FragmentAndParameters addGroupByClause(FragmentAndParameters partial) {
        return queryExpression.groupByModel()
                .map(this::renderGroupBy)
                .map(s -> partial.add(spaceBefore(s)))
                .orElse(partial);
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
