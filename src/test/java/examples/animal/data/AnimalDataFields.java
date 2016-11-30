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

import java.sql.JDBCType;

import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.SqlTable;

public interface AnimalDataFields {
    SqlTable animalData = SqlTable.of("AnimalData").withAlias("a");
    MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER).inTable(animalData); 
    MyBatis3Field<String> animalName = MyBatis3Field.of("animal_name", JDBCType.VARCHAR).inTable(animalData);
    MyBatis3Field<Double> bodyWeight = MyBatis3Field.of("body_weight", JDBCType.DOUBLE).inTable(animalData);
    MyBatis3Field<Double> brainWeight = MyBatis3Field.of("brain_weight", JDBCType.DOUBLE).inTable(animalData);
}
