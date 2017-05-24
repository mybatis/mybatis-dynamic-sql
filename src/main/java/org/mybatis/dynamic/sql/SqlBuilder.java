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
package org.mybatis.dynamic.sql;

import org.mybatis.dynamic.sql.delete.DeleteModelBuilder;
import org.mybatis.dynamic.sql.insert.InsertModelBuilder;
import org.mybatis.dynamic.sql.select.SelectModelBuilder;
import org.mybatis.dynamic.sql.update.UpdateModelBuilder;

public interface SqlBuilder {

    public static DeleteModelBuilder deleteFrom(SqlTable table) {
        return DeleteModelBuilder.of(table);
    }

    public static <T> InsertModelBuilder<T> insert(T record) {
        return InsertModelBuilder.insert(record);
    }
    
    public static SelectModelBuilder select(SqlColumn<?>...columns) {
        return SelectModelBuilder.of(columns);
    }
    
    public static SelectModelBuilder selectDistinct(SqlColumn<?>...columns) {
        return SelectModelBuilder.ofDistinct(columns);
    }
    
    public static UpdateModelBuilder update(SqlTable table) {
        return UpdateModelBuilder.of(table);
    }
}
