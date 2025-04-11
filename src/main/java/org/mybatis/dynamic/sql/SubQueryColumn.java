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
package org.mybatis.dynamic.sql;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class SubQueryColumn implements BasicColumn {
    private final Buildable<SelectModel> subQuery;
    private @Nullable String alias;

    private SubQueryColumn(Buildable<SelectModel> subQuery) {
        this.subQuery = subQuery;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public SubQueryColumn as(String alias) {
        SubQueryColumn answer = new SubQueryColumn(subQuery);
        answer.alias = alias;
        return answer;
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return SubQueryRenderer.withSelectModel(subQuery.build())
                .withRenderingContext(renderingContext)
                .withPrefix("(") //$NON-NLS-1$
                .withSuffix(")") //$NON-NLS-1$
                .build()
                .render();
    }

    public static SubQueryColumn of(Buildable<SelectModel> subQuery) {
        return new SubQueryColumn(subQuery);
    }
}
