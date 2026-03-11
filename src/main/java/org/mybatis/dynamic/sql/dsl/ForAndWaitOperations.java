/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

public interface ForAndWaitOperations<T> {
    default T forUpdate() {
        return setForClause("for update"); //$NON-NLS-1$
    }

    default T forNoKeyUpdate() {
        return setForClause("for no key update"); //$NON-NLS-1$
    }

    default T forShare() {
        return setForClause("for share"); //$NON-NLS-1$
    }

    default T forKeyShare() {
        return setForClause("for key share"); //$NON-NLS-1$
    }

    default T skipLocked() {
        return setWaitClause("skip locked"); //$NON-NLS-1$
    }

    default T nowait() {
        return setWaitClause("nowait"); //$NON-NLS-1$
    }

    T setWaitClause(String waitClause);

    T setForClause(String forClause);
}
