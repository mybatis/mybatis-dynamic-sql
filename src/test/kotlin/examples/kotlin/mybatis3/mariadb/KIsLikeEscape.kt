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
        internal fun <T : Any> empty(): KIsLikeEscape<T> = EMPTY as KIsLikeEscape<T>
    }
}
