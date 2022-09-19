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
package org.mybatis.dynamic.sql.util;

public abstract class GeneralInsertMappingVisitor<R> implements ColumnMappingVisitor<R> {
    @Override
    public final R visit(SelectMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(1));
    }

    @Override
    public final R visit(PropertyMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(2));
    }

    @Override
    public final R visit(PropertyWhenPresentMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(3));
    }

    @Override
    public final R visit(ColumnToColumnMapping columnMapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(4));
    }
}
