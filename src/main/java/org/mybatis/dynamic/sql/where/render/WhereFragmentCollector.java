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
package org.mybatis.dynamic.sql.where.render;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class WhereFragmentCollector extends FragmentCollector<WhereFragmentCollector> {
    
    public WhereFragmentCollector() {
        super();
    }
    
    public WhereSupport buildWhereSupport() {
        return new WhereSupport.Builder()
                .withWhereClause(calculateWhereClause())
                .withParameters(parameters)
                .build();
    }
    
    private String calculateWhereClause() {
        return fragments()
                .collect(Collectors.joining(" ", "where ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    @Override
    public WhereFragmentCollector getThis() {
        return this;
    }
    
    public void add(Triple triple) {
        fragments.add(triple.jdbcPlaceholder());
        parameters.put(triple.mapKey(), triple.value());
    }
    
    public static Collector<Triple, WhereFragmentCollector, WhereFragmentCollector> collectTriples() {
        return Collector.of(WhereFragmentCollector::new,
                WhereFragmentCollector::add,
                WhereFragmentCollector::merge);
    }
    
    public static Collector<FragmentAndParameters, WhereFragmentCollector, WhereSupport> toWhereSupport() {
        return Collector.of(WhereFragmentCollector::new,
                WhereFragmentCollector::add,
                WhereFragmentCollector::merge,
                WhereFragmentCollector::buildWhereSupport);
    }

    public static Collector<FragmentAndParameters, WhereFragmentCollector, WhereFragmentCollector> collect() {
        return Collector.of(WhereFragmentCollector::new,
                WhereFragmentCollector::add,
                WhereFragmentCollector::merge);
    }
    
    public static class Triple {
        private String mapKey;
        private String jdbcPlaceholder;
        private Object value;
        
        private Triple() {
            super();
        }
        
        public String mapKey() {
            return mapKey;
        }
        
        public String jdbcPlaceholder() {
            return jdbcPlaceholder;
        }
        
        public Object value() {
            return value;
        }
        
        public static Triple of(String mapKey, String jdbcPlaceholder, Object value) {
            Triple triple = new Triple();
            triple.mapKey = mapKey;
            triple.jdbcPlaceholder = jdbcPlaceholder;
            triple.value = value;
            return triple;
        }
    }
}
