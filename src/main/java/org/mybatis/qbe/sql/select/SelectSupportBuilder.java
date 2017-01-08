/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.select;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlColumn;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface SelectSupportBuilder {

    static SelectSupportBuildStep1 select(SqlColumn<?>...columns) {
        return new SelectSupportBuildStep1(columns);
    }
    
    static SelectSupportBuildStep1 selectDistinct(SqlColumn<?>...columns) {
        SelectSupportBuildStep1 buildStep = new SelectSupportBuildStep1(columns);
        buildStep.makeDistinct();
        return buildStep;
    }
    
    static SelectSupportBuildStep1 selectCount() {
        return new SelectSupportBuildStep1();
    }
    
    static class SelectSupportBuildStep1 {
        private SelectSupport.Builder selectSupportBuilder = new SelectSupport.Builder();

        public SelectSupportBuildStep1() {
            selectSupportBuilder.withColumnList("count(*)"); //$NON-NLS-1$
        }
        
        public SelectSupportBuildStep1(SqlColumn<?>...columns) {
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
                    .collect(Collectors.joining(", "));
        }
    }
    
    static class SelectSupportBuildStep2 {
        private SelectSupport.Builder selectSupportBuilder;
        
        public SelectSupportBuildStep2(SelectSupport.Builder selectSupportBuilder) {
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public <T> SelectSupportBuildStep3 where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new SelectSupportBuildStep3(selectSupportBuilder, column, condition, subCriteria);
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
    
    static class SelectSupportBuildStep3 extends AbstractWhereBuilder<SelectSupportBuildStep3> {
        private SelectSupport.Builder selectSupportBuilder;
        
        public <T> SelectSupportBuildStep3(SelectSupport.Builder selectSupportBuilder, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
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
        public SelectSupportBuildStep3 getThis() {
            return this;
        }
    }
    
    static class SelectSupportBuildStep4 {
        private SelectSupport.Builder selectSupportBuilder;

        public SelectSupportBuildStep4(SelectSupport.Builder selectSupportBuilder) {
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public SelectSupport build() {
            return selectSupportBuilder.build();
        }
    }
}
