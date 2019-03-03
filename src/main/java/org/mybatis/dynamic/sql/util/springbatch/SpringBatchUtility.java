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
package org.mybatis.dynamic.sql.util.springbatch;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SpringBatchUtility {
    private SpringBatchUtility() {}
    
    public static final String PARAMETER_KEY = "mybatis3_dsql_query"; //$NON-NLS-1$
    
    public static final RenderingStrategy SPRING_BATCH_READER_RENDERING_STRATEGY =
            new SpringBatchReaderRenderingStrategy();
    
    public static Map<String, Object> toParameterValues(SelectStatementProvider selectStatement) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put(PARAMETER_KEY, selectStatement);
        return parameterValues;
    }

    /**
     * Select builder that renders in a manner appropriate for the MyBatisPagingItemReader.
     * 
     * <b>Important</b> rendered SQL will contain LIMIT and OFFSET clauses in the SELECT statement. If your database
     * (Oracle) does not support LIMIT and OFFSET, the queries will fail.
     * 
     * @param selectList a column list for the SELECT statement
     * @return FromGatherer used to continue a SELECT statement
     */
    public static QueryExpressionDSL.FromGatherer<SpringBatchPagingReaderSelectModel> selectForPaging(
            BasicColumn...selectList) {
        return SelectDSL.select(SpringBatchPagingReaderSelectModel::new, selectList);
    }

    /**
     * Select builder that renders in a manner appropriate for the MyBatisCursorItemReader.
     * 
     * @param selectList a column list for the SELECT statement
     * @return FromGatherer used to continue a SELECT statement
     */
    public static QueryExpressionDSL.FromGatherer<SpringBatchCursorReaderSelectModel> selectForCursor(
            BasicColumn...selectList) {
        return SelectDSL.select(SpringBatchCursorReaderSelectModel::new, selectList);
    }
}
