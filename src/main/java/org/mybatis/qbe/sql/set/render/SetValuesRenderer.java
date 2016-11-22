package org.mybatis.qbe.sql.set.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.qbe.sql.set.SetValues;
import org.mybatis.qbe.sql.where.SqlField;

public class SetValuesRenderer {
    
    private SetValues setValues;
    
    private SetValuesRenderer(SetValues setValues) {
        this.setValues = setValues;
    }
    
    public SetSupport render() {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(new AtomicInteger(1)).render(setValues);
    }
    
    public SetSupport render(AtomicInteger sequence) {
        // we do this so the render method can be called multiple times
        // and return the same result
        return new Renderer(sequence).render(setValues);
    }

    public static SetValuesRenderer of(SetValues setValues) {
        return new SetValuesRenderer(setValues);
    }

    private static class Renderer {
        private AtomicInteger sequence;
        private Map<String, Object> parameters = new HashMap<>();

        public Renderer(AtomicInteger sequence) {
            super();
            this.sequence = sequence;
        }
        
        public SetSupport render(SetValues setClause) {
            List<String> phrases = new ArrayList<>();
            
            setClause.visitFieldValuePairs(p -> {
                int number = sequence.getAndIncrement();
                SqlField<?> field = p.getField();
                String phrase = String.format("%s = %s", field.render(),
                        field.getParameterRenderer(number).render());
                phrases.add(phrase);
                parameters.put(String.format("p%s", number), p.getValue());
            });
            
            String phrase = phrases.stream().collect(Collectors.joining(", ", "set ", ""));
            return SetSupport.of(phrase, parameters);
        }
    }
}
