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
package org.mybatis.dynamic.sql.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.util.Messages;

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

        assertThatExceptionOfType(DynamicSqlException.class)
                .isThrownBy(() -> configuration.loadProperties(inputStream, "empty.properties"))
                .withMessage(Messages.getString("ERROR.3", "empty.properties"))
                .withCauseInstanceOf(IOException.class);
    }
}
