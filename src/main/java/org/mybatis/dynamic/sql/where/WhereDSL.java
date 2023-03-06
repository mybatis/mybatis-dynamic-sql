/*
 *    Copyright 2016-2023 the original author or authors.
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

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.CriteriaGroup;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;

public class WhereDSL extends AbstractWhereStarter<WhereDSL.StandaloneWhereFinisher, WhereDSL> {
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private final StandaloneWhereFinisher whereBuilder = new StandaloneWhereFinisher();

    @Override
    public StandaloneWhereFinisher where() {
        return whereBuilder;
    }

    @Override
    public WhereDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public class StandaloneWhereFinisher extends AbstractWhereFinisher<StandaloneWhereFinisher>
            implements Buildable<WhereModel> {
        private StandaloneWhereFinisher() {
            super(statementConfiguration);
        }

        @Override
        protected StandaloneWhereFinisher getThis() {
            return this;
        }

        @NotNull
        @Override
        public WhereModel build() {
            return buildModel();
        }

        public WhereApplier toWhereApplier() {
            CriteriaGroup ic = new CriteriaGroup.Builder()
                    .withInitialCriterion(getInitialCriterion())
                    .withSubCriteria(subCriteria)
                    .build();

            return d -> d.initialize(ic);
        }
    }
}
