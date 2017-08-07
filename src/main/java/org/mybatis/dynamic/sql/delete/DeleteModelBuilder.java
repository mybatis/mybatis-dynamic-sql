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
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;

public class DeleteModelBuilder {

    private SqlTable table;
    
    private DeleteModelBuilder(SqlTable table) {
        this.table = table;
    }
    
    public <T> DeleteSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition) {
        return new DeleteSupportWhereBuilder(column, condition);
    }
    
    public <T> DeleteSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition,
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
        return new DeleteModel.Builder(table)
                .build();
    }
    
    public static DeleteModelBuilder of(SqlTable table) {
        return new DeleteModelBuilder(table);
    }
    
    public class DeleteSupportWhereBuilder extends AbstractWhereModelBuilder<DeleteSupportWhereBuilder> {
        
        private <T> DeleteSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition) {
            super(column, condition);
        }
        
        private <T> DeleteSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public DeleteModel build() {
            return new DeleteModel.Builder(table)
                    .withWhereModel(buildWhereModel())
                    .build();
        }
        
        @Override
        protected DeleteSupportWhereBuilder getThis() {
            return this;
        }
    }
}
