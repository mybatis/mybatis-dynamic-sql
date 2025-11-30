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
package examples.springbatch.cursor;

import static examples.springbatch.mapper.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.count;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import examples.springbatch.mapper.PersonMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBatchTest
@SpringJUnitConfig(classes = CursorReaderBatchConfiguration.class)
class SpringBatchCursorTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void testThatRowsAreTransformedToUpperCase() throws Exception {
        // starting condition
        assertThat(upperCaseRowCount()).isZero();

        JobExecution execution = jobOperatorTestUtils.startJob();
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(numberOfRowsProcessed(execution)).isEqualTo(2);

        // ending condition
        assertThat(upperCaseRowCount()).isEqualTo(2);
    }

    private int numberOfRowsProcessed(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .map(StepExecution::getExecutionContext)
                .mapToInt(this::getRowCount)
                .sum();
    }

    private int getRowCount(ExecutionContext executionContext) {
        return executionContext.getInt("row_count", 0);
    }

    private long upperCaseRowCount() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

            SelectStatementProvider selectStatement = SelectDSL.select(count())
                    .from(person)
                    .where(lastName, isEqualTo("FLINTSTONE"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            return personMapper.count(selectStatement);
        }
    }
}
