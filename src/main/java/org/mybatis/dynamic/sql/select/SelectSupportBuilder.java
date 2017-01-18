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
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.AbstractWhereBuilder;
import org.mybatis.dynamic.sql.where.WhereSupport;

public class SelectSupportBuilder {

    private SelectSupport.Builder selectSupportBuilder = new SelectSupport.Builder();

    private SelectSupportBuilder() {
        selectSupportBuilder.withColumnList("count(*)"); //$NON-NLS-1$
    }
    
    private SelectSupportBuilder(SqlColumn<?>...columns) {
        selectSupportBuilder.withColumnList(calculateColumnList(columns));
    }
    
    public SelectSupportBuildStep2 from(SqlTable table) {
        selectSupportBuilder.withTable(table);
        return new SelectSupportBuildStep2(selectSupportBuilder);
    }

    private void makeDistinct() {
        selectSupportBuilder.isDistinct();
    }
    
    private String calculateColumnList(SqlColumn<?>...columns) {
        return Arrays.stream(columns)
                .map(SqlColumn::nameIncludingTableAndColumnAlias)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    public static SelectSupportBuilder select(SqlColumn<?>...columns) {
        return new SelectSupportBuilder(columns);
    }
    
    public static SelectSupportBuilder selectDistinct(SqlColumn<?>...columns) {
        SelectSupportBuilder buildStep = new SelectSupportBuilder(columns);
        buildStep.makeDistinct();
        return buildStep;
    }
    
    public static SelectSupportBuilder selectCount() {
        return new SelectSupportBuilder();
    }
    
    public static class SelectSupportBuildStep2 {
        private SelectSupport.Builder selectSupportBuilder;
        
        private SelectSupportBuildStep2(SelectSupport.Builder selectSupportBuilder) {
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public <T> SelectSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new SelectSupportWhereBuilder(selectSupportBuilder, column, condition, subCriteria);
        }

        public SelectSupportBuildStep4 orderBy(SqlColumn<?>...columns) {
            String orderByClause = 
                    Arrays.stream(columns)
                    .map(SqlColumn::orderByPhrase)
                    .collect(Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            selectSupportBuilder.withOrderByClause(orderByClause);
            return new SelectSupportBuildStep4(selectSupportBuilder);
        }
        
        public SelectSupport build() {
            return selectSupportBuilder.build();
        }
    }
    
    public static class SelectSupportWhereBuilder extends AbstractWhereBuilder<SelectSupportWhereBuilder> {
        private SelectSupport.Builder selectSupportBuilder;
        
        private <T> SelectSupportWhereBuilder(SelectSupport.Builder selectSupportBuilder, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public SelectSupportBuildStep4 orderBy(SqlColumn<?>...columns) {
            String orderByClause = 
                    Arrays.stream(columns)
                    .map(SqlColumn::orderByPhrase)
                    .collect(Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            buildWhereSupport();
            selectSupportBuilder.withOrderByClause(orderByClause);
            return new SelectSupportBuildStep4(selectSupportBuilder);
        }
        
        public SelectSupport build() {
            buildWhereSupport();
            return selectSupportBuilder.build();
        }
        
        private void buildWhereSupport() {
            WhereSupport whereSupport = renderCriteria(SqlColumn::nameIncludingTableAlias);
            selectSupportBuilder.withParameters(whereSupport.getParameters())
                .withWhereClause(whereSupport.getWhereClause());
        }

        @Override
        protected SelectSupportWhereBuilder getThis() {
            return this;
        }
    }
    
    public static class SelectSupportBuildStep4 {
        private SelectSupport.Builder selectSupportBuilder;

        private SelectSupportBuildStep4(SelectSupport.Builder selectSupportBuilder) {
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public SelectSupport build() {
            return selectSupportBuilder.build();
        }
    }
}
