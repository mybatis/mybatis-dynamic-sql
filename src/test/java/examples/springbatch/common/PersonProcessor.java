/**
 *    Copyright 2016-2019 the original author or authors.
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
package examples.springbatch.common;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PersonProcessor implements ItemProcessor<Person, Person> {
    
    private ExecutionContext executionContext;

    @Override
    public Person process(Person person) throws Exception {
        incrementRowCount();
        
        Person transformed = new Person();
        transformed.setId(person.getId());
        transformed.setFirstName(person.getFirstName().toUpperCase());
        transformed.setLastName(person.getLastName().toUpperCase());
        return transformed;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        executionContext = stepExecution.getExecutionContext();
    }
    
    @BeforeChunk
    public void beforeChunk(ChunkContext chunkContext) {
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
