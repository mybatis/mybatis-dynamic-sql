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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.util.AbstractColumnAndValue;
import org.mybatis.dynamic.sql.util.BeanPropertyGetter;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;

public class InsertModelBuilder<T> {

    private T record;
    private SqlTable table;
    
    private InsertModelBuilder(T record) {
        this.record = record;
    }
    
    public InsertModelMappingBuilder into(SqlTable table) {
        this.table = table;
        return new InsertModelMappingBuilder();
    }
    
    public static <T> InsertModelBuilder<T> insert(T record) {
        return new InsertModelBuilder<>(record);
    }
    
    public class InsertModelMappingBuilder {
        private List<AbstractColumnAndValue> columnMappings = new ArrayList<>();
        
        private InsertModelMappingBuilder() {
            super();
        }
        
        public <F> InsertSupportMappingBuilderFinisher<F> map(SqlColumn<F> column) {
            return new InsertSupportMappingBuilderFinisher<>(column);
        }
        
        public InsertModel<T> build() {
            return InsertModel.of(table, record, columnMappings.stream());
        }
        
        public InsertSupport<T> buildAndRender() {
            return InsertRenderer.of(build()).render();
        }
        
        public class InsertSupportMappingBuilderFinisher<F> {
            private SqlColumn<F> column;
            
            public InsertSupportMappingBuilderFinisher(SqlColumn<F> column) {
                this.column = column;
            }
            
            public InsertModelMappingBuilder toProperty(String property) {
                columnMappings.add(PropertyMapping.of(column, property));
                return InsertModelMappingBuilder.this;
            }
            
            public InsertModelMappingBuilder toPropertyWhenPresent(String property) {
                if (BeanPropertyGetter.instance().getPropertyValue(record, property) != null) {
                    toProperty(property);
                }
                return InsertModelMappingBuilder.this;
            }
            
            public InsertModelMappingBuilder toNull() {
                columnMappings.add(NullMapping.of(column));
                return InsertModelMappingBuilder.this;
            }
            
            public InsertModelMappingBuilder toConstant(String constant) {
                columnMappings.add(ConstantMapping.of(column, constant));
                return InsertModelMappingBuilder.this;
            }
        }
    }
}
