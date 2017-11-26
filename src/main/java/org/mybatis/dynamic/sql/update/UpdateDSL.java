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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.UpdateMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

public class UpdateDSL {

    private List<UpdateMapping> columnsAndValues = new ArrayList<>();
    private SqlTable table;
    
    private UpdateDSL(SqlTable table) {
        this.table = Objects.requireNonNull(table);
    }
    
    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }
    
    public <T> UpdateWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new UpdateWhereBuilder(column, condition);
    }
    
    public <T> UpdateWhereBuilder where(BindableColumn<T> column, VisitableCondition<T> condition,
            SqlCriterion<?>...subCriteria) {
        return new UpdateWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     * 
     * @return the update model
     */
    public UpdateModel build() {
        return new UpdateModel.Builder()
                .withTable(table)
                .withColumnValues(columnsAndValues)
                .build();
    }
    
    public static UpdateDSL update(SqlTable table) {
        return new UpdateDSL(table);
    }
    
    public class SetClauseFinisher<T> {
        
        private SqlColumn<T> column;
        
        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }
        
        public UpdateDSL equalToNull() {
            columnsAndValues.add(NullMapping.of(column));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToConstant(String constant) {
            columnsAndValues.add(ConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }
        
        public UpdateDSL equalToStringConstant(String constant) {
            columnsAndValues.add(StringConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }
        
        public UpdateDSL equalTo(T value) {
            columnsAndValues.add(ValueMapping.of(column, value));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToWhenPresent(T value) {
            if (value != null) {
                columnsAndValues.add(ValueMapping.of(column, value));
            }
            return UpdateDSL.this;
        }
    }

    public class UpdateWhereBuilder extends AbstractWhereDSL<UpdateWhereBuilder> {
        
        public <T> UpdateWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition) {
            super(column, condition);
        }
        
        public <T> UpdateWhereBuilder(BindableColumn<T> column, VisitableCondition<T> condition,
                SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public UpdateModel build() {
            return new UpdateModel.Builder()
                    .withTable(table)
                    .withColumnValues(columnsAndValues)
                    .withWhereModel(buildWhereModel())
                    .build();
        }
        
        @Override
        protected UpdateWhereBuilder getThis() {
            return this;
        }
    }
}
