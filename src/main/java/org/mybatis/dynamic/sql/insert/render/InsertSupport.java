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

import org.mybatis.dynamic.sql.AbstractSqlSupport;

public class InsertSupport<T> extends AbstractSqlSupport {
    
    private String columnsPhrase;
    private String valuesPhrase;
    private T record;
    
    private InsertSupport(String tableName, String columnsPhrase, String valuesPhrase, T record) {
        super(tableName);
        this.columnsPhrase = columnsPhrase;
        this.valuesPhrase = valuesPhrase;
        this.record = record;
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
                + ONE_SPACE
                + getColumnsPhrase()
                + ONE_SPACE
                + getValuesPhrase();
    }

    public static <T> InsertSupport<T> of(String tableName, String columnsPhrase, String valuesPhrase, T record) {
        return new InsertSupport<>(tableName, columnsPhrase, valuesPhrase, record);
    }
}
