/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.util;

import java.util.StringJoiner;
import java.util.stream.Collector;

public interface CustomCollectors {

    static Collector<CharSequence, StringJoiner, String> joining(CharSequence delimiter, CharSequence prefix,
            CharSequence suffix) {
        return Collector.of(() -> {
            StringJoiner sj = new StringJoiner(delimiter, prefix, suffix);
            sj.setEmptyValue(""); //$NON-NLS-1$
            return sj;
        }, StringJoiner::add, StringJoiner::merge, StringJoiner::toString);
    }
}
