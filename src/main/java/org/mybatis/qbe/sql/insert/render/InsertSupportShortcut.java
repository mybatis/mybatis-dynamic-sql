package org.mybatis.qbe.sql.insert.render;

import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.where.SqlField;

public interface InsertSupportShortcut {

    static <T> Builder insertValue(SqlField<T> field, T value) {
        return new Builder(field, value);
    }

    static <T> Builder insertNullValue(SqlField<T> field) {
        return new Builder(field);
    }
    
    static class Builder extends InsertSupport.AbstractBuilder<Builder> {

        public <T> Builder(SqlField<T> field, T value) {
            super(field, value);
        }
        
        public <T> Builder(SqlField<T> field) {
            super(field);
        }

        public RenderedInsertSupport render() {
            return InsertSupportRenderer.of(build()).render();
        }

        public RenderedInsertSupport renderIgnoringAlias() {
            return InsertSupportRenderer.of(buildIgnoringAlias()).render();
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
