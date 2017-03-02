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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.AbstractWhereBuilder;
import org.mybatis.dynamic.sql.where.WhereSupport;

public class UpdateSupportBuilder {

    private int id = 1;
    private List<SetColumnAndValue<?>> columnsAndValues = new ArrayList<>();
    private SqlTable table;
    
    private UpdateSupportBuilder(SqlTable table) {
        this.table = table;
    }
    
    public <T> UpdateSupportBuilderFinisher<T> set(SqlColumn<T> column) {
        return new UpdateSupportBuilderFinisher<>(column);
    }
    
    public <T> UpdateSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return new UpdateSupportWhereBuilder(table, column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method will result in an update statement that updates
     * all rows in a table.
     * 
     * @return
     */
    public UpdateSupport build() {
        FragmentCollector setValuesCollector = renderSetValues();
        return UpdateSupport.of(setValuesCollector.fragments().collect(Collectors.joining(", ", "set ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                null, setValuesCollector.parameters(), table);
    }
    
    private FragmentCollector renderSetValues() {
        return columnsAndValues.stream()
                .map(SetColumnAndValue::fragmentAndParameters)
                .collect(FragmentCollector.fragmentAndParameterCollector());
    }
    
    public static UpdateSupportBuilder of(SqlTable table) {
        return new UpdateSupportBuilder(table);
    }
    
    public class UpdateSupportBuilderFinisher<T> {
        
        private SqlColumn<T> column;
        
        public UpdateSupportBuilderFinisher(SqlColumn<T> column) {
            this.column = column;
        }
        
        public UpdateSupportBuilder equalToNull() {
            columnsAndValues.add(SetColumnAndValue.of(column));
            return UpdateSupportBuilder.this;
        }

        public UpdateSupportBuilder equalTo(T value) {
            columnsAndValues.add(SetColumnAndValue.of(column, value, id++));
            return UpdateSupportBuilder.this;
        }

        public UpdateSupportBuilder equalToWhenPresent(T value) {
            if (value != null) {
                columnsAndValues.add(SetColumnAndValue.of(column, value, id++));
            }
            return UpdateSupportBuilder.this;
        }
    }

    public class UpdateSupportWhereBuilder extends AbstractWhereBuilder<UpdateSupportWhereBuilder> {
        private SqlTable table;
        
        public <T> UpdateSupportWhereBuilder(SqlTable table, SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
            this.table = table;
        }
        
        public UpdateSupport build() {
            Map<String, Object> parameters = new HashMap<>();
            FragmentCollector setValuesCollector = renderSetValues();
            WhereSupport whereSupport = renderCriteriaIgnoringTableAlias();
            parameters.putAll(setValuesCollector.parameters());
            parameters.putAll(whereSupport.getParameters());
            return UpdateSupport.of(setValuesCollector.fragments().collect(Collectors.joining(", ", "set ", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    whereSupport.getWhereClause(), parameters, table);
        }
        
        @Override
        protected UpdateSupportWhereBuilder getThis() {
            return this;
        }
    }
}
