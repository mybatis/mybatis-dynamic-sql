/**
 *    Copyright 2016-2019 the original author or authors.
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
import java.util.stream.IntStream;

public class FieldAndValueCollector {
    
    private List<FieldAndValue> fieldsAndValue = new ArrayList<>();
    
    public FieldAndValueCollector() {
        super();
    }
    
    public void add(FieldAndValue fieldAndValue) {
        fieldsAndValue.add(fieldAndValue);
    }
    
    public FieldAndValueCollector merge(FieldAndValueCollector other) {
        fieldsAndValue.addAll(other.fieldsAndValue);
        return this;
    }

    public String columnsPhrase() {
        return fieldsAndValue.stream()
                .map(FieldAndValue::fieldName)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String valuesPhrase() {
        return fieldsAndValue.stream()
                .map(FieldAndValue::valuePhrase)
                .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public String multiRowInsertValuesPhrase(int rowCount) {
        return IntStream.range(0, rowCount)
                .mapToObj(this::toSingleRowOfValues)
                .collect(Collectors.joining(", ", "values ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String toSingleRowOfValues(int row) {
        return fieldsAndValue.stream()
                .map(fmv -> fmv.valuePhrase(row))
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public static Collector<FieldAndValue, FieldAndValueCollector, FieldAndValueCollector> collect() {
        return Collector.of(FieldAndValueCollector::new,
                FieldAndValueCollector::add,
                FieldAndValueCollector::merge);
    }
}