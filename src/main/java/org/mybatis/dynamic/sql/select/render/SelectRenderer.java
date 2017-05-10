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

import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;
import org.mybatis.dynamic.sql.where.render.WhereSupport;

public class SelectRenderer {
    private SelectModel selectModel;
    
    private SelectRenderer(SelectModel selectModel) {
        this.selectModel = selectModel;
    }
    
    public SelectSupport render(RenderingStrategy renderingStrategy) {
        return selectModel.whereModel().map(wm -> renderWithWhereClause(wm, renderingStrategy))
                .orElse(renderWithoutWhereClause());
    }
    
    private SelectSupport renderWithWhereClause(WhereModel whereModel, RenderingStrategy renderingStrategy) {
        WhereSupport whereSupport = WhereRenderer.of(whereModel, renderingStrategy).renderCriteriaIncludingTableAlias();
        
        return new SelectSupport.Builder()
                .isDistinct(selectModel.isDistinct())
                .withColumnList(calculateColumnList())
                .withTable(selectModel.table())
                .withWhereClause(whereSupport.getWhereClause())
                .withParameters(whereSupport.getParameters())
                .withOrderByClause(calculateOrderByPhrase())
                .build();
    }
    
    private SelectSupport renderWithoutWhereClause() {
        return new SelectSupport.Builder()
                .isDistinct(selectModel.isDistinct())
                .withColumnList(calculateColumnList())
                .withTable(selectModel.table())
                .withOrderByClause(calculateOrderByPhrase())
                .build();
    }
    
    private String calculateColumnList() {
        return selectModel.columns()
                .map(SqlColumn::nameIncludingTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }
    
    private String calculateOrderByPhrase() {
        String orderBy = selectModel.orderByColumns()
                .map(SqlColumn::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return trimToNull(orderBy);
    }
    
    public static SelectRenderer of(SelectModel selectModel) {
        return new SelectRenderer(selectModel);
    }
    
    private String trimToNull(String in) {
        String out = in.trim();
        return out.length() == 0 ? null : out;
    }
}
