package org.mybatis.qbe.sql.set;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.where.SqlField;

public class SetValues {
    private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

    private SetValues(Stream<FieldValuePair<?>> fieldValuePairs) {
        fieldValuePairs.forEach(this.fieldValuePairs::add);
    }
    
    public void visitFieldValuePairs(Consumer<FieldValuePair<?>> consumer) {
        fieldValuePairs.stream().forEach(consumer);
    }
    
    public static class Builder {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();
        
        public <T> Builder(SqlField<T> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
        }

        public <T> Builder(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
        }
        
        public <T> Builder andSet(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return this;
        }
        
        public <T> Builder andSetNull(SqlField<T> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
            return this;
        }
        
        public SetValues build() {
            return new SetValues(fieldValuePairs.stream());
        }
        
        public SetValues buildIgnoringAlias() {
            return new SetValues(fieldValuePairs.stream().map(FieldValuePair::ignoringAlias));
        }
    }
}
