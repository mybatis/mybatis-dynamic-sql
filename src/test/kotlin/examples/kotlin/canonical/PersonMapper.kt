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
package examples.kotlin.canonical

import org.apache.ibatis.annotations.*
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 *
 * Note: this is the canonical mapper with the new style methods
 * and represents the desired output for MyBatis Generator
 *
 */
@Mapper
interface PersonMapper {

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type = SqlProviderAdapter::class, method = "delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    fun insert(insertStatement: InsertStatementProvider<PersonRecord>): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "insertMultiple")
    fun insertMultiple(insertStatement: MultiRowInsertStatementProvider<PersonRecord>): Int

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(id = "PersonResult", value = [
        Result(column = "A_ID", property = "id", jdbcType = JdbcType.INTEGER, id = true),
        Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
        Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR, typeHandler = LastNameTypeHandler::class),
        Result(column = "birth_date", property = "birthDate", jdbcType = JdbcType.DATE),
        Result(column = "employed", property = "employed", jdbcType = JdbcType.VARCHAR, typeHandler = YesNoTypeHandler::class),
        Result(column = "occupation", property = "occupation", jdbcType = JdbcType.VARCHAR),
        Result(column = "address_id", property = "addressId", jdbcType = JdbcType.INTEGER)])
    fun selectMany(selectStatement: SelectStatementProvider): List<PersonRecord>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("PersonResult")
    fun selectOne(selectStatement: SelectStatementProvider): PersonRecord?

    @UpdateProvider(type = SqlProviderAdapter::class, method = "update")
    fun update(updateStatement: UpdateStatementProvider): Int
}
