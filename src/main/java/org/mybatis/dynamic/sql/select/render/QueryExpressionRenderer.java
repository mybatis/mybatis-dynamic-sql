/*
 *    Copyright 2016-2023 the original author or authors.
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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.render.TableAliasCalculatorWithParent;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.HavingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class QueryExpressionRenderer {
    private final QueryExpressionModel queryExpression;
    private final TableExpressionRenderer tableExpressionRenderer;
    private final RenderingContext renderingContext;

    private QueryExpressionRenderer(Builder builder) {
        queryExpression = Objects.requireNonNull(builder.queryExpression);
        TableAliasCalculator tableAliasCalculator =
                calculateTableAliasCalculator(queryExpression, builder.renderingContext.tableAliasCalculator());

        renderingContext = new RenderingContext.Builder()
                .withRenderingStrategy(Objects.requireNonNull(builder.renderingContext.renderingStrategy()))
                .withSequence(builder.renderingContext.sequence())
                .withTableAliasCalculator(tableAliasCalculator)
                .build();

        tableExpressionRenderer = new TableExpressionRenderer.Builder()
                .withRenderingContext(renderingContext)
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

        return new TableAliasCalculatorWithParent.Builder()
                .withParent(parentTableAliasCalculator)
                .withChild(baseTableAliasCalculator)
                .build();
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
        FragmentCollector fragmentCollector = new FragmentCollector();

        fragmentCollector.add(calculateQueryExpressionStart());
        calculateJoinClause().ifPresent(fragmentCollector::add);
        calculateWhereClause().ifPresent(fragmentCollector::add);
        calculateGroupByClause().ifPresent(fragmentCollector::add);
        calculateHavingClause().ifPresent(fragmentCollector::add);

        return toFragmentAndParameters(fragmentCollector);
    }

    private FragmentAndParameters toFragmentAndParameters(FragmentCollector fragmentCollector) {
        return FragmentAndParameters
                .withFragment(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters calculateQueryExpressionStart() {
        FragmentAndParameters columnList = calculateColumnList();

        String start = queryExpression.connector().map(StringUtilities::spaceAfter).orElse("") //$NON-NLS-1$
                + "select " //$NON-NLS-1$
                + (queryExpression.isDistinct() ? "distinct " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + columnList.fragment()
                + " from "; //$NON-NLS-1$

        FragmentAndParameters renderedTable = renderTableExpression(queryExpression.table());
        start += renderedTable.fragment();

        return FragmentAndParameters.withFragment(start)
                .withParameters(renderedTable.parameters())
                .withParameters(columnList.parameters())
                .build();
    }

    private FragmentAndParameters calculateColumnList() {
        FragmentCollector fc = queryExpression.mapColumns(this::applyTableAndColumnAlias)
                .collect(FragmentCollector.collect());

        String s = fc.collectFragments(Collectors.joining(", ")); //$NON-NLS-1$

        return FragmentAndParameters.withFragment(s)
                .withParameters(fc.parameters())
                .build();
    }

    private FragmentAndParameters applyTableAndColumnAlias(BasicColumn selectListItem) {
        FragmentAndParameters renderedColumn = selectListItem.render(renderingContext);

        String nameAndTableAlias = selectListItem.alias().map(a -> renderedColumn.fragment() + " as " + a) //$NON-NLS-1$
                .orElse(renderedColumn.fragment());

        return FragmentAndParameters.withFragment(nameAndTableAlias)
                .withParameters(renderedColumn.parameters())
                .build();
    }

    private FragmentAndParameters renderTableExpression(TableExpression table) {
        return table.accept(tableExpressionRenderer);
    }

    private Optional<FragmentAndParameters> calculateJoinClause() {
        return queryExpression.joinModel().map(this::renderJoin);
    }

    private FragmentAndParameters renderJoin(JoinModel joinModel) {
        return JoinRenderer.withJoinModel(joinModel)
                .withTableExpressionRenderer(tableExpressionRenderer)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> calculateWhereClause() {
        return queryExpression.whereModel().flatMap(this::renderWhereClause);
    }

    private Optional<FragmentAndParameters> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> calculateGroupByClause() {
        return queryExpression.groupByModel().map(this::renderGroupBy);
    }

    private FragmentAndParameters renderGroupBy(GroupByModel groupByModel) {
        FragmentCollector fc = groupByModel.mapColumns(this::applyTableAlias)
                .collect(FragmentCollector.collect());
        String groupBy = fc.collectFragments(
                Collectors.joining(", ", "group by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)

        return FragmentAndParameters.withFragment(groupBy)
                .withParameters(fc.parameters())
                .build();
    }

    private Optional<FragmentAndParameters> calculateHavingClause() {
        return queryExpression.havingModel().flatMap(this::renderHavingClause);
    }

    private Optional<FragmentAndParameters> renderHavingClause(HavingModel havingModel) {
        return HavingRenderer.withHavingModel(havingModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    private FragmentAndParameters applyTableAlias(BasicColumn column) {
        return column.render(renderingContext);
    }

    public static Builder withQueryExpression(QueryExpressionModel model) {
        return new Builder().withQueryExpression(model);
    }

    public static class Builder {
        private QueryExpressionModel queryExpression;
        private RenderingContext renderingContext;

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
            this.queryExpression = queryExpression;
            return this;
        }

        public QueryExpressionRenderer build() {
            return new QueryExpressionRenderer(this);
        }
    }
}
