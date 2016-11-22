package org.mybatis.qbe.sql.insert.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.where.SqlField;

public class InsertSupportRenderer {
    private InsertSupport insertSupport;
    
    private InsertSupportRenderer(InsertSupport insertSupport) {
        this.insertSupport = insertSupport;
    }
    
    public RenderedInsertSupport render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(new AtomicInteger(1)).render(insertSupport);
    }
    
    public static InsertSupportRenderer of(InsertSupport insertSupport) {
        return new InsertSupportRenderer(insertSupport);
    }

    private static class Renderer {
        private AtomicInteger sequence;
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer(AtomicInteger sequence) {
            super();
            this.sequence = sequence;
        }
        
        public RenderedInsertSupport render(InsertSupport insertClause) {
            List<String> fieldPhrases = new ArrayList<>();
            List<String> valuePhrases = new ArrayList<>();
            
            insertClause.visitFieldValuePairs(p -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = p.getField();
                fieldPhrases.add(field.render());
                valuePhrases.add(field.getParameterRenderer(number).render());
                parameters.put(String.format("p%s", number), p.getValue());
            });
            
            String fieldsPhrase = fieldPhrases.stream().collect(Collectors.joining(", ", "(", ")"));
            String valuesPhrase = valuePhrases.stream().collect(Collectors.joining(", ", "values (", ")"));
            return RenderedInsertSupport.of(fieldsPhrase, valuesPhrase, parameters);
        }
    }
}
