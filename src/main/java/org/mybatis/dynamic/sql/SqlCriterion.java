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

public abstract class SqlCriterion {

    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    protected SqlCriterion(AbstractBuilder<?> builder) {
        subCriteria.addAll(builder.subCriteria);
    }

    public List<AndOrCriteriaGroup> subCriteria() {
        return Collections.unmodifiableList(subCriteria);
    }

    public abstract <R> R accept(SqlCriterionVisitor<R> visitor);

    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

        public T withSubCriteria(List<AndOrCriteriaGroup> subCriteria) {
            this.subCriteria.addAll(subCriteria);
            return getThis();
        }

        protected abstract T getThis();
    }
}
