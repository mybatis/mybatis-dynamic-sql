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
package org.mybatis.dynamic.sql.select.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class QueryExpressionCollector {
    private List<String> queryExpressions = new ArrayList<>();
    private Map<String, Object> parameters = new HashMap<>();
    
    private QueryExpressionCollector() {
        super();
    }
    
    private void add(QueryExpression queryExpression) {
        queryExpressions.add(queryExpression.queryExpression());
        parameters.putAll(queryExpression.parameters());
    }
    
    private QueryExpressionCollector merge(QueryExpressionCollector other) {
        queryExpressions.addAll(other.queryExpressions);
        parameters.putAll(other.parameters);
        return this;
    }
    
    public String queryExpression() {
        return queryExpressions.stream().collect(Collectors.joining(" ")); //$NON-NLS-1$
    }
    
    public Map<String, Object> parameters() {
        return parameters;
    }
    
    public static Collector<QueryExpression, QueryExpressionCollector, QueryExpressionCollector> collect() {
        return Collector.of(QueryExpressionCollector::new,
                QueryExpressionCollector::add,
                QueryExpressionCollector::merge);
    }
}
