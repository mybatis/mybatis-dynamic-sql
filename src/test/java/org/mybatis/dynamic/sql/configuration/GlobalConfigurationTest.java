/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class GlobalConfigurationTest {
    @Test
    void testAlternateConfigurationFileChangesDefaults() {
        System.setProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY, "defaultTrue.properties");
        GlobalConfiguration configuration = new GlobalConfiguration();
        System.clearProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY);

        assertThat(configuration.isIsNonRenderingWhereClauseAllowed()).isTrue();
    }

    @Test
    void testMissingPropertyUsesDefaults() {
        System.setProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY, "empty.properties");
        GlobalConfiguration configuration = new GlobalConfiguration();
        System.clearProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY);

        assertThat(configuration.isIsNonRenderingWhereClauseAllowed()).isFalse();
    }

    @Test
    void testMissingPropertyFileUsesDefaults() {
        System.setProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY, "apfbsglf.properties");
        GlobalConfiguration configuration = new GlobalConfiguration();
        System.clearProperty(GlobalConfiguration.CONFIGURATION_FILE_PROPERTY);

        assertThat(configuration.isIsNonRenderingWhereClauseAllowed()).isFalse();
    }

    @Test
    void testBadPropertyFile() throws IOException {
        GlobalConfiguration configuration = new GlobalConfiguration();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("empty.properties");
        assert inputStream != null;
        inputStream.close();

        TestLogHandler testLogHandler = new TestLogHandler();
        Logger logger = Logger.getLogger(GlobalConfiguration.class.getName());
        // we only want to use our handler for this test, so we don't pollute the test output
        logger.setUseParentHandlers(false);
        logger.addHandler(testLogHandler);

        configuration.loadProperties(inputStream, "empty.properties");
        assertThat(testLogHandler.records).hasSize(1);
        assertThat(testLogHandler.getRecords().get(0).getMessage())
                .isEqualTo("IOException reading property file \"empty.properties\"");
    }

    public static class TestLogHandler extends Handler {

        private final List<LogRecord> records = new ArrayList<>();

        public List<LogRecord> getRecords() {
            return records;
        }

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }
}
