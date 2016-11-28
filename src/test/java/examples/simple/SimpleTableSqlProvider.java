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
package examples.simple;

import org.mybatis.qbe.sql.where.WhereSupport;

public class SimpleTableSqlProvider {
    
    public String selectByExample(WhereSupport whereSupport) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.id, a.first_name as firstName, a.last_name as lastName, a.birth_date as birthDate, a.occupation ");
        sb.append("from SimpleTable a ");
        sb.append(whereSupport.getWhereClause());
        
        return sb.toString();
    }

    public String deleteByExample(WhereSupport whereSupport) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("delete from SimpleTable ");
        sb.append(whereSupport.getWhereClause());
        
        return sb.toString();
    }
}
