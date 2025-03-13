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
import java.util.function.Predicate;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

@NullMarked
public class IsLikeEscape<T> extends AbstractSingleValueCondition<T> {
    private static final IsLikeEscape<?> EMPTY = new IsLikeEscape<Object>(-1, null) {
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

    private final @Nullable Character escapeCharacter;

    protected IsLikeEscape(T value, @Nullable Character escapeCharacter) {
        super(value);
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public String operator() {
        return "like";
    }

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        var fragment = super.renderCondition(renderingContext, leftColumn);
        if (escapeCharacter != null) {
            fragment = fragment.mapFragment(this::addEscape);
        }

        return fragment;
    }

    private String addEscape(String s) {
        return s + " ESCAPE '" + escapeCharacter + "'";
    }

    @Override
    public IsLikeEscape<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsLikeEscape::empty, this);
    }

    @Override
    public <R> IsLikeEscape<R> map(Function<? super T, ? extends R> mapper) {
        return mapSupport(mapper, v -> new IsLikeEscape<>(v, escapeCharacter), IsLikeEscape::empty);
    }

    public static <T> IsLikeEscape<T> isLike(T value) {
        return new IsLikeEscape<>(value, null);
    }

    public static <T> IsLikeEscape<T> isLike(T value, Character escapeCharacter) {
        return new IsLikeEscape<>(value, escapeCharacter);
    }
}
