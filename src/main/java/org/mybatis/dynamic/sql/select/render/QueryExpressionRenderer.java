/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.render.TableAliasCalculatorWithParent;
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
    private final TableAliasCalculator tableAliasCalculator;

    private QueryExpressionRenderer(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
        tableAliasCalculator = calculateTableAliasCalculator(queryExpression, builder.parentTableAliasCalculator);
        tableExpressionRenderer = new TableExpressionRenderer.Builder()
                .withTableAliasCalculator(tableAliasCalculator)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build();
    }

    /**
     * This function calculates a table alias calculator to use in the current context. There are several
     * possibilities: this could be a renderer for a top level select statement, or it could be a renderer for a table
     * expression in a join, or a column to sub query where condition, or it could be a renderer for a select
     * statement in an "exists" condition in a where clause.
     *
     * <p>In the case of conditions in a where clause, we will have a parent table alias calculator. This will give
     * visibility to the aliases in the outer select statement to this renderer so columns in aliased tables can be
     * used in where clause sub query conditions without having to re-specify the alias.
     *
     * <p>Another complication is that we calculate aliases differently if there are joins and sub queries. The
     * cases are as follows:
     *
     * <ol>
     *     <li>If there are no joins, then we will only use aliases that are explicitly set by the user</li>
     *     <lI>If there are joins and sub queries, we will also only use explicit aliases</lI>
     *     <li>If there are joins, but no sub queries, then we will automatically use the table name
     *     as an alias if no explicit alias has been specified</li>
     * </ol>
     *
     * @param queryExpression the model to render
     * @param parentTableAliasCalculator table alias calculator from the parent query
     * @return a table alias calculator appropriate for this context
     */
    private TableAliasCalculator calculateTableAliasCalculator(QueryExpressionModel queryExpression,
                                                               TableAliasCalculator parentTableAliasCalculator) {
        TableAliasCalculator baseTableAliasCalculator = queryExpression.joinModel()
                .map(JoinModel::containsSubQueries)
                .map(this::calculateTableAliasCalculatorWithJoins)
                .orElseGet(this::explicitTableAliasCalculator);

        if (parentTableAliasCalculator == null) {
            return baseTableAliasCalculator;
        } else {
            return new TableAliasCalculatorWithParent.Builder()
                    .withParent(parentTableAliasCalculator)
                    .withChild(baseTableAliasCalculator)
                    .build();
        }
    }

    private TableAliasCalculator calculateTableAliasCalculatorWithJoins(boolean hasSubQueries) {
        if (hasSubQueries) {
            // if there are subqueries, we cannot use the table name automatically
            // so all aliases must be specified
            return explicitTableAliasCalculator();
        } else {
            // without subqueries, we can automatically use table names as aliases
            return guaranteedTableAliasCalculator();
        }
    }

    private TableAliasCalculator explicitTableAliasCalculator() {
        return ExplicitTableAliasCalculator.of(queryExpression.tableAliases());
    }

    private TableAliasCalculator guaranteedTableAliasCalculator() {
        return GuaranteedTableAliasCalculator.of(queryExpression.tableAliases());
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
        return selectListItem.renderWithTableAndColumnAlias(tableAliasCalculator);
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
                .withTableExpressionRenderer(tableExpressionRenderer)
                .withTableAliasCalculator(tableAliasCalculator)
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
                .withTableAliasCalculator(tableAliasCalculator)
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
        return column.renderWithTableAlias(tableAliasCalculator);
    }

    public static Builder withQueryExpression(QueryExpressionModel model) {
        return new Builder().withQueryExpression(model);
    }

    public static class Builder extends AbstractQueryRendererBuilder<Builder> {
        private QueryExpressionModel queryExpression;

        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }

        Builder getThis() {
            return this;
        }

        public QueryExpressionRenderer build() {
            return new QueryExpressionRenderer(this);
        }
    }
}
