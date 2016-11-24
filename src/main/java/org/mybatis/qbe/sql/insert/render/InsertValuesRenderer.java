package org.mybatis.qbe.sql.insert.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.qbe.sql.FieldValuePairList;
import org.mybatis.qbe.sql.where.SqlField;

public class InsertValuesRenderer {
    private FieldValuePairList insertValues;
    
    private InsertValuesRenderer(FieldValuePairList insertValues) {
        this.insertValues = insertValues;
    }
    
    public InsertSupport render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(new AtomicInteger(1)).render(insertValues);
    }
    
    public static InsertValuesRenderer of(FieldValuePairList insertValues) {
        return new InsertValuesRenderer(insertValues);
    }

    private static class Renderer {
        private AtomicInteger sequence;
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer(AtomicInteger sequence) {
            super();
            this.sequence = sequence;
        }
        
        public InsertSupport render(FieldValuePairList insertValues) {
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            insertValues.visitFieldValuePairs(p -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = p.getField();
                fieldPhrases.add(field.render());
                valuePhrases.add(field.getParameterRenderer(number).render());
                parameters.put(String.format("p%s", number), p.getValue());
            });
            
            String fieldsPhrase = fieldPhrases.stream().collect(Collectors.joining(", ", "(", ")"));
            String valuesPhrase = valuePhrases.stream().collect(Collectors.joining(", ", "values (", ")"));
            return InsertSupport.of(fieldsPhrase, valuesPhrase, parameters);
        }
    }
}
