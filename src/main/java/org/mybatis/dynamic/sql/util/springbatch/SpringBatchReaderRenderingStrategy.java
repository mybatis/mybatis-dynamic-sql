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

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.MyBatis3RenderingStrategy;

/**
 * This rendering strategy should be used for MyBatis3 statements using one of the 
 * Spring batch readers supplied by mybatis-spring integration (http://www.mybatis.org/spring/).
 * Those readers are MyBatisPagingItemReader and MyBatisCursorItemReader.
 *
 */
public class SpringBatchReaderRenderingStrategy extends MyBatis3RenderingStrategy {

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        String newPrefix = SpringBatchUtility.PARAMETER_KEY + "." + prefix; //$NON-NLS-1$
        return super.getFormattedJdbcPlaceholder(column, newPrefix, parameterName);
    }
}
