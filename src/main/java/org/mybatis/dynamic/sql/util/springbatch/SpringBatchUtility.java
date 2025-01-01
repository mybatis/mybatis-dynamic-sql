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
package org.mybatis.dynamic.sql.util.springbatch;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SpringBatchUtility {
    private SpringBatchUtility() {}

    static final String PARAMETER_KEY = "mybatis3_dsql_query"; //$NON-NLS-1$

    /**
     * Constant for use in a query intended for use with the MyBatisPagingItemReader.
     * This value will not be used in the query at runtime because MyBatis Spring integration
     * will supply a value for _skiprows.
     *
     * <p>This value can be used as a parameter for the "offset" method in a query to make the intention
     * clear that the actual runtime value will be supplied by MyBatis Spring integration.
     *
     * <p>See <a href="https://mybatis.org/spring/batch.html">https://mybatis.org/spring/batch.html</a> for details.
     */
    public static final long MYBATIS_SPRING_BATCH_SKIPROWS = -437L;

    /**
     * Constant for use in a query intended for use with the MyBatisPagingItemReader.
     * This value will not be used in the query at runtime because MyBatis Spring integration
     * will supply a value for _pagesize.
     *
     * <p>This value can be used as a parameter for the "limit" or "fetchFirst" method in a query to make the intention
     * clear that the actual runtime value will be supplied by MyBatis Spring integration.
     *
     * <p>See <a href="https://mybatis.org/spring/batch.html">https://mybatis.org/spring/batch.html</a> for details.
     */
    public static final long MYBATIS_SPRING_BATCH_PAGESIZE = -439L;

    public static final RenderingStrategy SPRING_BATCH_PAGING_ITEM_READER_RENDERING_STRATEGY =
            new SpringBatchPagingItemReaderRenderingStrategy();

    public static Map<String, Object> toParameterValues(SelectStatementProvider selectStatement) {
        var parameterValues = new HashMap<String, Object>();
        parameterValues.put(PARAMETER_KEY, selectStatement.getSelectStatement());
        parameterValues.put(RenderingStrategy.DEFAULT_PARAMETER_PREFIX, selectStatement.getParameters());
        return parameterValues;
    }
}
