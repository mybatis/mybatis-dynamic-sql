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

import org.mybatis.dynamic.sql.delete.DeleteSupportBuilder;
import org.mybatis.dynamic.sql.insert.InsertSupportBuilder;
import org.mybatis.dynamic.sql.select.SelectCountOrDistinctBuilder;
import org.mybatis.dynamic.sql.select.SelectSupportBuilder;
import org.mybatis.dynamic.sql.update.UpdateSupportBuilder;

public interface SqlBuilder {

    public static DeleteSupportBuilder deleteFrom(SqlTable table) {
        return DeleteSupportBuilder.of(table);
    }

    public static <T> InsertSupportBuilder<T> insert(T record) {
        return InsertSupportBuilder.of(record);
    }
    
    public static SelectSupportBuilder select(SqlColumn<?>...columns) {
        return SelectSupportBuilder.of(columns);
    }
    
    public static SelectCountOrDistinctBuilder select() {
        return new SelectCountOrDistinctBuilder();
    }
    
    public static UpdateSupportBuilder update(SqlTable table) {
        return UpdateSupportBuilder.of(table);
    }
}
