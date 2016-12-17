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
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface SelectSupportBuilder {

    static Builder selectSupport() {
        return new Builder();
    }
    
    static class Builder {
        private SelectSupport.Builder selectSupportBuilder = new SelectSupport.Builder();
        
        public Builder distinct() {
            selectSupportBuilder.isDistinct();
            return this;
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(selectSupportBuilder, field, condition, subCriteria);
        }
    }
    
    static class WhereBuilder extends AbstractWhereBuilder<WhereBuilder> {
        private SelectSupport.Builder selectSupportBuilder;
        
        public <T> WhereBuilder(SelectSupport.Builder selectSupportBuilder, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(field, condition, subCriteria);
            this.selectSupportBuilder = selectSupportBuilder;
        }
        
        public FinalBuilder orderBy(SqlField<?>...fields) {
            String orderByClause = 
                    Arrays.stream(fields)
                    .map(SqlField::orderByPhrase)
                    .collect(Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            buildWhereSupport();
            SelectSupport selectSupport = selectSupportBuilder.withOrderByClause(orderByClause)
                .build();
            return new FinalBuilder(selectSupport);
        }
        
        public SelectSupport build() {
            buildWhereSupport();
            return selectSupportBuilder.build();
        }
        
        private void buildWhereSupport() {
            WhereSupport whereSupport = renderCriteria(SqlField::nameIncludingTableAlias);
            selectSupportBuilder.withParameters(whereSupport.getParameters())
                .withWhereClause(whereSupport.getWhereClause());
        }

        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
    
    static class FinalBuilder {
        private SelectSupport selectSupport;

        public FinalBuilder(SelectSupport selectSupport) {
            this.selectSupport = selectSupport;
        }
        
        public SelectSupport build() {
            return selectSupport;
        }
    }
}
