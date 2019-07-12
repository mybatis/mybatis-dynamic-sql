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

public class FieldAndMultipleValuesCollector {
    
    private List<FieldAndMultipleValues> fieldsAndValues = new ArrayList<>();
    
    public FieldAndMultipleValuesCollector() {
        super();
    }
    
    public void add(FieldAndMultipleValues fieldAndMultipleValues) {
        fieldsAndValues.add(fieldAndMultipleValues);
    }
    
    public FieldAndMultipleValuesCollector merge(FieldAndMultipleValuesCollector other) {
        fieldsAndValues.addAll(other.fieldsAndValues);
        return this;
    }

    public String columnsPhrase() {
        return fieldsAndValues.stream()
                .map(FieldAndMultipleValues::fieldName)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String valuesPhrase() {
        return transposeValues().stream()
                .map(this::toSingleRowOfValues)
                .collect(Collectors.joining(", ", "values ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String toSingleRowOfValues(List<String> in) {
        return in.stream()
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private List<List<String>> transposeValues() {
        // Note: this will fail if the array is not perfectly rectangular
        List<List<String>> answer = new ArrayList<>();
        int newRowCount = fieldsAndValues.get(0).valuePhrases().size();
        for (int i = 0; i < newRowCount; i++) {
            List<String> newRow = new ArrayList<>();
            answer.add(newRow);
            for (FieldAndMultipleValues fmv : fieldsAndValues) {
                newRow.add(fmv.valuePhrases().get(i));
            }
        }
        
        return answer;
    }
    
    public static Collector<FieldAndMultipleValues, FieldAndMultipleValuesCollector,
            FieldAndMultipleValuesCollector> collect() {
        return Collector.of(FieldAndMultipleValuesCollector::new,
                FieldAndMultipleValuesCollector::add,
                FieldAndMultipleValuesCollector::merge);
    }
}
