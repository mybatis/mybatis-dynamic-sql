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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
            return build(t -> true);
        }

        public InsertSupport<T> buildSelectiveInsert() {
            return build(fm -> fm.getterFunction.get() != null);
        }
        
        private InsertSupport<T> build(Predicate<FieldMapping<?>> filter) {
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            fieldMappings.stream().filter(filter).forEach(fm -> {
                SqlField<?> field = fm.field;
                fieldPhrases.add(field.nameIgnoringTableAlias());
                valuePhrases.add(field.getFormattedJdbcPlaceholder(String.format("record.%s", fm.property)));
            });
            
            String fieldsPhrase = fieldPhrases.stream().collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String valuesPhrase = valuePhrases.stream().collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return InsertSupport.of(fieldsPhrase, valuesPhrase, record);
        }

        /**
         * A little triplet to hold the field mapping.  Only intended for use in this builder. 
         *  
         * @param <F> the field type of this mapping
         */
        private static class FieldMapping<F> {
            private SqlField<F> field;
            private String property;
            private Supplier<F> getterFunction;
            
            private FieldMapping() {
                super();
            }
            
            private static <F> FieldMapping<F> of(SqlField<F> field, String property, Supplier<F> getterFunction) {
                FieldMapping<F> mapping = new FieldMapping<>();
                mapping.field = field;
                mapping.property = property;
                mapping.getterFunction = getterFunction;
                return mapping;
            }
        }
    }
}
