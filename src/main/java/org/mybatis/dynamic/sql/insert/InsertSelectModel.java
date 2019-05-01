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
package org.mybatis.dynamic.sql.insert;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertSelectRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModel;

public class InsertSelectModel {
    private SqlTable table;
    private InsertColumnListModel columnList;
    private SelectModel selectModel;
    
    private InsertSelectModel(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        columnList = builder.columnList;
        selectModel = Objects.requireNonNull(builder.selectModel);
    }

    public SqlTable table() {
        return table;
    }
    
    public SelectModel selectModel() {
        return selectModel;
    }
    
    public Optional<InsertColumnListModel> columnList() {
        return Optional.ofNullable(columnList);
    }
    
    public InsertSelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return InsertSelectRenderer.withInsertSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
    
    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }
    
    public static class Builder {
        private SqlTable table;
        private InsertColumnListModel columnList;
        private SelectModel selectModel;
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withColumnList(InsertColumnListModel columnList) {
            this.columnList = columnList;
            return this;
        }
        
        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }
        
        public InsertSelectModel build() {
            return new InsertSelectModel(this);
        }
    }
}
