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
package org.mybatis.dynamic.sql.where;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.dsl.BooleanOperations;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;

public class StandaloneWhereBuilder implements BooleanOperations<StandaloneWhereBuilder>,
        ConfigurableStatement<StandaloneWhereBuilder>, Buildable<WhereModel> {
    private @Nullable SqlCriterion initialCriterion;
    private final List<AndOrCriteriaGroup> subCriteria = new ArrayList<>();
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    public StandaloneWhereBuilder() {}

    public StandaloneWhereBuilder(SqlCriterion initialCriterion, List<AndOrCriteriaGroup> subCriteria) {
        this.initialCriterion = initialCriterion;
        this.subCriteria.addAll(subCriteria);
    }

    @Override
    public StandaloneWhereBuilder addSubCriterion(AndOrCriteriaGroup subCriterion) {
        subCriteria.add(subCriterion);
        return this;
    }

    @Override
    public WhereModel build() {
        return new WhereModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    @Override
    public StandaloneWhereBuilder configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public WhereApplier toWhereApplier() {
        // TODO - deal with null
        return new WhereApplier(initialCriterion, subCriteria);
    }
}
