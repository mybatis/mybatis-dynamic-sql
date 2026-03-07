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
package org.mybatis.dynamic.sql.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;

public abstract class AbstractJoinSupport<D extends JoinOperations<D, F>, F extends AbstractJoinSupport<D, F>>
        implements BooleanOperations<F> {
    private final JoinType joinType;
    private final TableExpression joinTable;
    private final SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();

    protected AbstractJoinSupport(JoinType joinType, TableExpression joinTable, SqlCriterion initialCriterion) {
        this.joinType = joinType;
        this.joinTable = joinTable;
        this.initialCriterion = initialCriterion;
    }

    @Override
    public F addSubCriterion(AndOrCriteriaGroup subCriterion) {
        subCriteria.add(subCriterion);
        return getThis();
    }

    protected abstract F getThis();

    protected JoinSpecification toJoinSpecification() {
        return JoinSpecification.withJoinTable(Objects.requireNonNull(joinTable))
                .withJoinType(Objects.requireNonNull(joinType))
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }
}
