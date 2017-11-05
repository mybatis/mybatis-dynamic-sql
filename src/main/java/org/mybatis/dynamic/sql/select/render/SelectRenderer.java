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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.OrderByModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;

public class SelectRenderer {
    private SelectModel selectModel;
    
    private SelectRenderer(SelectModel selectModel) {
        this.selectModel = Objects.requireNonNull(selectModel);
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        return render(renderingStrategy, new AtomicInteger(1));
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy, AtomicInteger sequence) {
        RenderedQueryExpression rqe = QueryExpressionRenderer.of(selectModel.queryExpression())
                .render(renderingStrategy, sequence);
        
        SelectSupport.Builder builder = new SelectSupport.Builder()
                .withRenderedQueryExpression(rqe);
        
        selectModel.orderByModel().ifPresent(applyOrderBy(builder));
        
        return builder.build();
    }

    private Consumer<OrderByModel> applyOrderBy(SelectSupport.Builder builder) {
        return orderByModel -> applyOrderBy(builder, orderByModel);
    }
    
    private void applyOrderBy(SelectSupport.Builder builder, OrderByModel orderByModel) {
        String orderByClause = orderByModel.mapColumns(this::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        builder.withOrderByClause(orderByClause);
    }
    
    private String orderByPhrase(SqlColumn<?> column) {
        String phrase = column.alias().orElse(applyTableAlias(column));
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
    
    private String applyTableAlias(SqlColumn<?> column) {
        return column.applyTableAliasToName(selectModel.queryExpression().tableAliasCalculator());
    }
    
    public static SelectRenderer of(SelectModel selectModel) {
        return new SelectRenderer(selectModel);
    }
}
