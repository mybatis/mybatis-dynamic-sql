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

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface SelectSupportBuilder {

    static Builder selectSupport() {
        return new Builder(false);
    }
    
    static class Builder {
        private boolean isDistinct;
        
        public Builder(boolean isDistinct) {
            this.isDistinct = isDistinct;
        }
        
        public Builder distinct() {
            return new Builder(true);
        }
        
        public <T> WhereBuilder where(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new WhereBuilder(isDistinct, field, condition, subCriteria);
        }
    }
    
    static class WhereBuilder extends AbstractWhereBuilder<WhereBuilder> {
        private boolean isDistinct;
        private String orderByClause;
        
        public <T> WhereBuilder(boolean isDistinct, SqlField<T> field, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(field, condition, subCriteria);
            this.isDistinct = isDistinct;
        }
        
        public WhereBuilder orderBy(String orderByClause) {
            this.orderByClause = orderByClause;
            return this;
        }
        
        public SelectSupport build() {
            WhereSupport whereSupport = renderCriteria(SqlField::nameIncludingTableAlias);
            return SelectSupport.of(isDistinct, whereSupport.getWhereClause(), whereSupport.getParameters(), orderByClause);
        }
        
        @Override
        public WhereBuilder getThis() {
            return this;
        }
    }
}
