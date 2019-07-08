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

import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SpringBatchPagingReaderSelectModel {
    
    private SelectModel selectModel;
    
    public SpringBatchPagingReaderSelectModel(SelectModel selectModel) {
        this.selectModel = selectModel;
    }

    public SelectStatementProvider render() {
        SelectStatementProvider selectStatement =
                selectModel.render(SpringBatchUtility.SPRING_BATCH_READER_RENDERING_STRATEGY);
        return new LimitAndOffsetDecorator(selectStatement);
    }

    public static class LimitAndOffsetDecorator implements SelectStatementProvider {
        private Map<String, Object> parameters = new HashMap<>();
        private String selectStatement;
        
        public LimitAndOffsetDecorator(SelectStatementProvider delegate) {
            parameters.putAll(delegate.getParameters());
            
            selectStatement = delegate.getSelectStatement()
                    + " LIMIT #{_pagesize} OFFSET #{_skiprows}"; //$NON-NLS-1$
        }

        @Override
        public Map<String, Object> getParameters() {
            return parameters;
        }

        @Override
        public String getSelectStatement() {
            return selectStatement;
        }
    }
}
