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
package org.mybatis.qbe.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.SqlField;

public interface InsertSupportBuilder {

    static <T> Builder<T> insertSupport(T record) {
        return new Builder<>(record);
    }
    
    static class Builder<T> {
        private T record;
        private List<FieldMapping<?>> fieldMappings = new ArrayList<>();

        public Builder(T record) {
            super();
            this.record = record;
        }
        
        public <F> Builder<T> withFieldMapping(SqlField<F> field, String property, Supplier<F> getterFunction) {
            fieldMappings.add(FieldMapping.of(field, property, getterFunction));
            return this;
        }
        
        public InsertSupport<T> buildFullInsert() {
            return build(fieldMappings.stream());
        }

        public InsertSupport<T> buildSelectiveInsert() {
            return build(fieldMappings.stream().filter(FieldMapping::hasValue));
        }
        
        private InsertSupport<T> build(Stream<FieldMapping<?>> mappings) {
            return mappings.collect(Collector.of(
                    CollectorSupport::new,
                    CollectorSupport::add,
                    CollectorSupport::merge,
                    c -> c.toInsertSupport(record)));
        }
        
        /**
         * A little triplet to hold the field mapping.  Only intended for use in this builder. 
         *  
         * @param <F> the field type of this mapping
         */
        static class FieldMapping<F> {
            SqlField<F> field;
            String property;
            Supplier<F> getterFunction;
            
            boolean hasValue() {
                return getterFunction.get() != null;
            }
            
            static <F> FieldMapping<F> of(SqlField<F> field, String property, Supplier<F> getterFunction) {
                FieldMapping<F> mapping = new FieldMapping<>();
                mapping.field = field;
                mapping.property = property;
                mapping.getterFunction = getterFunction;
                return mapping;
            }
        }
        
        static class CollectorSupport {
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            void add(FieldMapping<?> mapping) {
                fieldPhrases.add(mapping.field.nameIgnoringTableAlias());
                valuePhrases.add(mapping.field.getFormattedJdbcPlaceholder(String.format("record.%s", mapping.property))); //$NON-NLS-1$
            }
            
            CollectorSupport merge(CollectorSupport other) {
                fieldPhrases.addAll(other.fieldPhrases);
                valuePhrases.addAll(other.valuePhrases);
                return this;
            }
            
            String fieldsPhrase() {
                return fieldPhrases.stream()
                        .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
            }

            String valuesPhrase() {
                return valuePhrases.stream()
                        .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            
            <T> InsertSupport<T> toInsertSupport(T record) {
                return InsertSupport.of(fieldsPhrase(), valuesPhrase(), record);
            }
        }
    }
}
