package org.mybatis.qbe.sql.set;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.where.SqlField;

public class SetClause {
    private List<SetPhrase<?>> phrases = new ArrayList<>();

    private SetClause(Stream<SetPhrase<?>> phrases) {
        phrases.forEach(this.phrases::add);
    }
    
    public void visitPhrases(Consumer<SetPhrase<?>> consumer) {
        phrases.stream().forEach(consumer);
    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private List<SetPhrase<?>> phrases = new ArrayList<>();
        
        public <S> AbstractBuilder(SqlField<S> field) {
            phrases.add(SetPhrase.of(field));
        }
        
        public <S> AbstractBuilder(SqlField<S> field, S value) {
            phrases.add(SetPhrase.of(field, value));
        }
        
        public <S> T set(SqlField<S> field, S value) {
            phrases.add(SetPhrase.of(field, value));
            return getThis();
        }
        
        public <S> T setNull(SqlField<S> field) {
            phrases.add(SetPhrase.of(field));
            return getThis();
        }
        
        public SetClause build() {
            return new SetClause(phrases.stream());
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
