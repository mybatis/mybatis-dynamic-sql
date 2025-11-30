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
package examples.springbatch.common;

import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PersonProcessor implements ItemProcessor<PersonRecord, PersonRecord> {

    private ExecutionContext executionContext;

    @Override
    public PersonRecord process(PersonRecord person) {
        incrementRowCount();

        return new PersonRecord(person.id(), person.firstName().toUpperCase(), person.lastName().toUpperCase());
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        executionContext = stepExecution.getExecutionContext();
    }

    @BeforeChunk
    public void beforeChunk(Chunk<PersonRecord> chunk) {
        incrementChunkCount();
    }

    private void incrementRowCount() {
        executionContext.putInt("row_count",
                executionContext.getInt("row_count", 0) + 1);
    }

    private void incrementChunkCount() {
        executionContext.putInt("chunk_count",
                executionContext.getInt("chunk_count", 0) + 1);
    }
}
