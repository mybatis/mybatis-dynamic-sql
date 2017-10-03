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

import java.util.Arrays;
import java.util.List;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;
import org.mybatis.dynamic.sql.where.WhereModel;

public class SelectModelBuilder {

    private boolean isDistinct;
    private List<SelectListItem> selectList;
    private SqlTable table;
    private String tableAlias;
    private WhereModel whereModel;
    private List<SqlColumn<?>> orderByColumns;
    
    private SelectModelBuilder(SelectListItem...selectList) {
        this.selectList = Arrays.asList(selectList);
    }
    
    public SelectSupportAfterFromBuilder from(SqlTable table) {
        this.table = table;
        return new SelectSupportAfterFromBuilder();
    }

    public SelectSupportAfterFromBuilder from(SqlTable table, String tableAlias) {
        this.table = table;
        this.tableAlias = tableAlias;
        return new SelectSupportAfterFromBuilder();
    }

    public static SelectModelBuilder of(SelectListItem...selectList) {
        return new SelectModelBuilder(selectList);
    }
    
    public static SelectModelBuilder ofDistinct(SelectListItem...selectList) {
        SelectModelBuilder builder = SelectModelBuilder.of(selectList);
        builder.isDistinct = true;
        return builder;
    }
    
    protected SelectModel buildModel() {
        return new SelectModel.Builder(table)
                .isDistinct(isDistinct)
                .withColumns(selectList)
                .withTableAlias(tableAlias)
                .withWhereModel(whereModel)
                .withOrderByColumns(orderByColumns)
                .build();
    }
    
    @FunctionalInterface
    public interface Buildable {
        SelectModel build();
    }
    
    public class SelectSupportAfterFromBuilder implements Buildable {
        private SelectSupportAfterFromBuilder() {
            super();
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition) {
            return new SelectSupportWhereBuilder(column, condition);
        }

        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        @Override
        public SelectModel build() {
            return buildModel();
        }
    }
    
    public class SelectSupportWhereBuilder extends AbstractWhereModelBuilder<SelectSupportWhereBuilder> 
            implements Buildable {
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition) {
            super(column, condition);
        }
        
        private <T> SelectSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public SelectSupportAfterOrderByBuilder orderBy(SqlColumn<?>...columns) {
            whereModel = buildWhereModel();
            orderByColumns = Arrays.asList(columns);
            return new SelectSupportAfterOrderByBuilder();
        }
        
        @Override
        public SelectModel build() {
            whereModel = buildWhereModel();
            return buildModel();
        }
        
        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public class SelectSupportAfterOrderByBuilder implements Buildable {
        private SelectSupportAfterOrderByBuilder() {
            super();
        }
        
        @Override
        public SelectModel build() {
            return buildModel();
        }
    }
}
