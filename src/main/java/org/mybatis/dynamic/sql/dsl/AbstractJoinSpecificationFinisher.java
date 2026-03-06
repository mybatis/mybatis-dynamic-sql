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

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;

public abstract class AbstractJoinSpecificationFinisher<D extends JoinOperations<D, F>,
        F extends AbstractJoinSpecificationFinisher<D, F>> implements BooleanOperations<F> {
    private @Nullable SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();
    private @Nullable TableExpression joinTable;
    private @Nullable JoinType joinType;

    @Override
    public F addSubCriterion(AndOrCriteriaGroup subCriterion) {
        subCriteria.add(subCriterion);
        return getThis();
    }

    public void setInitialCriterion(SqlCriterion initialCriterion) {
        this.initialCriterion = initialCriterion;
    }

    protected abstract F getThis();

    void setJoinTable(TableExpression joinTable) {
        this.joinTable = joinTable;
    }

    void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    protected JoinSpecification toJoinSpecification() {
        return JoinSpecification.withJoinTable(Objects.requireNonNull(joinTable))
                .withJoinType(Objects.requireNonNull(joinType))
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }

    public abstract D endJoinSpecification();

    protected abstract void addTableAlias(SqlTable table, String tableAlias);
}
