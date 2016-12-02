/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.update;

import java.util.Optional;

import org.mybatis.qbe.sql.SqlField;

/**
 * A field value pair used to render update statements.
 * 
 * In an update statement it is used to render the set clause.  For example: 
 *   set foo = ?
 * 
 * @author Jeff Butler
 *
 * @param <T>
 */
public class FieldAndValue<T> {
    private T value;
    private SqlField<T> field;
    
    private FieldAndValue() {
        super();
    }
    
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public SqlField<T> getField() {
        return field;
    }
    
    public static <S> FieldAndValue<S> of(SqlField<S> field, S value) {
        FieldAndValue<S> phrase = new FieldAndValue<>();
        phrase.value = value;
        phrase.field = field;
        return phrase;
    }

    public static <S> FieldAndValue<S> of(SqlField<S> field) {
        FieldAndValue<S> phrase = new FieldAndValue<>();
        phrase.field = field;
        return phrase;
    }
}
