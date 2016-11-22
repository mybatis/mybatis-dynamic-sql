package org.mybatis.qbe.sql.insert.render;

import org.mybatis.qbe.sql.insert.InsertValues;
import org.mybatis.qbe.sql.where.SqlField;

public interface InsertSupportShortcut {

    static <T> Builder insertValue(SqlField<T> field, T value) {
        return new Builder(field, value);
    }

    static <T> Builder insertNullValue(SqlField<T> field) {
        return new Builder(field);
    }
    
    static class Builder {
        
        private InsertValues.Builder insertValuesBuilder;

        public <T> Builder(SqlField<T> field) {
            insertValuesBuilder = new InsertValues.Builder(field);
        }
        
        public <T> Builder(SqlField<T> field, T value) {
            insertValuesBuilder = new InsertValues.Builder(field, value);
        }
        
        public <T> Builder andValue(SqlField<T> field, T value) {
            insertValuesBuilder.andValue(field, value);
            return this;
        }

        public <T> Builder andNullValue(SqlField<T> field) {
            insertValuesBuilder.andNullValue(field);
            return this;
        }

        public InsertSupport build() {
            return InsertValuesRenderer.of(insertValuesBuilder.build()).render();
        }

        public InsertSupport buildIgnoringAlias() {
            return InsertValuesRenderer.of(insertValuesBuilder.buildIgnoringAlias()).render();
        }
    }
}
