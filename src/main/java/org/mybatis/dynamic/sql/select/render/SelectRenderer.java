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
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class SelectRenderer {
    private SelectModel selectModel;
    
    private SelectRenderer(SelectModel selectModel) {
        this.selectModel = Objects.requireNonNull(selectModel);
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        return render(renderingStrategy, new AtomicInteger(1));
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy, AtomicInteger sequence) {
        SelectSupport.Builder builder = new SelectSupport.Builder()
                .isDistinct(selectModel.isDistinct())
                .withColumnList(calculateColumnList())
                .withTableName(calculateTableName(selectModel.table()));
        
        selectModel.joinModel().ifPresent(jm -> applyJoin(builder, jm));
        selectModel.whereModel().ifPresent(wm -> applyWhere(builder, wm, renderingStrategy, sequence));
        selectModel.orderByColumns().ifPresent(cs -> applyOrderBy(builder, cs));
        
        return builder.build();
    }

    private String calculateColumnList() {
        return selectModel.columns()
                .map(this::nameIncludingTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private String calculateTableName(SqlTable table) {
        return RenderingUtilities.tableNameIncludingAlias(table, selectModel.tableAliases());
    }
    
    private String nameIncludingTableAndColumnAlias(SelectListItem selectListItem) {
        StringBuilder buffer = new StringBuilder(calculateColumnNameAndTableAlias(selectListItem));
        selectListItem.alias().ifPresent(a -> {
            buffer.append(" as "); //$NON-NLS-1$
            buffer.append(a);
        });
        
        return buffer.toString();
    }
    
    private String calculateColumnNameAndTableAlias(SelectListItem selectListItem) {
        return selectListItem.nameIncludingTableAlias(selectModel.tableAlias(selectListItem.table()));
    }
    
    private void applyJoin(SelectSupport.Builder builder, JoinModel joinModel) {
        String joinClause = JoinRenderer.of(joinModel, selectModel.tableAliases())
                .render();
        
        builder.withJoinClause(joinClause);
    }
    
    private void applyWhere(SelectSupport.Builder builder, WhereModel whereModel, RenderingStrategy renderingStrategy,
            AtomicInteger sequence) {
        WhereSupport whereSupport = new WhereRenderer.Builder()
                .withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withTableAliases(selectModel.tableAliases())
                .withSequence(sequence)
                .build()
                .render();
        
        builder.withWhereClause(whereSupport.getWhereClause());
        builder.withParameters(whereSupport.getParameters());
    }
    
    private void applyOrderBy(SelectSupport.Builder builder, Stream<SqlColumn<?>> columns) {
        String orderByClause = columns.map(this::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        builder.withOrderByClause(orderByClause);
    }
    
    private String orderByPhrase(SqlColumn<?> column) {
        String phrase = column.alias().orElse(calculateColumnNameAndTableAlias(column));
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
    
    public static SelectRenderer of(SelectModel selectModel) {
        return new SelectRenderer(selectModel);
    }
}
