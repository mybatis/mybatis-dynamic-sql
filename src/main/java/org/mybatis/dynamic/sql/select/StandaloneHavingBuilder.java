/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.dsl.BooleanOperations;
import org.mybatis.dynamic.sql.util.Buildable;

public class StandaloneHavingBuilder implements BooleanOperations<StandaloneHavingBuilder>, Buildable<HavingModel> {
    private final SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    public StandaloneHavingBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        this.initialCriterion = initialCriterion;
        this.subCriteria.addAll(subCriteria);
    }

    @Override
    public StandaloneHavingBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
        subCriteria.add(subCriterion);
        return this;
    }

    @Override
    public HavingModel build() {
        return new HavingModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    public HavingApplier toHavingApplier() {
        return new HavingApplier(initialCriterion, subCriteria);
    }
}
