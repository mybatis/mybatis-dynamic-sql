/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql;

import org.mybatis.dynamic.sql.render.RenderingContext;

/**
 * Deprecated interface.
 *
 * <p>Conditions are no longer rendered with a visitor, so the name is misleading. This change makes it far easier
 * to implement custom conditions for functionality not supplied out of the box by the library.
 *
 * <p>If you created any direct implementations of this interface, you will need to change the rendering functions.
 * The library now calls {@link RenderableCondition#renderCondition(RenderingContext, BindableColumn)} and
 * {@link RenderableCondition#renderLeftColumn(RenderingContext, BindableColumn)} instead of the previous methods
 * like <code>operator</code>, <code>value</code>, etc. Subclasses of the supplied abstract conditions should continue
 * to function as before.
 *
 * @param <T> the Java type related to the column this condition relates to. Used primarily for compiler type checking
 * @deprecated since 2.0.0. Please use {@link RenderableCondition} instead.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public interface VisitableCondition<T> extends RenderableCondition<T> { }
