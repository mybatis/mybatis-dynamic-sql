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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.AbstractColumnMapping;
import org.mybatis.dynamic.sql.where.WhereModel;

public class UpdateModel {
    private SqlTable table;
    private WhereModel whereModel;
    private List<AbstractColumnMapping> columnValues;
    
    private UpdateModel() {
        super();
    }
    
    public SqlTable table() {
        return table;
    }
    
    public Optional<WhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }
    
    public Stream<AbstractColumnMapping> columnValues() {
        return columnValues.stream();
    }
    
    public static UpdateModel of(SqlTable table, Stream<AbstractColumnMapping> columnValues) {
        return of(table, null, columnValues);
    }

    public static UpdateModel of(SqlTable table, WhereModel whereModel, Stream<AbstractColumnMapping> columnValues) {
        UpdateModel model = new UpdateModel();
        model.table = table;
        model.columnValues = columnValues.collect(Collectors.toList());
        model.whereModel = whereModel;
        return model;
    }
}
