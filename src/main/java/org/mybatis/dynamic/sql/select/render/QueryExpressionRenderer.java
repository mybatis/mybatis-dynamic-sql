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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.QueryExpression;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class QueryExpressionRenderer {
    private QueryExpression queryExpression;
    
    private QueryExpressionRenderer(QueryExpression queryExpression) {
        this.queryExpression = Objects.requireNonNull(queryExpression);
    }
    
    public RenderedQueryExpression render(RenderingStrategy renderingStrategy, AtomicInteger sequence) {
        RenderedQueryExpression.Builder builder = new RenderedQueryExpression.Builder()
                .isDistinct(queryExpression.isDistinct())
                .withColumnList(calculateColumnList())
                .withTableName(calculateTableName(queryExpression.table()));
        
        queryExpression.joinModel().ifPresent(applyJoin(builder));
        queryExpression.whereModel().ifPresent(applyWhere(builder, renderingStrategy, sequence));
        
        return builder.build();
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
    
    private Consumer<JoinModel> applyJoin(RenderedQueryExpression.Builder builder) {
        return joinModel -> applyJoin(builder, joinModel);
    }
    
    private void applyJoin(RenderedQueryExpression.Builder builder, JoinModel joinModel) {
        String joinClause = new JoinRenderer.Builder()
                .withJoinModel(joinModel)
                .withQueryExpression(queryExpression)
                .build()
                .render();
        
        builder.withJoinClause(joinClause);
    }
    
    private Consumer<WhereModel> applyWhere(RenderedQueryExpression.Builder builder, RenderingStrategy renderingStrategy,
            AtomicInteger sequence) {
        return whereModel -> applyWhere(builder, renderingStrategy, sequence, whereModel);
    }
    
    private void applyWhere(RenderedQueryExpression.Builder builder, RenderingStrategy renderingStrategy,
            AtomicInteger sequence, WhereModel whereModel) {
        WhereSupport whereSupport = new WhereRenderer.Builder()
                .withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliasCalculator(queryExpression.tableAliasCalculator())
                .withSequence(sequence)
                .build()
                .render();
        
        builder.withWhereClause(whereSupport.getWhereClause());
        builder.withParameters(whereSupport.getParameters());
    }
    
    public static QueryExpressionRenderer of(QueryExpression queryExpression) {
        return new QueryExpressionRenderer(queryExpression);
    }
}
