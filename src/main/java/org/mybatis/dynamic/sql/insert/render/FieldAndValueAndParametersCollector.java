/**
 *    Copyright 2016-2020 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FieldAndValueAndParametersCollector {
    
    private List<Optional<FieldAndValueAndParameters>> fieldsAndValue = new ArrayList<>();
    
    public FieldAndValueAndParametersCollector() {
        super();
    }
    
    public void add(Optional<FieldAndValueAndParameters> fieldAndValue) {
        fieldsAndValue.add(fieldAndValue);
    }
    
    public FieldAndValueAndParametersCollector merge(FieldAndValueAndParametersCollector other) {
        fieldsAndValue.addAll(other.fieldsAndValue);
        return this;
    }

    public String columnsPhrase() {
        return fieldsAndValue.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FieldAndValueAndParameters::fieldName)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String valuesPhrase() {
        return fieldsAndValue.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FieldAndValueAndParameters::valuePhrase)
                .collect(Collectors.joining(", ", "values (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public Map<String, Object> parameters() {
        return fieldsAndValue.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FieldAndValueAndParameters::parameters)
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }
    
    public static Collector<Optional<FieldAndValueAndParameters>, FieldAndValueAndParametersCollector,
            FieldAndValueAndParametersCollector> collect() {
        return Collector.of(FieldAndValueAndParametersCollector::new,
                FieldAndValueAndParametersCollector::add,
                FieldAndValueAndParametersCollector::merge);
    }
}
