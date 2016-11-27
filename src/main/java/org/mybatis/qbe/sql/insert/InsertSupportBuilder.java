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
            return build(Function.identity());
        }

        public InsertSupport buildIgnoringAlias() {
            return build(FieldAndValue::ignoringAlias);
        }

        private InsertSupport build(Function<FieldAndValue<?>, FieldAndValue<?>> mapper) {
            AtomicInteger sequence = new AtomicInteger(1);
            Map<String, Object> parameters = new HashMap<>();
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            fieldsAndValues.stream().map(mapper).forEach(fv -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = fv.getField();
                fieldPhrases.add(field.render());
                valuePhrases.add(field.getParameterRenderer(number).render());
                parameters.put(String.format("p%s", number), fv.getValue()); //$NON-NLS-1$
            });
            
            String fieldsPhrase = fieldPhrases.stream().collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String valuesPhrase = valuePhrases.stream().collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return InsertSupport.of(fieldsPhrase, valuesPhrase, parameters);
        }
    }
}
