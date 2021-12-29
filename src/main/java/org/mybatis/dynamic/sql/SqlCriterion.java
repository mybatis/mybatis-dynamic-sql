/*
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
package org.mybatis.dynamic.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SqlCriterion {

    private final String connector;
    private final List<SqlCriterion> subCriteria;

    protected SqlCriterion(AbstractBuilder<?> builder) {
        connector = builder.connector;
        subCriteria = Objects.requireNonNull(builder.subCriteria);
    }

    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }

    public <R> Stream<R> mapSubCriteria(Function<SqlCriterion, R> mapper) {
        return subCriteria.stream().map(mapper);
    }

    public abstract <R> R accept(SqlCriterionVisitor<R> visitor);

    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private String connector;
        private final List<SqlCriterion> subCriteria = new ArrayList<>();

        public T withConnector(String connector) {
            this.connector = connector;
            return getThis();
        }

        public T withCriterion(SqlCriterion criterion) {
            this.subCriteria.add(Objects.requireNonNull(criterion));
            return getThis();
        }

        public T withSubCriteria(List<SqlCriterion> subCriteria) {
            this.subCriteria.addAll(subCriteria);
            return getThis();
        }

        protected abstract T getThis();
    }
}
