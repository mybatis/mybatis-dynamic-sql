/*
 *    Copyright 2016-2020 the original author or authors.
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
package examples.animal.data;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.select.aggregate.AbstractAggregate;

public class DeprecatedAverage extends AbstractAggregate<DeprecatedAverage> {
    private DeprecatedAverage(BasicColumn column) {
        super(column);
    }

    @Override
    protected String render(String columnName) {
        return "avg(" + columnName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected DeprecatedAverage copy() {
        return new DeprecatedAverage(column);
    }

    public static DeprecatedAverage of(BasicColumn column) {
        return new DeprecatedAverage(column);
    }
}
