/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.render;

import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class FetchFirstPagingModelRenderer {
    private static final String FETCH_FIRST_ROWS_PARAMETER = "_fetchFirstRows"; //$NON-NLS-1$
    private static final String OFFSET_PARAMETER = "_offset"; //$NON-NLS-1$
    private RenderingStrategy renderingStrategy;
    private PagingModel pagingModel;

    public FetchFirstPagingModelRenderer(RenderingStrategy renderingStrategy,
            PagingModel pagingModel) {
        this.renderingStrategy = renderingStrategy;
        this.pagingModel = pagingModel;
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
        return FragmentAndParameters
                .withFragment("fetch first " + renderPlaceholder(FETCH_FIRST_ROWS_PARAMETER) //$NON-NLS-1$
                + " rows only") //$NON-NLS-1$
                .withParameter(FETCH_FIRST_ROWS_PARAMETER, fetchFirstRows)
                .buildOptional();
    }
    
    private Optional<FragmentAndParameters> renderOffsetOnly(Long offset) {
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(OFFSET_PARAMETER) //$NON-NLS-1$
                    + " rows") //$NON-NLS-1$
                .withParameter(OFFSET_PARAMETER, offset)
                .buildOptional();
    }
    
    private Optional<FragmentAndParameters> renderOffsetAndFetchFirstRows(Long offset, Long fetchFirstRows) {
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(OFFSET_PARAMETER) //$NON-NLS-1$
                    + " rows fetch first " + renderPlaceholder(FETCH_FIRST_ROWS_PARAMETER) //$NON-NLS-1$
                    + " rows only") //$NON-NLS-1$
                .withParameter(OFFSET_PARAMETER, offset)
                .withParameter(FETCH_FIRST_ROWS_PARAMETER, fetchFirstRows)
                .buildOptional();
    }
    
    private String renderPlaceholder(String parameterName) {
        return renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX,
                parameterName); 
    }
}
