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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class VariousPagingAndLimitScenariosTest {

    @Test
    void testOptionalLimitOnDelete() {
        var deleteStatement = deleteFrom(animalData)
                .limitWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("delete from AnimalData");
    }

    @Test
    void testOptionalLimitOnDeleteWithWhere() {
        var deleteStatement = deleteFrom(animalData)
                .where(id, isLessThan(22))
                .limitWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement())
                .isEqualTo("delete from AnimalData where id < :p1");
    }

    @Test
    void testOptionalLimitOnUpdate() {
        var updateStatement = update(animalData)
                .set(id).equalTo(1)
                .limitWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update AnimalData set id = :p1");
    }

    @Test
    void testOptionalLimitOnUpdateWithWhere() {
        var updateStatement = update(animalData)
                .set(id).equalTo(1)
                .where(id, isLessThan(22))
                .limitWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update AnimalData set id = :p1 where id < :p2");
   }

    @Test
    void testOptionalLimitOnSelect() {
        var selectStatement = select(animalData.allColumns())
                .from(animalData)
                .limitWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData");
    }

    @Test
    void testOptionalOffsetOnSelect() {
        var selectStatement = select(animalData.allColumns())
                .from(animalData)
                .offsetWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData");
    }

    @Test
    void testOptionalFetchFirstOnSelect() {
        var selectStatement = select(animalData.allColumns())
                .from(animalData)
                .fetchFirstWhenPresent(null).rowsOnly()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData");
    }

    @Test
    void testOptionalLimitAndOffsetOnSelect() {
        var selectStatement = select(animalData.allColumns())
                .from(animalData)
                .limitWhenPresent(null)
                .offsetWhenPresent(null)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData");
    }

    @Test
    void testOptionalOffsetAndFetchOnSelect() {
        var selectStatement = select(animalData.allColumns())
                .from(animalData)
                .offsetWhenPresent(null)
                .fetchFirstWhenPresent(null).rowsOnly()
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData");
    }
}
