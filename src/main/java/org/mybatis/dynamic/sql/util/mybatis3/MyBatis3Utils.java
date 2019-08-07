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
package org.mybatis.dynamic.sql.util.mybatis3;

import java.util.function.ToIntFunction;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

/**
 * Utility functions for building MyBatis3 mappers.
 * 
 * @author Jeff Butler
 *
 */
public class MyBatis3Utils {
    private MyBatis3Utils() {}

    /**
     * Initiates a delete statement using the non-boxing adapter.
     * 
     * @param mapper a MyBatis3 mapper delete method
     * @param table the table to delete from
     * @return a partially completed DeleteDSL
     */
    public static DeleteDSL<MyBatis3DeleteModelToIntAdapter> deleteFrom(ToIntFunction<DeleteStatementProvider> mapper,
            SqlTable table) {
        return DeleteDSL.deleteFrom(dm -> MyBatis3DeleteModelToIntAdapter.of(dm, mapper), table);
    }

    /**
     * Initiates an update statement using the non-boxing adapter.
     * 
     * @param mapper a MyBatis3 mapper update method
     * @param table the table to update
     * @return a partially completed UpdateDSL
     */
    public static UpdateDSL<MyBatis3UpdateModelToIntAdapter> update(ToIntFunction<UpdateStatementProvider> mapper,
            SqlTable table) {
        return UpdateDSL.update(um -> MyBatis3UpdateModelToIntAdapter.of(um, mapper), table);
    }
}
