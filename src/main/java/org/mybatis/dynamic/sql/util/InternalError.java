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
package org.mybatis.dynamic.sql.util;

/**
 * Enum for managing internal error numbers.
 */
public enum InternalError {
    INTERNAL_ERROR_1(1),
    INTERNAL_ERROR_2(2),
    INTERNAL_ERROR_3(3),
    INTERNAL_ERROR_4(4),
    INTERNAL_ERROR_5(5),
    INTERNAL_ERROR_6(6),
    INTERNAL_ERROR_7(7),
    INTERNAL_ERROR_8(8),
    INTERNAL_ERROR_9(9),
    INTERNAL_ERROR_10(10),
    INTERNAL_ERROR_11(11),
    INTERNAL_ERROR_12(12),
    INTERNAL_ERROR_13(13),
    INTERNAL_ERROR_14(14),
    INTERNAL_ERROR_15(15);

    private final int number;

    InternalError(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
