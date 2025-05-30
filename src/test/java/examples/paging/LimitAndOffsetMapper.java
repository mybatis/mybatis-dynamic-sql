/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.paging;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;

import java.util.List;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import examples.animal.data.AnimalData;

public interface LimitAndOffsetMapper {

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Arg(column = "id", javaType = int.class, id = true)
    @Arg(column = "animal_name", javaType = String.class)
    @Arg(column = "brain_weight", javaType = double.class)
    @Arg(column = "body_weight", javaType = double.class)
    List<AnimalData> selectMany(SelectStatementProvider selectStatement);

    default QueryExpressionDSL<LimitAndOffsetAdapter<List<AnimalData>>> selectWithLimitAndOffset(int limit, int offset) {
        return SelectDSL.select(selectModel -> LimitAndOffsetAdapter.of(selectModel, this::selectMany, limit, offset),
                id, animalName, brainWeight, bodyWeight)
                .from(animalData);
    }
}
