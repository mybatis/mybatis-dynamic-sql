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
package org.mybatis.dynamic.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a criteria group with either an AND or an OR connector.
 * This class is intentionally NOT derived from SqlCriterion because we only want it to be
 * available where an AND or an OR condition is appropriate.
 *
 * @author Jeff Butler
 *
 * @since 1.4.0
 */
public class AndOrCriteriaGroup {
    private final String connector;
    private final SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria;

    private AndOrCriteriaGroup(Builder builder) {
        connector = Objects.requireNonNull(builder.connector);
        initialCriterion = builder.initialCriterion;
        subCriteria = builder.subCriteria;
    }

    public String connector() {
        return connector;
    }

    public Optional<SqlCriterion> initialCriterion() {
        return Optional.ofNullable(initialCriterion);
    }

    public List<AndOrCriteriaGroup> subCriteria() {
        return Collections.unmodifiableList(subCriteria);
    }

    public static class Builder {
        private String connector;
        private SqlCriterion initialCriterion;
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public Builder withConnector(String connector) {
            this.connector = connector;
            return this;
        }

        public Builder withInitialCriterion(SqlCriterion initialCriterion) {
            this.initialCriterion = initialCriterion;
            return this;
        }

        public Builder withSubCriteria(List<AndOrCriteriaGroup> subCriteria) {
            this.subCriteria.addAll(subCriteria);
            return this;
        }

        public AndOrCriteriaGroup build() {
            return new AndOrCriteriaGroup(this);
        }
    }
}
