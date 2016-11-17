package org.mybatis.qbe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.mybatis.qbe.condition.Condition;
import org.mybatis.qbe.field.Field;

public class Criterion<T> {
    private Field<T> field;
    private Condition<T> condition;
    private String connector;
    private List<Criterion<?>> subCriteria = new ArrayList<>();
    
    private Criterion() {
        super();
    }
    
    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }
    
    public boolean hasSubCriteria() {
        return !subCriteria.isEmpty();
    }
    
    public Field<T> field() {
        return field;
    }
    
    public Condition<T> condition() {
        return condition;
    }
    
    public String fieldName() {
        return condition.fieldName(field);
    }
    
    public String fieldNameWithoutAlias() {
        return condition.fieldNameWithoutAlias(field);
    }
    
    public void visitSubCriteria(Consumer<Criterion<?>> consumer) {
        subCriteria.stream().forEach(consumer::accept);
    }

    public static <T> Criterion<T> of(Field<T> field, Condition<T> condition, Criterion<?>...subCriteria) {
        return Criterion.of(null,  field, condition, subCriteria);
    }
    
    public static <T> Criterion<T> of(String connector, Field<T> field, Condition<T> condition, Criterion<?>...subCriteria) {
        Criterion<T> criterion = new Criterion<>();
        criterion.field = field;
        criterion.condition = condition;
        criterion.connector = connector;
        Arrays.stream(subCriteria).forEach(criterion.subCriteria::add);
        return criterion;
    }
}
