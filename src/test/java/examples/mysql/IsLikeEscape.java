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
package examples.mysql;

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.condition.IsLike;

@NullMarked
public class IsLikeEscape<T> extends IsLike<T> {
    private static final IsLikeEscape<?> EMPTY = new IsLikeEscape<Object>(-1, "") {
        @Override
        public Object value() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsLikeEscape<T> empty() {
        @SuppressWarnings("unchecked")
        IsLikeEscape<T> t = (IsLikeEscape<T>) EMPTY;
        return t;
    }

    private final String escapeString;

    protected IsLikeEscape(T value, String escapeString) {
        super(value);
        this.escapeString = escapeString;
    }

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        return super.renderCondition(renderingContext, leftColumn).mapFragment(this::addEscape);
    }

    private String addEscape(String s) {
        return s + " ESCAPE '" + escapeString + "'";
    }

    @Override
    public <R> IsLike<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, v -> new IsLikeEscape<>(v, escapeString), IsLikeEscape::empty);
    }

    public static <T> IsLikeEscape<T> isLike(T value, String escapeString) {
        return new IsLikeEscape<>(value, escapeString);
    }
}
