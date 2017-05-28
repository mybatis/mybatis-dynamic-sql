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
package org.mybatis.dynamic.sql.update;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.AbstractColumnAndValue;
import org.mybatis.dynamic.sql.where.WhereModel;

public class UpdateModel {
    private SqlTable table;
    private Optional<WhereModel> whereModel;
    private List<AbstractColumnAndValue> columnValues;
    
    private UpdateModel() {
        super();
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<WhereModel> whereModel() {
        return whereModel;
    }
    
    public Stream<AbstractColumnAndValue> columnValues() {
        return columnValues.stream();
    }
    
    public static class Builder {
        private SqlTable table;
        private WhereModel whereModel;
        private List<AbstractColumnAndValue> columnValues;
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withColumnValues(List<AbstractColumnAndValue> columnValues) {
            this.columnValues = columnValues;
            return this;
        }
        
        public Builder withWhereModel(WhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }
        
        public UpdateModel build() {
            UpdateModel model = new UpdateModel();
            model.table = table;
            model.columnValues = columnValues;
            model.whereModel = Optional.ofNullable(whereModel);
            return model;
        }
    }
}
