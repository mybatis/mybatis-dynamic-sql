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

/**
 * Visitor for all column mappings. Various column mappings are used by insert and update
 * statements. Only the null and constant mappings are supported by all statements. Other mappings
 * may or may not be supported. For example, it makes no sense to map a column to another column in
 * an insert - so the ColumnToColumnMapping is only supported on update statements.
 *
 * <p>Rather than implement this interface directly, we recommend implementing one of the derived
 * classes. The derived classes encapsulate the rules about which mappings are applicable to the
 * different types of statements.
 *
 * @author Jeff Butler
 *
 * @param <R>
 *            The type of object created by the visitor
 */
public interface ColumnMappingVisitor<R> {
    R visit(NullMapping mapping);

    R visit(ConstantMapping mapping);

    R visit(StringConstantMapping mapping);

    <T> R visit(ValueMapping<T> mapping);

    <T> R visit(ValueOrNullMapping<T> mapping);

    <T> R visit(ValueWhenPresentMapping<T> mapping);

    R visit(SelectMapping mapping);

    R visit(PropertyMapping mapping);

    R visit(PropertyWhenPresentMapping mapping);

    R visit(ColumnToColumnMapping columnMapping);
}
