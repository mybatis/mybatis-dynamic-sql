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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;
import org.mybatis.dynamic.sql.where.WhereModel;

public class SelectModelBuilder {

    private boolean isDistinct;
    private List<SqlColumn<?>> columns = new ArrayList<>();
    private SqlTable table;
    private WhereModel whereModel;
    private List<SqlColumn<?>> orderByColumns;
    
    private SelectModelBuilder(SqlColumn<?>...columns) {
        this.columns.addAll(Arrays.asList(columns));
    }
    
    public SelectSupportAfterFromBuilder from(SqlTable table) {
        this.table = table;
        return new SelectSupportAfterFromBuilder();
    }


    public static SelectModelBuilder of(SqlColumn<?>...columns) {
        return new SelectModelBuilder(columns);
    }
    
    public static SelectModelBuilder ofDistinct(SqlColumn<?>...columns) {
        SelectModelBuilder builder = SelectModelBuilder.of(columns);
        builder.isDistinct = true;
        return builder;
    }
    
    protected SelectModel buildModel() {
        return new SelectModel.Builder()
                .isDistinct(isDistinct)
                .withColumns(columns)
                .withTable(table)
                .withWhereModel(whereModel)
                .withOrderByColumns(orderByColumns)
                .build();
    }
    
    protected SelectSupport buildModelAndRender(RenderingStrategy renderingStrategy) {
        return SelectRenderer.of(buildModel()).render(renderingStrategy);
    }
    
    public class SelectSupportAfterFromBuilder {
        private SelectSupportAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }

        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectModel build() {
            return buildModel();
        }

        public SelectSupport buildAndRender(RenderingStrategy renderingStrategy) {
            return buildModelAndRender(renderingStrategy);
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereModelBuilder<SelectSupportWhereBuilder> {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            whereModel = buildWhereModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        public SelectModel build() {
            whereModel = buildWhereModel();
            return buildModel();
        }
        
        public SelectSupport buildAndRender(RenderingStrategy renderingStrategy) {
            whereModel = buildWhereModel();
            return buildModelAndRender(renderingStrategy);
        }

        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class SelectSupportAfterOrderByBuilder {
        private SelectSupportAfterOrderByBuilder() {
            super();
        }
        
        public SelectModel build() {
            return buildModel();
        }

        public SelectSupport buildAndRender(RenderingStrategy renderingStrategy) {
            return buildModelAndRender(renderingStrategy);
        }
    }
}
