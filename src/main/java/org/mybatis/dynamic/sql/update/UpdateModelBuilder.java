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

import org.mybatis.dynamic.sql.Condition;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateRenderer;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;
import org.mybatis.dynamic.sql.util.AbstractColumnAndValue;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.where.AbstractWhereModelBuilder;

public class UpdateModelBuilder {

    private List<AbstractColumnAndValue> columnsAndValues = new ArrayList<>();
    private SqlTable table;
    
    private UpdateModelBuilder(SqlTable table) {
        this.table = table;
    }
    
    public <T> UpdateSupportBuilderFinisher<T> set(SqlColumn<T> column) {
        return new UpdateSupportBuilderFinisher<>(column);
    }
    
    public <T> UpdateSupportWhereBuilder where(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
        return new UpdateSupportWhereBuilder(column, condition, subCriteria);
    }
    
    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     * 
     * @return
     */
    public UpdateModel build() {
        return new UpdateModel.Builder()
                .withTable(table)
                .withColumnValues(columnsAndValues)
                .build();
    }
    
    public UpdateSupport buildAndRender(RenderingStrategy renderingStrategy) {
        return UpdateRenderer.of(build()).render(renderingStrategy);
    }
    
    public static UpdateModelBuilder of(SqlTable table) {
        return new UpdateModelBuilder(table);
    }
    
    public class UpdateSupportBuilderFinisher<T> {
        
        private SqlColumn<T> column;
        
        public UpdateSupportBuilderFinisher(SqlColumn<T> column) {
            this.column = column;
        }
        
        public UpdateModelBuilder equalToNull() {
            columnsAndValues.add(NullMapping.of(column));
            return UpdateModelBuilder.this;
        }

        public UpdateModelBuilder equalToConstant(String constant) {
            columnsAndValues.add(ConstantMapping.of(column, constant));
            return UpdateModelBuilder.this;
        }
        
        public UpdateModelBuilder equalTo(T value) {
            columnsAndValues.add(ValueMapping.of(column, value));
            return UpdateModelBuilder.this;
        }

        public UpdateModelBuilder equalToWhenPresent(T value) {
            if (value != null) {
                columnsAndValues.add(ValueMapping.of(column, value));
            }
            return UpdateModelBuilder.this;
        }
    }

    public class UpdateSupportWhereBuilder extends AbstractWhereModelBuilder<UpdateSupportWhereBuilder> {
        
        public <T> UpdateSupportWhereBuilder(SqlColumn<T> column, Condition<T> condition, SqlCriterion<?>...subCriteria) {
            super(column, condition, subCriteria);
        }
        
        public UpdateModel build() {
            return new UpdateModel.Builder()
                    .withTable(table)
                    .withColumnValues(columnsAndValues)
                    .withWhereModel(buildWhereModel())
                    .build();
        }
        
        public UpdateSupport buildAndRender(RenderingStrategy renderingStrategy) {
            return UpdateRenderer.of(build()).render(renderingStrategy);
        }
        
        @Override
        protected UpdateSupportWhereBuilder getThis() {
            return this;
        }
    }
}
