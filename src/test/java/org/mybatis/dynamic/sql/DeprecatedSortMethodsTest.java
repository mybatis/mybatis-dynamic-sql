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
package org.mybatis.dynamic.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.common.OrderByRenderer;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Messages;

class DeprecatedSortMethodsTest {

    @Test
    void bothMethodsExist() {
        SortSpecification ss = new SortSpecification() {
            @Override
            public SortSpecification descending() {
                return this;
            }

            @Override
            public String orderByName() {
                return "id";
            }

            @Override
            public boolean isDescending() {
                return true;
            }
       };

        OrderByModel model = OrderByModel.of(List.of(ss));

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .withStatementConfiguration(new StatementConfiguration())
                .build();
        OrderByRenderer renderer = new OrderByRenderer(renderingContext);
        FragmentAndParameters fp = renderer.render(model);
        assertThat(fp.fragment()).isEqualTo("order by id DESC");
    }

    @Test
    void orderByNameMethodMissing() {
        SortSpecification ss = new SortSpecification() {
            @Override
            public SortSpecification descending() {
                return this;
            }

            @Override
            public boolean isDescending() {
                return true;
            }
        };

        OrderByModel model = OrderByModel.of(List.of(ss));

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .withStatementConfiguration(new StatementConfiguration())
                .build();
        OrderByRenderer renderer = new OrderByRenderer(renderingContext);
        assertThatExceptionOfType(DynamicSqlException.class)
                .isThrownBy(() -> renderer.render(model))
                .withMessage(Messages.getString("ERROR.44"));
    }

    @Test
    void isDescendingMethodMissing() {
        SortSpecification ss = new SortSpecification() {
            @Override
            public SortSpecification descending() {
                return this;
            }

            @Override
            public String orderByName() {
                return "id";
            }
        };

        OrderByModel model = OrderByModel.of(List.of(ss));

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .withStatementConfiguration(new StatementConfiguration())
                .build();
        OrderByRenderer renderer = new OrderByRenderer(renderingContext);
        assertThatExceptionOfType(DynamicSqlException.class)
                .isThrownBy(() -> renderer.render(model))
                .withMessage(Messages.getString("ERROR.44"));
    }

    @Test
    void bothMethodsMissing() {
        SortSpecification ss = new SortSpecification() {
            @Override
            public SortSpecification descending() {
                return this;
            }
        };

        OrderByModel model = OrderByModel.of(List.of(ss));

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .withStatementConfiguration(new StatementConfiguration())
                .build();
        OrderByRenderer renderer = new OrderByRenderer(renderingContext);
        assertThatExceptionOfType(DynamicSqlException.class)
                .isThrownBy(() -> renderer.render(model))
                .withMessage(Messages.getString("ERROR.44"));
    }
}
