package org.mybatis.qbe.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.where.SqlField;

public class InsertValues {
    private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

    private InsertValues(Stream<FieldValuePair<?>> fieldValuePairs) {
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
        
        public <T> Builder andValue(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return this;
        }
        
        public <T> Builder andNullValue(SqlField<T> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
            return this;
        }
        
        public InsertValues build() {
            return new InsertValues(fieldValuePairs.stream());
        }
        
        public InsertValues buildIgnoringAlias() {
            return new InsertValues(fieldValuePairs.stream().map(FieldValuePair::ignoringAlias));
        }
    }
}
