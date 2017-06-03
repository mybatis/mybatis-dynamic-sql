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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;

public class FieldAndValueCollector<T> {
    
    private List<String> columnNames = new ArrayList<>();
    private List<String> valuePhrases = new ArrayList<>();
    private T record;
    private SqlTable table;
    
    public FieldAndValueCollector(T record, SqlTable table) {
        this.record = record;
        this.table = table;
    }
    
    public void add(FieldAndValue fieldAndValue) {
        columnNames.add(fieldAndValue.fieldName());
        valuePhrases.add(fieldAndValue.valuePhrase());
    }
    
    public FieldAndValueCollector<T> merge(FieldAndValueCollector<T> other) {
        columnNames.addAll(other.columnNames);
        valuePhrases.addAll(other.valuePhrases);
        return this;
    }

    public String columnsPhrase() {
        return columnNames.stream()
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
    }

    public String valuesPhrase() {
        return valuePhrases.stream()
                .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public InsertSupport<T> toInsertSupport() {
        return InsertSupport.of(columnsPhrase(), valuesPhrase(), record, table);
    }
    
    public static <T> Collector<FieldAndValue, FieldAndValueCollector<T>, InsertSupport<T>> toInsertSupport(T record,
            SqlTable table) {
        return Collector.of(() -> new FieldAndValueCollector<>(record, table),
                FieldAndValueCollector::add,
                FieldAndValueCollector::merge,
                FieldAndValueCollector::toInsertSupport);
    }
}