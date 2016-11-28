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
package examples.animal.data;

import org.mybatis.qbe.sql.where.WhereSupport;

public class AnimalDataSqlProvider {

    public String selectByExample(WhereSupport whereSupport) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("select a.id, a.animal_name as animalName, a.brain_weight as brainWeight, a.body_weight as bodyWeight");
        buffer.append(" from AnimalData a");
        if (whereSupport != null) {
            buffer.append(' ');
            buffer.append(whereSupport.getWhereClause());
        }
        buffer.append(" order by a.id");
        
        return buffer.toString();
    }

    public String deleteByExample(WhereSupport whereSupport) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("delete from AnimalData");
        if (whereSupport != null) {
            buffer.append(' ');
            buffer.append(whereSupport.getWhereClause());
        }
        
        return buffer.toString();
    }
}
