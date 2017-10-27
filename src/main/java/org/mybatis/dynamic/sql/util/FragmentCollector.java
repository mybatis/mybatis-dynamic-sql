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
package org.mybatis.dynamic.sql.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class FragmentCollector<T extends FragmentCollector<T>> {
    protected List<String> fragments = new ArrayList<>();
    protected Map<String, Object> parameters = new HashMap<>();
    
    protected FragmentCollector() {
        super();
    }
    
    public void add(FragmentAndParameters fragmentAndParameters) {
        fragments.add(fragmentAndParameters.fragment());
        parameters.putAll(fragmentAndParameters.parameters());
    }
    
    public T merge(T other) {
        fragments.addAll(other.fragments);
        parameters.putAll(other.parameters);
        return getThis();
    }
    
    public Stream<String> fragments() {
        return fragments.stream();
    }
    
    public Map<String, Object> parameters() {
        return parameters;
    }
    
    public abstract T getThis();
    
}