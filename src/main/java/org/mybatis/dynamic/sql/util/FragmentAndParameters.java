/**
 *    Copyright 2016-2018 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FragmentAndParameters {
    
    private String fragment;
    private Map<String, Object> parameters;
    
    private FragmentAndParameters(Builder builder) {
        fragment = Objects.requireNonNull(builder.fragment);
        parameters = Objects.requireNonNull(builder.parameters);
    }
    
    public String fragment() {
        return fragment;
    }
    
    public Map<String, Object> parameters() {
        return parameters;
    }
    
    public FragmentAndParameters prependFragment(String s) {
        return FragmentAndParameters.withFragment(s + " " + fragment) //$NON-NLS-1$
                .withParameters(parameters)
                .build();
    }
    
    public static Builder withFragment(String fragment) {
        return new Builder().withFragment(fragment);
    }
    
    public static class Builder {
        private String fragment;
        private Map<String, Object> parameters = new HashMap<>();
        
        public Builder withFragment(String fragment) {
            this.fragment = fragment;
            return this;
        }
        
        public Builder withParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public FragmentAndParameters build() {
            return new FragmentAndParameters(this);
        }
        
        public Optional<FragmentAndParameters> buildOptional() {
            return Optional.of(build());
        }
    }
}
