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
import java.util.Objects;

public class FieldAndMultipleValues {
    private String fieldName;
    private List<String> valuePhrases;
    
    private FieldAndMultipleValues(Builder builder) {
        fieldName = Objects.requireNonNull(builder.fieldName);
        valuePhrases = builder.valuePhrases;
    }
    
    public String fieldName() {
        return fieldName;
    }
    
    public List<String> valuePhrases() {
        return valuePhrases;
    }
    
    public static Builder withFieldName(String fieldName) {
        return new Builder().withFieldName(fieldName);
    }
    
    public static class Builder {
        private String fieldName;
        private List<String> valuePhrases = new ArrayList<>();

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder withValuePhrases(List<String> valuePhrases) {
            this.valuePhrases.addAll(valuePhrases);
            return this;
        }
        
        public FieldAndMultipleValues build() {
            return new FieldAndMultipleValues(this);
        }
    }
}
