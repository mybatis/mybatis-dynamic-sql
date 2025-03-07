package examples.kotlin.mybatis3.mariadb

import org.mybatis.dynamic.sql.AbstractSingleValueCondition
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.function.Predicate

open class KIsLikeEscape<T : Any>(value: T, private val escapeCharacter: Char? = null) : AbstractSingleValueCondition<T>(value) {

    override fun operator(): String = "like"

    override fun renderCondition(
        renderingContext: RenderingContext,
        leftColumn: BindableColumn<T>
    ): FragmentAndParameters {
        val f = super.renderCondition(renderingContext, leftColumn)

        return escapeCharacter?.let { f.mapFragment{ "$it ESCAPE '$escapeCharacter'"} } ?: f
    }

    override fun filter(predicate: Predicate<in T>): KIsLikeEscape<T> {
        return filterSupport(predicate, ::empty, this)
    }

    fun <R : Any> map(mapper : (T) -> R): KIsLikeEscape<R> {
        return mapSupport(mapper, { r -> KIsLikeEscape(r, escapeCharacter) }, ::empty)
    }

    private class EmptyCondition : KIsLikeEscape<Any>(-1) {
        override fun isEmpty(): Boolean = true

        override fun value(): Any {
            throw NoSuchElementException("No value present") //$NON-NLS-1$
        }
    }

    companion object {
        private val EMPTY: KIsLikeEscape<Any> = EmptyCondition()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): KIsLikeEscape<T> = EMPTY as KIsLikeEscape<T>
    }
}
