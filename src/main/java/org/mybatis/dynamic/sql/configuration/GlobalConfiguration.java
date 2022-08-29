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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.util.Messages;

public class GlobalConfiguration {
    public static final String CONFIGURATION_FILE_PROPERTY = "mybatis-dynamic-sql.configurationFile"; //$NON-NLS-1$
    private static final String DEFAULT_PROPERTY_FILE = "mybatis-dynamic-sql.properties"; //$NON-NLS-1$
    private boolean isNonRenderingWhereClauseAllowed = false;
    private final Properties properties = new Properties();

    public GlobalConfiguration() {
        initialize();
    }

    private void initialize() {
        initializeProperties();
        initializeNonRenderingWhereClauseAllowed();
    }

    private void initializeProperties() {
        String configFileName = getConfigurationFileName();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileName);
        if (inputStream != null) {
            loadProperties(inputStream, configFileName);
        }
    }

    private String getConfigurationFileName() {
        String property = System.getProperty(CONFIGURATION_FILE_PROPERTY);
        if (property == null) {
            return DEFAULT_PROPERTY_FILE;
        } else {
            return property;
        }
    }

    void loadProperties(InputStream inputStream, String propertyFile) {
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new DynamicSqlException(Messages.getString("ERROR.3", propertyFile), e); //$NON-NLS-1$
        }
    }

    private void initializeNonRenderingWhereClauseAllowed() {
        String value = properties.getProperty("nonRenderingWhereClauseAllowed", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        isNonRenderingWhereClauseAllowed = Boolean.parseBoolean(value);
    }

    public boolean isIsNonRenderingWhereClauseAllowed() {
        return isNonRenderingWhereClauseAllowed;
    }
}
