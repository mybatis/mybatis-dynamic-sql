package org.mybatis.qbe.sql.insert.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.FieldValuePairList;
import org.mybatis.qbe.sql.where.SqlField;

public interface InsertSupportShortcut {

    static Builder insert() {
        return new Builder();
    }
    
    static class Builder {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

        public Builder() {
            super();
        }
        
        public <T> Builder withValueIfPresent(SqlField<T> field, T value) {
            if (value != null) {
                withValue(field, value);
            }
            return this;
        }
        
        public <T> Builder withValue(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return this;
        }

        public <T> Builder withNullValue(SqlField<T> field) {
            withValue(field, null);
            return this;
        }

        public InsertSupport build() {
            FieldValuePairList fvp = new FieldValuePairList.Builder()
                .withFieldValuePairs(fieldValuePairs.stream())
                .build();
            return InsertValuesRenderer.of(fvp).render();
        }

        public InsertSupport buildIgnoringAlias() {
            FieldValuePairList fvp = new FieldValuePairList.Builder()
                    .withFieldValuePairs(fieldValuePairs.stream())
                    .buildIgnoringAlias();
            return InsertValuesRenderer.of(fvp).render();
        }
    }
}
