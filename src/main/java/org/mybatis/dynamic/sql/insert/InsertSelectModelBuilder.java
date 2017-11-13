/**
 *    Copyright 2016-2017 the original author or authors.
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.Buildable;
import org.mybatis.dynamic.sql.select.SelectModel;

public class InsertSelectModelBuilder {

    private SqlTable table;
    private Optional<List<SqlColumn<?>>> columns;
    private SelectModel selectModel;
    
    private InsertSelectModelBuilder(SqlTable table, List<SqlColumn<?>> columns, SelectModel selectModel) {
        this.table = table;
        this.columns = Optional.of(columns);
        this.selectModel = selectModel;
    }
    
    private InsertSelectModelBuilder(SqlTable table, SelectModel selectModel) {
        this.table = table;
        this.columns = Optional.empty();
        this.selectModel = selectModel;
    }
    
    public InsertSelectModel build() {
        return new InsertSelectModel.Builder()
                .withTable(table)
                .withColumns(columns)
                .withSelectModel(selectModel)
                .build();
    }
    
    public static InsertColumnGatherer insertInto(SqlTable table) {
        return new InsertColumnGatherer(table);
    }

    public static class InsertColumnGatherer {
        private SqlTable table;
        
        private InsertColumnGatherer(SqlTable table) {
            this.table = table;
        }
        
        public SelectGatherer withColumnList(SqlColumn<?>...columns) {
            return new SelectGatherer(table, Arrays.asList(columns));
        }

        public InsertSelectModelBuilder withSelectStatement(Buildable<SelectModel> selectModelBuilder) {
            return new InsertSelectModelBuilder(table, selectModelBuilder.build());
        }
    }
    
    public static class SelectGatherer {
        private SqlTable table;
        private List<SqlColumn<?>> columns;
        
        private SelectGatherer(SqlTable table, List<SqlColumn<?>> columns) {
            this.table = table;
            this.columns = columns;
        }
        
        public InsertSelectModelBuilder withSelectStatement(Buildable<SelectModel> selectModelBuilder) {
            return new InsertSelectModelBuilder(table, columns, selectModelBuilder.build());
        }
    }
}
