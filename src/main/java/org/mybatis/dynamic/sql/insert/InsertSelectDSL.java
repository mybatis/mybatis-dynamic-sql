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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;

public class InsertSelectDSL {

    private SqlTable table;
    private InsertColumnListModel columnList;
    private SelectModel selectModel;
    
    private InsertSelectDSL(SqlTable table, InsertColumnListModel columnList, SelectModel selectModel) {
        this(table, selectModel);
        this.columnList = columnList;
    }
    
    private InsertSelectDSL(SqlTable table, SelectModel selectModel) {
        this.table = Objects.requireNonNull(table);
        this.selectModel = Objects.requireNonNull(selectModel);
    }
    
    public InsertSelectModel build() {
        return InsertSelectModel.withTable(table)
                .withColumnList(columnList)
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

        public InsertSelectDSL withSelectStatement(Buildable<SelectModel> selectModelBuilder) {
            return new InsertSelectDSL(table, selectModelBuilder.build());
        }
    }
    
    public static class SelectGatherer {
        private SqlTable table;
        private InsertColumnListModel columnList;
        
        private SelectGatherer(SqlTable table, List<SqlColumn<?>> columns) {
            this.table = table;
            columnList = InsertColumnListModel.of(columns);
        }
        
        public InsertSelectDSL withSelectStatement(Buildable<SelectModel> selectModelBuilder) {
            return new InsertSelectDSL(table, columnList, selectModelBuilder.build());
        }
    }
}
