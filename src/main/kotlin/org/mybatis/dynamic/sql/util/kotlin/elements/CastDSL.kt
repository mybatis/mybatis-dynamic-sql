/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.select.function.Cast
import org.mybatis.dynamic.sql.util.kotlin.assertNull

class CastDSL {
    internal var cast: Cast? = null
        private set(value) {
            assertNull(field, "ERROR.43") //$NON-NLS-1$
            field = value
        }

    infix fun String.`as`(targetType: String) {
        cast = SqlBuilder.cast(this).`as`(targetType)
    }

    infix fun BasicColumn.`as`(targetType: String) {
        cast = SqlBuilder.cast(this).`as`(targetType)
    }
}
