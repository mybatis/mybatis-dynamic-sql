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
package org.mybatis.dynamic.sql.common;

import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class OrderByRenderer {
    public FragmentAndParameters render(OrderByModel orderByModel) {
        String phrase = orderByModel.mapColumns(this::calculateOrderByPhrase)
                .collect(Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return FragmentAndParameters.withFragment(phrase).build();
    }

    private String calculateOrderByPhrase(SortSpecification column) {
        String phrase = column.orderByName();
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
}
