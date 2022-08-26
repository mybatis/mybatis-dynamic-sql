/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping

@MyBatisDslMarker
sealed class AbstractInsertColumnMapCompleter<T>(
    internal val column: SqlColumn<T>,
    internal val mappingConsumer: (AbstractColumnMapping) -> Unit) {

    fun toNull() = mappingConsumer.invoke(NullMapping.of(column))

    infix fun toConstant(constant: String) = mappingConsumer.invoke(ConstantMapping.of(column, constant))

    infix fun toStringConstant(constant: String) = mappingConsumer.invoke(StringConstantMapping.of(column, constant))
}

class MultiRowInsertColumnMapCompleter<T>(
    column: SqlColumn<T>,
    mappingConsumer: (AbstractColumnMapping) -> Unit)
    : AbstractInsertColumnMapCompleter<T>(column, mappingConsumer) {

    infix fun toProperty(property: String) = mappingConsumer.invoke(PropertyMapping.of(column, property))
}

class SingleRowInsertColumnMapCompleter<T>(
    column: SqlColumn<T>,
    mappingConsumer: (AbstractColumnMapping) -> Unit)
    : AbstractInsertColumnMapCompleter<T>(column, mappingConsumer) {

    infix fun toProperty(property: String) = mappingConsumer.invoke(PropertyMapping.of(column, property))

    fun toPropertyWhenPresent(property: String, valueSupplier: () -> T?) =
        mappingConsumer.invoke(PropertyWhenPresentMapping.of(column, property, valueSupplier))
}

class GeneralInsertColumnSetCompleter<T>(
    column: SqlColumn<T>,
    mappingConsumer: (AbstractColumnMapping) -> Unit)
    : AbstractInsertColumnMapCompleter<T>(column, mappingConsumer) {

    infix fun toValue(value: T & Any) = toValue { value }

    infix fun toValue(value: () -> T & Any) = mappingConsumer.invoke(ValueMapping.of(column, value))

    infix fun toValueOrNull(value: T?) = toValueOrNull { value }

    infix fun toValueOrNull(value: () -> T?) = mappingConsumer.invoke(ValueOrNullMapping.of(column, value))

    infix fun toValueWhenPresent(value: T?) = toValueWhenPresent { value }

    infix fun toValueWhenPresent(value: () -> T?) = mappingConsumer.invoke(ValueWhenPresentMapping.of(column, value))
}
