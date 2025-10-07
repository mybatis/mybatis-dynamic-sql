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
package examples.kotlin.mybatis3.mariadb

import java.util.function.Predicate
import java.util.function.Function
import org.mybatis.dynamic.sql.AbstractSingleValueCondition
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

sealed class KIsLikeEscape<T : Any>(
    value: T,
    private val escapeCharacter: Char? = null
) : AbstractSingleValueCondition<T>(value), AbstractSingleValueCondition.Filterable<T>,
    AbstractSingleValueCondition.Mappable<T> {

    override fun operator(): String = "like"

    override fun renderCondition(
        renderingContext: RenderingContext,
        leftColumn: BindableColumn<T>
    ): FragmentAndParameters = with(super.renderCondition(renderingContext, leftColumn)) {
        escapeCharacter?.let { mapFragment { "$it ESCAPE '$escapeCharacter'" } } ?: this
    }

    override fun filter(predicate: Predicate<in T>): KIsLikeEscape<T> =
        filterSupport(predicate, EmptyIsLikeEscape::empty, this)

    override fun <R : Any> map(mapper : Function<in T, out R>): KIsLikeEscape<R> =
        mapSupport(mapper, { r -> ConcreteIsLikeEscape(r, escapeCharacter) }, EmptyIsLikeEscape::empty)

    companion object {
        fun <T: Any> isLike(value: T, escapeCharacter: Char? = null) : KIsLikeEscape<T> =
            ConcreteIsLikeEscape(value, escapeCharacter)
    }
}

private class ConcreteIsLikeEscape<T: Any>(
    value: T,
    escapeCharacter: Char? = null
) : KIsLikeEscape<T>(value, escapeCharacter)

private class EmptyIsLikeEscape : KIsLikeEscape<Any>(-1) {
    override fun isEmpty(): Boolean = true

    override fun value(): Any {
        throw NoSuchElementException("No value present")
    }

    companion object {
        private val EMPTY: KIsLikeEscape<Any> = EmptyIsLikeEscape()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): KIsLikeEscape<T> = EMPTY as KIsLikeEscape<T>
    }
}
