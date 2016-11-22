package org.mybatis.qbe.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.where.SqlField;

public class InsertSupport {
    private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

    private InsertSupport(Stream<FieldValuePair<?>> fieldValuePairs) {
        fieldValuePairs.forEach(this.fieldValuePairs::add);
    }

    public void visitFieldValuePairs(Consumer<FieldValuePair<?>> consumer) {
        fieldValuePairs.stream().forEach(consumer);
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();
        
        public <S> AbstractBuilder(SqlField<S> field, S value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
        }
        
        public <S> AbstractBuilder(SqlField<S> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
        }
        
        public <S> T andValue(SqlField<S> field, S value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return getThis();
        }
        
        public <S> T andNullValue(SqlField<S> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
            return getThis();
        }
        
        public InsertSupport build() {
            return new InsertSupport(fieldValuePairs.stream());
        }
        
        public InsertSupport buildIgnoringAlias() {
            return new InsertSupport(fieldValuePairs.stream().map(FieldValuePair::ignoringAlias));
        }
        
        public abstract T getThis();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        public <T> Builder(SqlField<T> field) {
            super(field);
        }
        
        public <T> Builder(SqlField<T> field, T value) {
            super(field, value);
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
