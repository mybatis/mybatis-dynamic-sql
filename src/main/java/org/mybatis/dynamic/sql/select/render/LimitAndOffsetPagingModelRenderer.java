/*
 *    Copyright 2016-2022 the original author or authors.
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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class LimitAndOffsetPagingModelRenderer {
    private final RenderingStrategy renderingStrategy;
    private final Long limit;
    private final PagingModel pagingModel;
    private final AtomicInteger sequence;

    public LimitAndOffsetPagingModelRenderer(RenderingStrategy renderingStrategy,
            Long limit, PagingModel pagingModel, AtomicInteger sequence) {
        this.renderingStrategy = renderingStrategy;
        this.limit = limit;
        this.pagingModel = pagingModel;
        this.sequence = sequence;
    }

    public Optional<FragmentAndParameters> render() {
        return pagingModel.offset().map(this::renderLimitAndOffset)
                .orElseGet(this::renderLimitOnly);
    }

    private Optional<FragmentAndParameters> renderLimitOnly() {
        String mapKey = RenderingStrategy.formatParameterMapKey(sequence);
        return FragmentAndParameters.withFragment("limit " + renderPlaceholder(mapKey)) //$NON-NLS-1$
                .withParameter(mapKey, limit)
                .buildOptional();
    }

    private Optional<FragmentAndParameters> renderLimitAndOffset(Long offset) {
        String mapKey1 = RenderingStrategy.formatParameterMapKey(sequence);
        String mapKey2 = RenderingStrategy.formatParameterMapKey(sequence);
        return FragmentAndParameters.withFragment("limit " + renderPlaceholder(mapKey1) //$NON-NLS-1$
                    + " offset " + renderPlaceholder(mapKey2)) //$NON-NLS-1$
                .withParameter(mapKey1, limit)
                .withParameter(mapKey2, offset)
                .buildOptional();
    }

    private String renderPlaceholder(String parameterName) {
        return renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX,
                parameterName);
    }
}
