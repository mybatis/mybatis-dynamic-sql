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
package examples.kotlin

import org.apache.ibatis.annotations.*
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

interface PersonMapper {
    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    fun insert(insertStatement: InsertStatementProvider<PersonRecord>): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "insertMultiple")
    fun insertMultiple(multipleInsertStatement: MultiRowInsertStatementProvider<PersonRecord>): Int

    @UpdateProvider(type = SqlProviderAdapter::class, method = "update")
    fun update(updateStatement: UpdateStatementProvider): Int

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(id = "PersonRecordResult", value = [
        Result(column = "a_id", property = "id"),
        Result(column = "first_name", property = "firstName"),
        Result(column = "last_name", property = "lastName"),
        Result(column = "birth_date", property = "birthDate"),
        Result(column = "employed", property = "employed", typeHandler = YesNoTypeHandler::class),
        Result(column = "occupation", property = "occupation"),
        Result(column = "address_id", property = "addressId")
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonRecord>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("PersonRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): PersonRecord?

    @DeleteProvider(type = SqlProviderAdapter::class, method = "delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun count(selectStatement: SelectStatementProvider): Long
}
