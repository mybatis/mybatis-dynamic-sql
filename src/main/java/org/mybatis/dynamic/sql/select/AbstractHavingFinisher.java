/*
 *    Copyright 2016-2025 the original author or authors.
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

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;

public abstract class AbstractHavingFinisher<T extends AbstractHavingFinisher<T>>
        extends AbstractBooleanExpressionDSL<T> {
    void initialize(SqlCriterion sqlCriterion) {
        setInitialCriterion(sqlCriterion, StatementType.HAVING);
    }

    void initialize(@Nullable SqlCriterion sqlCriterion, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(sqlCriterion, StatementType.HAVING);
        super.subCriteria.addAll(subCriteria);
    }

    protected HavingModel buildModel() {
        return new HavingModel.Builder()
                .withInitialCriterion(getInitialCriterion())
                .withSubCriteria(subCriteria)
                .build();
    }
}
