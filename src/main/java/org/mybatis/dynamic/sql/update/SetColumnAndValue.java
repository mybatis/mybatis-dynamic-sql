/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.update;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class SetColumnAndValue<T> {
    private FragmentAndParameters fragmentAndParameters;
    
    private SetColumnAndValue(SqlColumn<T> column, T value, int uniqueId) {
        String mapKey = "up" + uniqueId; //$NON-NLS-1$
        String jdbcPlaceholder = column.getFormattedJdbcPlaceholder("parameters", mapKey); //$NON-NLS-1$
        String setPhrase = column.name() + " = " + jdbcPlaceholder; //$NON-NLS-1$
        
        fragmentAndParameters = new FragmentAndParameters.Builder(setPhrase) //$NON-NLS-1$)
                .withParameter(mapKey, value)
                .build();
    }
    
    private SetColumnAndValue(SqlColumn<T> column) {
        fragmentAndParameters = new FragmentAndParameters.Builder(column.name() + " = null") //$NON-NLS-1$)
                .build();
    }
    
    private SetColumnAndValue(SqlColumn<T> column, String constant) {
        fragmentAndParameters = new FragmentAndParameters.Builder(column.name() + " = " + constant) //$NON-NLS-1$)
                .build();
    }
    
    public FragmentAndParameters fragmentAndParameters() {
        return fragmentAndParameters;
    }
    
    public static <T> SetColumnAndValue<T> of(SqlColumn<T> column, T value, int uniqueId) {
        return new SetColumnAndValue<>(column, value, uniqueId);
    }

    public static <T> SetColumnAndValue<T> of(SqlColumn<T> column, String constant) {
        return new SetColumnAndValue<>(column, constant);
    }
    
    public static <T> SetColumnAndValue<T> of(SqlColumn<T> column) {
        return new SetColumnAndValue<>(column);
    }
}