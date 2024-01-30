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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;

import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
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
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo();
        return FragmentAndParameters.withFragment("limit " + parameterInfo.renderedPlaceHolder()) //$NON-NLS-1$
                .withParameter(parameterInfo.parameterMapKey(), limit)
                .build();
    }

    private FragmentAndParameters renderLimitAndOffset(Long offset) {
        RenderedParameterInfo parameterInfo1 = renderingContext.calculateParameterInfo();
        RenderedParameterInfo parameterInfo2 = renderingContext.calculateParameterInfo();
        return FragmentAndParameters.withFragment("limit " + parameterInfo1.renderedPlaceHolder() //$NON-NLS-1$
                    + " offset " + parameterInfo2.renderedPlaceHolder()) //$NON-NLS-1$
                .withParameter(parameterInfo1.parameterMapKey(), limit)
                .withParameter(parameterInfo2.parameterMapKey(), offset)
                .build();
    }
}
