package org.mybatis.qbe.sql.insert.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.FieldValuePairList;
import org.mybatis.qbe.sql.where.SqlField;

public interface InsertSupportShortcut {

    static Builder insertSelective() {
        return new Builder();
    }
    
    static <T> Builder insertValue(SqlField<T> field, T value) {
        return new Builder(field, value);
    }

    static <T> Builder insertNullValue(SqlField<T> field) {
        return new Builder(field);
    }
    
    static class Builder {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

        public Builder() {
            super();
        }
        
        public <T> Builder(SqlField<T> field) {
            andNullValue(field);
        }
        
        public <T> Builder(SqlField<T> field, T value) {
            andValue(field, value);
        }
        
        public <T> Builder withValueIfPresent(SqlField<T> field, T value) {
            if (value != null) {
                andValue(field, value);
            }
            return this;
        }
        
        public <T> Builder andValue(SqlField<T> field, T value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return this;
        }

        public <T> Builder andNullValue(SqlField<T> field) {
            andValue(field, null);
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
