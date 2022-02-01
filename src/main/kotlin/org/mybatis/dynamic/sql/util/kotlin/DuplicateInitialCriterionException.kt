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
package org.mybatis.dynamic.sql.util.kotlin

/**
 * This exception is thrown when a where clause contains more than one criterion and there
 * is not an "and" or an "or" to connect them.
 *
 * @since 1.4.0
 */
class DuplicateInitialCriterionException : RuntimeException(
    "Setting more than one initial criterion is not allowed. " +
            "Additional criteria should be added with \"and\" or \"or\" expression")
