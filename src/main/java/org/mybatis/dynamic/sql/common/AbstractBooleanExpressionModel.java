/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;

public abstract class AbstractBooleanExpressionModel {
    private final SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    protected AbstractBooleanExpressionModel(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        this.initialCriterion = initialCriterion;
        this.subCriteria.addAll(subCriteria);
    }

    public Optional<SqlCriterion> initialCriterion() {
        return Optional.ofNullable(initialCriterion);
    }

    public List<AndOrCriteriaGroup> subCriteria() {
        return Collections.unmodifiableList(subCriteria);
    }
}
