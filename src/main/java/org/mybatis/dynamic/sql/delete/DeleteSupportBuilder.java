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
package org.mybatis.dynamic.sql.delete;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.AbstractWhereBuilder;
import org.mybatis.dynamic.sql.where.WhereSupport;

public class DeleteSupportBuilder {

    private SqlTable table;
    
    private DeleteSupportBuilder(SqlTable table) {
        this.table = table;
    }
    
    public <T> DeleteSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return new DeleteSupportWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method will result in an delete statement that deletes
     * all rows in a table.
     * 
     * @return
     */
    public DeleteSupport build() {
        return DeleteSupport.of(table);
    }
    
    public static DeleteSupportBuilder of(SqlTable table) {
        return new DeleteSupportBuilder(table);
    }
    
    public class DeleteSupportWhereBuilder extends AbstractWhereBuilder<DeleteSupportWhereBuilder> {
        
        private <T> DeleteSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public DeleteSupport build() {
            WhereSupport whereSupport = renderCriteria(SqlColumn::name);
            return DeleteSupport.of(whereSupport.getWhereClause(), whereSupport.getParameters(), table);
        }
        
        @Override
        protected DeleteSupportWhereBuilder getThis() {
            return this;
        }
    }
}
