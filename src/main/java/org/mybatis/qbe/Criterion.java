package org.mybatis.qbe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Criterion<T, S extends Field<T>, R extends Criterion<?, ?, ?>> {
    protected S field;
    protected Condition<T> condition;
    protected String connector;
    protected List<R> subCriteria = new ArrayList<>();
    
    protected Criterion() {
        super();
    }
    
    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }
    
    public S field() {
        return field;
    }
    
    public Condition<T> condition() {
        return condition;
    }
    
    public boolean hasSubCriteria() {
        return !subCriteria.isEmpty();
    }
 
    public void visitSubCriteria(Consumer<R> consumer) {
        subCriteria.forEach(consumer::accept);
    }
    
    public String renderField() {
        return condition.renderField(field);
    }
}
