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

import java.util.Objects;

public class FieldAndValue {
    private String fieldName;
    private String valuePhrase;
    
    private FieldAndValue(Builder builder) {
        fieldName = Objects.requireNonNull(builder.fieldName);
        valuePhrase = Objects.requireNonNull(builder.valuePhrase);
    }
    
    public String fieldName() {
        return fieldName;
    }
    
    public String valuePhrase() {
        return valuePhrase;
    }
    
    public String valuePhrase(int row) {
        return String.format(valuePhrase, row);
    }

    public static Builder withFieldName(String fieldName) {
        return new Builder().withFieldName(fieldName);
    }
    
    public static class Builder {
        private String fieldName;
        private String valuePhrase;

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder withValuePhrase(String valuePhrase) {
            this.valuePhrase = valuePhrase;
            return this;
        }
        
        public FieldAndValue build() {
            return new FieldAndValue(this);
        }
    }
}
