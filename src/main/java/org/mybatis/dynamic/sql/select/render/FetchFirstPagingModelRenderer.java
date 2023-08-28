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

import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.InternalError;
import org.mybatis.dynamic.sql.util.Messages;

public class FetchFirstPagingModelRenderer {
    private final RenderingContext renderingContext;
    private final PagingModel pagingModel;

    public FetchFirstPagingModelRenderer(RenderingContext renderingContext, PagingModel pagingModel) {
        this.renderingContext = renderingContext;
        this.pagingModel = pagingModel;
    }

    public FragmentAndParameters render() {
        return pagingModel.offset()
                .map(this::renderWithOffset)
                .orElseGet(this::renderFetchFirstRowsOnly);
    }

    private FragmentAndParameters renderWithOffset(Long offset) {
        return pagingModel.fetchFirstRows()
                .map(ffr -> renderOffsetAndFetchFirstRows(offset, ffr))
                .orElseGet(() -> renderOffsetOnly(offset));
    }

    private FragmentAndParameters renderFetchFirstRowsOnly() {
        return pagingModel.fetchFirstRows().map(this::renderFetchFirstRowsOnly)
                .orElseThrow(() ->
                        new InvalidSqlException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13)));
    }

    private FragmentAndParameters renderFetchFirstRowsOnly(Long fetchFirstRows) {
        String mapKey = renderingContext.nextMapKey();
        return FragmentAndParameters
                .withFragment("fetch first " + renderPlaceholder(mapKey) //$NON-NLS-1$
                    + " rows only") //$NON-NLS-1$
                .withParameter(mapKey, fetchFirstRows)
                .build();
    }

    private FragmentAndParameters renderOffsetOnly(Long offset) {
        String mapKey = renderingContext.nextMapKey();
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(mapKey) //$NON-NLS-1$
                + " rows") //$NON-NLS-1$
                .withParameter(mapKey, offset)
                .build();
    }

    private FragmentAndParameters renderOffsetAndFetchFirstRows(Long offset, Long fetchFirstRows) {
        String mapKey1 = renderingContext.nextMapKey();
        String mapKey2 = renderingContext.nextMapKey();
        return FragmentAndParameters.withFragment("offset " + renderPlaceholder(mapKey1) //$NON-NLS-1$
                + " rows fetch first " + renderPlaceholder(mapKey2) //$NON-NLS-1$
                + " rows only") //$NON-NLS-1$
                .withParameter(mapKey1, offset)
                .withParameter(mapKey2, fetchFirstRows)
                .build();
    }

    private String renderPlaceholder(String parameterName) {
        return renderingContext.renderingStrategy()
                .getFormattedJdbcPlaceholder(renderingContext.parameterName(), parameterName);
    }
}
