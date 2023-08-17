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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class LimitAndOffsetPagingModelRenderer {
    private final RenderingContext renderingContext;
    private final Long limit;
    private final PagingModel pagingModel;

    public LimitAndOffsetPagingModelRenderer(RenderingContext renderingContext,
                                             Long limit, PagingModel pagingModel) {
        this.renderingContext = renderingContext;
        this.limit = Objects.requireNonNull(limit);
        this.pagingModel = pagingModel;
    }

    public FragmentAndParameters render() {
        return pagingModel.offset().map(this::renderLimitAndOffset)
                .orElseGet(this::renderLimitOnly);
    }

    private FragmentAndParameters renderLimitOnly() {
        String mapKey = renderingContext.nextMapKey();
        return FragmentAndParameters.withFragment("limit " + renderPlaceholder(mapKey)) //$NON-NLS-1$
                .withParameter(mapKey, limit)
                .build();
    }

    private FragmentAndParameters renderLimitAndOffset(Long offset) {
        String mapKey1 = renderingContext.nextMapKey();
        String mapKey2 = renderingContext.nextMapKey();
        return FragmentAndParameters.withFragment("limit " + renderPlaceholder(mapKey1) //$NON-NLS-1$
                    + " offset " + renderPlaceholder(mapKey2)) //$NON-NLS-1$
                .withParameter(mapKey1, limit)
                .withParameter(mapKey2, offset)
                .build();
    }

    private String renderPlaceholder(String parameterName) {
        return renderingContext.renderingStrategy()
                .getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX, parameterName);
    }
}
