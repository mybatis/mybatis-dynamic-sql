/**
 *    Copyright 2016-2018 the original author or authors.
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
package examples.column.comparison;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ColumnComparisonDynamicSqlSupport {
    public static final ColumnComparison columnComparison = new ColumnComparison();
    public static final SqlColumn<Integer> number1 = columnComparison.number1; 
    public static final SqlColumn<Integer> number2 = columnComparison.number2; 
    
    public static final class ColumnComparison extends SqlTable {
        public final SqlColumn<Integer> number1 = column("number1", JDBCType.INTEGER); 
        public final SqlColumn<Integer> number2 = column("number2", JDBCType.INTEGER); 

        public ColumnComparison() {
            super("ColumnComparison");
        }
    }
}
