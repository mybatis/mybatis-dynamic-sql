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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class DeleteDSL {

    private SqlTable table;
    
    private DeleteDSL(SqlTable table) {
        this.table = table;
    }
    
    public <T> DeleteSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition) {
        return new DeleteSupportWhereBuilder(column, condition);
    }
    
    public <T> DeleteSupportWhereBuilder where(SqlColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new DeleteSupportWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method could result in an delete statement that deletes
     * all rows in a table.
     * 
     * @return the model class
     */
    public DeleteModel build() {
        return new DeleteModel.Builder()
                .withTable(table)
                .build();
    }
    
    public static DeleteDSL deleteFrom(SqlTable table) {
        return new DeleteDSL(table);
    }
    
    public class DeleteSupportWhereBuilder extends AbstractWhereDSL<DeleteSupportWhereBuilder> {
        
        private <T> DeleteSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        private <T> DeleteSupportWhereBuilder(SqlColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public DeleteModel build() {
            return new DeleteModel.Builder()
                    .withTable(table)
                    .withWhereModel(buildWhereModel())
                    .build();
        }
        
        @Override
        protected DeleteSupportWhereBuilder getThis() {
            return this;
        }
    }
}
