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
package org.mybatis.qbe.sql.delete;

import org.mybatis.qbe.Condition;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlColumn;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder;
import org.mybatis.qbe.sql.where.WhereSupport;

public interface DeleteSupportBuilder {

    static DeleteSupportBuildStep1 deleteFrom(SqlTable table) {
        return new DeleteSupportBuildStep1(table);
    }
    
    static class DeleteSupportBuildStep1 {
        private SqlTable table;
        
        public DeleteSupportBuildStep1(SqlTable table) {
            this.table = table;
        }
        
        public <T> DeleteSupportBuildStep2 where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            return new DeleteSupportBuildStep2(table, column, condition, subCriteria);
        }
    }
    
    static class DeleteSupportBuildStep2 extends AbstractWhereBuilder<DeleteSupportBuildStep2> {
        private SqlTable table;
        
        public <T> DeleteSupportBuildStep2(SqlTable table, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
            this.table = table;
        }
        
        public DeleteSupport build() {
            WhereSupport whereSupport = renderCriteria(SqlColumn::name);
            return DeleteSupport.of(whereSupport.getWhereClause(), whereSupport.getParameters(), table);
        }
        
        @Override
        public DeleteSupportBuildStep2 getThis() {
            return this;
        }
    }
}
