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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class SelectRenderer {
    private SelectModel selectModel;
    
    private SelectRenderer(SelectModel selectModel) {
        this.selectModel = selectModel;
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        SelectSupport.Builder builder = new SelectSupport.Builder(calculateTableName(selectModel.table()))
                .isDistinct(selectModel.isDistinct())
                .withColumnList(calculateColumnList())
                .withOrderByClause(calculateOrderByPhrase());
        
        selectModel.whereModel().ifPresent(wm -> {
            WhereSupport whereSupport = WhereRenderer.of(wm, renderingStrategy, selectModel.tableAliases()).render();
            builder.withWhereClause(whereSupport.getWhereClause())
                .withParameters(whereSupport.getParameters());
        });
        
        return builder.build();
    }
    
    private String calculateTableName(SqlTable table) {
        return selectModel.tableAlias(Optional.of(table))
                .map(a -> table.name() + " " + a) //$NON-NLS-1$
                .orElse(table.name());
    }
    
    private String calculateColumnList() {
        return selectModel.columns()
                .map(this::nameIncludingTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }
    
    private String nameIncludingTableAndColumnAlias(SqlColumn<?> column) {
        StringBuilder buffer = new StringBuilder(calculateColumnNameAndTableAlias(column));
        column.alias().ifPresent(a -> {
            buffer.append(" as "); //$NON-NLS-1$
            buffer.append(a);
        });
        
        return buffer.toString();
    }
    
    private String calculateColumnNameAndTableAlias(SqlColumn<?> column) {
        return column.nameIncludingTableAlias(selectModel.tableAlias(column.table()));
    }
    
    private Optional<String> calculateOrderByPhrase() {
        return selectModel.orderByColumns()
                .flatMap(c -> Optional.of(calculateOrderByPhrase(c)));
    }
    
    private String calculateOrderByPhrase(Stream<SqlColumn<?>> columns) {
        return columns.map(this::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String orderByPhrase(SqlColumn<?> column) {
        return column.alias().orElse(column.name()) + " " + column.sortOrder(); //$NON-NLS-1$
    }
    
    public static SelectRenderer of(SelectModel selectModel) {
        return new SelectRenderer(selectModel);
    }
}
