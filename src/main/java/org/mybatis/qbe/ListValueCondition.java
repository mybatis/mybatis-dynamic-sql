/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ListValueCondition<T> extends Condition<T> {
    void visitValues(Consumer<T> consumer);
    String render(Stream<Renderer> parameterRenderers);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
