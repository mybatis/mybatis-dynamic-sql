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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import issues.gh324.NameRecord;
import issues.gh324.NameTableMapper;

@Service
public class SpringNameService {
    @Autowired
    private NameTableMapper mapper;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertRecord() {
        NameRecord record = new NameRecord();
        record.setId(1);
        record.setName("Fred");
        mapper.insert(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRecordAndCommit() {
        NameRecord record = new NameRecord();
        record.setId(1);
        record.setName("Barney");
        mapper.updateByPrimaryKey(record);
    }

    public void updateRecordAndRollback() {
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        NameRecord record = new NameRecord();
        record.setId(1);
        record.setName("Barney");
        mapper.updateByPrimaryKey(record);
        transactionManager.rollback(txStatus);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<NameRecord> getRecord() {
        return mapper.selectByPrimaryKey(1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetDatabase() {
        mapper.deleteAll();
    }
}
