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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FieldAndValueAndParameters {
    private final String fieldName;
    private final String valuePhrase;
    private final Map<String, Object> parameters;

    private FieldAndValueAndParameters(Builder builder) {
        fieldName = Objects.requireNonNull(builder.fieldName);
        valuePhrase = Objects.requireNonNull(builder.valuePhrase);
        parameters = builder.parameters;
    }

    public String fieldName() {
        return fieldName;
    }

    public String valuePhrase() {
        return valuePhrase;
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public static Builder withFieldName(String fieldName) {
        return new Builder().withFieldName(fieldName);
    }

    public static class Builder {
        private String fieldName;
        private String valuePhrase;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder withValuePhrase(String valuePhrase) {
            this.valuePhrase = valuePhrase;
            return this;
        }

        public Builder withParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        public FieldAndValueAndParameters build() {
            return new FieldAndValueAndParameters(this);
        }

        public Optional<FieldAndValueAndParameters> buildOptional() {
            return Optional.of(build());
        }
    }
}
