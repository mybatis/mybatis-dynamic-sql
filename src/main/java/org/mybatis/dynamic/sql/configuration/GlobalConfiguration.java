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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// TODO - read initial value from a properties file!
public class GlobalConfiguration {
    private static boolean unrenderableWhereClauseAllowed = false;

    static {
        String propertyFile = "mybatis-dynamic-sql.properties";
        try (InputStream is = GlobalConfiguration.class.getResourceAsStream(propertyFile)) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                String value = p.getProperty("unrenderableWhereClauseAllowed");
                if (value != null) {
                    unrenderableWhereClauseAllowed = Boolean.parseBoolean(value);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    public static boolean getUnrenderableWhereClauseAllowed() {
        return unrenderableWhereClauseAllowed;
    }
}
