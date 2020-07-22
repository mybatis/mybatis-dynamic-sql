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
package org.mybatis.dynamic.sql.update.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class OptionalFragmentCollector {
    List<Optional<FragmentAndParameters>> fragments = new ArrayList<>();
    
    OptionalFragmentCollector() {
        super();
    }
    
    public void add(Optional<FragmentAndParameters> fragmentAndParameters) {
        fragments.add(fragmentAndParameters);
    }
    
    public OptionalFragmentCollector merge(OptionalFragmentCollector other) {
        fragments.addAll(other.fragments);
        return this;
    }
    
    public Stream<String> fragments() {
        return fragments.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FragmentAndParameters::fragment);
    }
    
    public Map<String, Object> parameters() {
        return fragments.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FragmentAndParameters::parameters)
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }
    
    public static Collector<Optional<FragmentAndParameters>, OptionalFragmentCollector, OptionalFragmentCollector> collect() {
        return Collector.of(OptionalFragmentCollector::new,
                OptionalFragmentCollector::add,
                OptionalFragmentCollector::merge);
    }
}
