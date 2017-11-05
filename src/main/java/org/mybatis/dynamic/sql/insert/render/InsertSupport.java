/**
 *    Copyright 2016-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.insert.render;

import java.util.Objects;

import org.mybatis.dynamic.sql.AbstractSqlSupport;

public class InsertSupport<T> extends AbstractSqlSupport {
    
    private String columnsPhrase;
    private String valuesPhrase;
    private T record;
    
    private InsertSupport(Builder<T> builder) {
        super(builder.tableName);
        this.columnsPhrase = Objects.requireNonNull(builder.columnsPhrase);
        this.valuesPhrase = Objects.requireNonNull(builder.valuesPhrase);
        this.record = Objects.requireNonNull(builder.record);
    }
    
    public String getColumnsPhrase() {
        return columnsPhrase;
    }

    public String getValuesPhrase() {
        return valuesPhrase;
    }

    public T getRecord() {
        return record;
    }
    
    public String getFullInsertStatement() {
        return "insert into " //$NON-NLS-1$
                + tableName()
                + " " //$NON-NLS-1$
                + columnsPhrase
                + " " //$NON-NLS-1$
                + valuesPhrase;
    }

    public static class Builder<T> {
        private String tableName;
        private String columnsPhrase;
        private String valuesPhrase;
        private T record;
        
        public Builder<T> withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder<T> withColumnsPhrase(String columnsPhrase) {
            this.columnsPhrase = columnsPhrase;
            return this;
        }
        
        public Builder<T> withValuesPhrase(String valuesPhrase) {
            this.valuesPhrase = valuesPhrase;
            return this;
        }
        
        public Builder<T> withRecord(T record) {
            this.record = record;
            return this;
        }
        
        public InsertSupport<T> build() {
            return new InsertSupport<>(this);
        }
    }
}
