/*
 *    Copyright 2016-2022 the original author or authors.
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
package examples.kotlin.mybatis3.canonical

import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.generatedAlways
import examples.kotlin.mybatis3.canonical.GeneratedAlwaysDynamicSqlSupport.lastName
import org.apache.ibatis.annotations.Flush
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.executor.BatchResult
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertMultipleWithGeneratedKeys

interface GeneratedAlwaysMapper {
    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty = "row.id,row.fullName", keyColumn = "id,full_name")
    fun insert(insertStatement: InsertStatementProvider<GeneratedAlwaysRecord>): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "generalInsert")
    @Options(useGeneratedKeys = true, keyProperty = "parameters.id,parameters.fullName", keyColumn = "id,full_name")
    fun generalInsert(insertStatement: GeneralInsertStatementProvider): Int

    @InsertProvider(type = SqlProviderAdapter::class, method = "insertMultipleWithGeneratedKeys")
    @Options(useGeneratedKeys = true, keyProperty = "records.id,records.fullName", keyColumn = "id,full_name")
    fun insertMultiple(insertStatement: String, @Param("records") records: List<GeneratedAlwaysRecord>): Int

    @Flush
    fun flush(): List<BatchResult>
}

fun GeneratedAlwaysMapper.insert(record: GeneratedAlwaysRecord): Int {
    return insert(this::insert, record, generatedAlways) {
        map(firstName) toProperty "firstName"
        map(lastName) toProperty "lastName"
    }
}

fun GeneratedAlwaysMapper.insertMultiple(vararg records: GeneratedAlwaysRecord): Int = insertMultiple(records.asList())

fun GeneratedAlwaysMapper.insertMultiple(records: Collection<GeneratedAlwaysRecord>): Int {
    return insertMultipleWithGeneratedKeys(this::insertMultiple, records, generatedAlways) {
        map(firstName) toProperty "firstName"
        map(lastName) toProperty "lastName"
    }
}

fun GeneratedAlwaysMapper.insertBatch(records: Collection<GeneratedAlwaysRecord>): List<Int> {
    return insertBatch(this::insert, records, generatedAlways) {
        map(firstName) toProperty "firstName"
        map(lastName) toProperty "lastName"
    }
}
