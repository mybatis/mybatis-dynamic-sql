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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mybatis.qbe.sql.FieldAndValue;
import org.mybatis.qbe.sql.SqlField;

public interface InsertSupportBuilder {

    static Builder insertSupport() {
        return new Builder();
    }
    
    static class Builder {
        private List<FieldAndValue<?>> fieldsAndValues = new ArrayList<>();

        public Builder() {
            super();
        }
        
        public <T> Builder withValueIfPresent(SqlField<T> field, T value) {
            if (value != null) {
                withValue(field, value);
            }
            return this;
        }
        
        public <T> Builder withValue(SqlField<T> field, T value) {
            fieldsAndValues.add(FieldAndValue.of(field, value));
            return this;
        }

        public <T> Builder withNullValue(SqlField<T> field) {
            withValue(field, null);
            return this;
        }

        public InsertSupport build() {
            return build(SqlField::nameWithTableAlias);
        }

        public InsertSupport buildIgnoringAlias() {
            return build(SqlField::nameWithoutTableAlias);
        }

        private InsertSupport build(Function<SqlField<?>, String> nameFunction) {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            fieldsAndValues.forEach(fv -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = fv.getField();
                fieldPhrases.add(nameFunction.apply(field));
                valuePhrases.add(field.getParameterRenderer(number).render());
                parameters.put(String.format("p%s", number), fv.getValue()); //$NON-NLS-1$
            });
            
            String fieldsPhrase = fieldPhrases.stream().collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String valuesPhrase = valuePhrases.stream().collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return InsertSupport.of(fieldsPhrase, valuesPhrase, parameters);
        }
    }
}
