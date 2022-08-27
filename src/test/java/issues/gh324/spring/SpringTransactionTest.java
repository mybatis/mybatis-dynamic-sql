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
package issues.gh324.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import issues.gh324.ObservableCache;
import issues.gh324.TestUtils;

@SpringJUnitConfig(classes = TestConfiguration.class)
class SpringTransactionTest {

    @Autowired
    private SpringNameService nameService;

    @Test
    void testCacheWithCommit() {
        nameService.resetDatabase();

        nameService.insertRecord();
        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsFred);
        assertThat(ObservableCache.getInstance().getHits()).isZero();

        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsFred);
        assertThat(ObservableCache.getInstance().getHits()).isEqualTo(1);

        nameService.updateRecordAndCommit();
        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsBarney);
        assertThat(ObservableCache.getInstance().getHits()).isZero();

        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsBarney);
        assertThat(ObservableCache.getInstance().getHits()).isEqualTo(1);
    }

    @Test
    void testCacheWithRollback() {
        nameService.resetDatabase();

        nameService.insertRecord();
        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsFred);
        assertThat(ObservableCache.getInstance().getHits()).isZero();

        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsFred);
        assertThat(ObservableCache.getInstance().getHits()).isEqualTo(1);

        nameService.updateRecordAndRollback();
        assertThat(nameService.getRecord()).hasValueSatisfying(TestUtils::recordIsFred);
        // should pull the result from cache as the transaction was rolled back
        assertThat(ObservableCache.getInstance().getHits()).isEqualTo(2);
    }
}
