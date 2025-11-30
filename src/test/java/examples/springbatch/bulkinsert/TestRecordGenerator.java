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
package examples.springbatch.bulkinsert;

import examples.springbatch.common.PersonRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemReader;

public class TestRecordGenerator implements ItemReader<PersonRecord> {

    private int index = 0;

    private static final PersonRecord[] testRecords = {
            new PersonRecord(null, "Fred", "Flintstone"),
            new PersonRecord(null, "Wilma", "Flintstone"),
            new PersonRecord(null, "Pebbles", "Flintstone"),
            new PersonRecord(null, "Barney", "Rubble"),
            new PersonRecord(null, "Betty", "Rubble"),
            new PersonRecord(null, "Bamm Bamm", "Rubble")
    };

    @Override
    public @Nullable PersonRecord read() {
        if (index < testRecords.length) {
            return (testRecords[index++]);
        } else {
            return null;
        }
    }

    public static int recordCount() {
        return testRecords.length;
    }
}
