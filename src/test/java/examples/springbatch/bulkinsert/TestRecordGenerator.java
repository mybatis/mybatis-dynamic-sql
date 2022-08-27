/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.springframework.batch.item.ItemReader;

import examples.springbatch.common.PersonRecord;

public class TestRecordGenerator implements ItemReader<PersonRecord> {

    private int index = 0;

    private static PersonRecord[] testRecords = {
            new PersonRecord("Fred", "Flintstone"),
            new PersonRecord("Wilma", "Flintstone"),
            new PersonRecord("Pebbles", "Flintstone"),
            new PersonRecord("Barney", "Rubble"),
            new PersonRecord("Betty", "Rubble"),
            new PersonRecord("Bamm Bamm", "Rubble")
    };

    @Override
    public PersonRecord read() {
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
