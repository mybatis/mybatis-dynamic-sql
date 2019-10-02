/**
 * Copyright 2016-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.dynamic.sql.select.render;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class FetchFirstPagingModelRenderer {
    private static final String FETCH_FIRST_ROWS_PARAMETER = "_fetchFirstRows"; //$NON-NLS-1$
    private static final String OFFSET_PARAMETER = "_offset"; //$NON-NLS-1$
    private RenderingStrategy renderingStrategy;
    private PagingModel pagingModel;
    private AtomicInteger sequence;

    public FetchFirstPagingModelRenderer(RenderingStrategy renderingStrategy,
                                         PagingModel pagingModel, AtomicInteger sequence) {
        this.renderingStrategy = renderingStrategy;
        this.pagingModel = pagingModel;
        this.sequence = sequence;
    }

    public Optional<FragmentAndParameters> render() {
        return pagingModel.offset()
                .map(this::renderWithOffset)
                .orElseGet(this::renderFetchFirstRowsOnly);
    }

    private Optional<FragmentAndParameters> renderWithOffset(Long offset) {
        return pagingModel.fetchFirstRows()
                .map(ffr -> renderOffsetAndFetchFirstRows(offset, ffr))
                .orElseGet(() -> renderOffsetOnly(offset));
    }

    private Optional<FragmentAndParameters> renderFetchFirstRowsOnly() {
        return pagingModel.fetchFirstRows().flatMap(this::renderFetchFirstRowsOnly);
    }

    private Optional<FragmentAndParameters> renderFetchFirstRowsOnly(Long fetchFirstRows) {
        String mapKey = formatParameterMapKey(FETCH_FIRST_ROWS_PARAMETER);
        return FragmentAndParameters
                .withFragment("fetch first " + renderPlaceholder(mapKey) //$NON-NLS-1$
                        + " rows only") //$NON-NLS-1$
                .withParameter(mapKey, fetchFirstRows)
                .buildOptional();
    }

    private Optional<FragmentAndParameters> renderOffsetOnly(Long offset) {
        String mapKey = formatParameterMapKey(OFFSET_PARAMETER);
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(mapKey) //$NON-NLS-1$
                + " rows") //$NON-NLS-1$
                .withParameter(mapKey, offset)
                .buildOptional();
    }

    private Optional<FragmentAndParameters> renderOffsetAndFetchFirstRows(Long offset, Long fetchFirstRows) {
        String mapKey1 = formatParameterMapKey(OFFSET_PARAMETER);
        String mapKey2 = formatParameterMapKey(FETCH_FIRST_ROWS_PARAMETER);
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(mapKey1) //$NON-NLS-1$
                + " rows fetch first " + renderPlaceholder(mapKey2) //$NON-NLS-1$
                + " rows only") //$NON-NLS-1$
                .withParameter(mapKey1, offset)
                .withParameter(mapKey2, fetchFirstRows)
                .buildOptional();
    }

    private String formatParameterMapKey(String parameterMapKey) {
        return parameterMapKey + sequence.getAndIncrement(); //$NON-NLS-1$
    }

    private String renderPlaceholder(String parameterName) {
        return renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX,
                parameterName);
    }
}
