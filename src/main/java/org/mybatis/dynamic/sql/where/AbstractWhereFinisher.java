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
package org.mybatis.dynamic.sql.where;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionDSL;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

public abstract class AbstractWhereFinisher<T extends AbstractWhereFinisher<T>> extends AbstractBooleanExpressionDSL<T>
        implements ConfigurableStatement<T> {
    private final ConfigurableStatement<?> parentStatement;

    protected AbstractWhereFinisher(ConfigurableStatement<?> parentStatement) {
        this.parentStatement = Objects.requireNonNull(parentStatement);
    }

    void initialize(SqlCriterion sqlCriterion) {
        setInitialCriterion(sqlCriterion, StatementType.WHERE);
    }

    void initialize(SqlCriterion sqlCriterion, List<AndOrCriteriaGroup> subCriteria) {
        setInitialCriterion(sqlCriterion, StatementType.WHERE);
        super.subCriteria.addAll(subCriteria);
    }

    @Override
    public T configureStatement(Consumer<StatementConfiguration> consumer) {
        parentStatement.configureStatement(consumer);
        return getThis();
    }

    protected EmbeddedWhereModel buildModel() {
        return new EmbeddedWhereModel.Builder()
                .withInitialCriterion(getInitialCriterion())
                .withSubCriteria(subCriteria)
                .build();
    }
}
