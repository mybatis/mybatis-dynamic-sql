/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.insert.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FieldAndValueCollector {
    final List<FieldAndValueAndParameters> fieldsAndValues = new ArrayList<>();

    public FieldAndValueCollector() {
        super();
    }

    public void add(FieldAndValueAndParameters fieldAndValueAndParameters) {
        fieldsAndValues.add(fieldAndValueAndParameters);
    }

    public FieldAndValueCollector merge(FieldAndValueCollector other) {
        fieldsAndValues.addAll(other.fieldsAndValues);
        return this;
    }

    public boolean isEmpty() {
        return fieldsAndValues.isEmpty();
    }

    public String columnsPhrase() {
        return fieldsAndValues.stream()
                .map(FieldAndValueAndParameters::fieldName)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String valuesPhrase() {
        return fieldsAndValues.stream()
                .map(FieldAndValueAndParameters::valuePhrase)
                .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String multiRowInsertValuesPhrase(int rowCount) {
        return IntStream.range(0, rowCount)
                .mapToObj(this::toSingleRowOfValues)
                .collect(Collectors.joining(", ", "values ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private String toSingleRowOfValues(int row) {
        return fieldsAndValues.stream()
                .map(FieldAndValueAndParameters::valuePhrase)
                .map(s -> String.format(s, row))
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public Map<String, Object> parameters() {
        return fieldsAndValues.stream()
                .map(FieldAndValueAndParameters::parameters)
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }

    public static Collector<FieldAndValueAndParameters, FieldAndValueCollector, FieldAndValueCollector> collect() {
        return Collector.of(FieldAndValueCollector::new,
                FieldAndValueCollector::add,
                FieldAndValueCollector::merge);
    }
}
