/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.LinkedHashMap;



public class FragmentCollector {
    final List<String> fragments = new ArrayList<>();
    final Map<String, Object> parameters = new HashMap<>();

    public FragmentCollector() {
        super();
    }

    private FragmentCollector(FragmentAndParameters initialFragment) {
        add(initialFragment);
    }

    public void add(FragmentAndParameters fragmentAndParameters) {
        fragments.add(fragmentAndParameters.fragment());
        parameters.putAll(fragmentAndParameters.parameters());
    }

    public FragmentCollector merge(FragmentCollector other) {
        fragments.addAll(other.fragments);
        parameters.putAll(other.parameters);
        return this;
    }

    public Optional<String> firstFragment() {
        return fragments.stream().findFirst();
    }

    public String collectFragments(Collector<CharSequence, ?, String> fragmentCollector) {
        return fragments.stream().collect(fragmentCollector);
    }

    public FragmentAndParameters toFragmentAndParameters(Collector<CharSequence, ?, String> fragmentCollector) {
        return FragmentAndParameters.withFragment(collectFragments(fragmentCollector))
                .withParameters(parameters())
                .build();
    }

    public Map<String, Object> parameters() {
    return fragments.stream()
            .map(FragmentAndParameters::parameters)   
            .collect(
                LinkedHashMap::new,  
                (map, fragmentParams) -> map.putAll(fragmentParams), 
                Map::putAll          
            );
}




    public boolean hasMultipleFragments() {
        return fragments.size() > 1;
    }

    public boolean isEmpty() {
        return fragments.isEmpty();
    }

    public static Collector<FragmentAndParameters, FragmentCollector, FragmentCollector> collect() {
        return Collector.of(FragmentCollector::new,
                FragmentCollector::add,
                FragmentCollector::merge);
    }

    public static Collector<FragmentAndParameters, FragmentCollector, FragmentCollector> collect(
            FragmentAndParameters initialFragment) {
        return Collector.of(() -> new FragmentCollector(initialFragment),
                FragmentCollector::add,
                FragmentCollector::merge);
    }
}
