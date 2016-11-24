package org.mybatis.qbe.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.FieldValuePair;

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
        
        public Builder() {
            super();
        }
        
        public Builder withFieldValuePairs(Stream<FieldValuePair<?>> fieldValuePairs) {
            fieldValuePairs.forEach(this.fieldValuePairs::add);
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
