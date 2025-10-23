package examples.simple;

import org.mybatis.dynamic.sql.ParameterTypeConverter;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class PrimaryKeyColumn<T> extends SqlColumn<T> {
    private final boolean isPrimaryKeyColumn;

    private PrimaryKeyColumn(Builder<T> builder) {
        super(builder);
        isPrimaryKeyColumn = builder.isPrimaryKeyColumn;
    }

    public boolean isPrimaryKeyColumn() {
        return isPrimaryKeyColumn;
    }

    @Override
    public PrimaryKeyColumn<T> descending() {
        return copyBuilder().withDescendingPhrase(" DESC").build(); //$NON-NLS-1$
    }

    @Override
    public PrimaryKeyColumn<T> as(String alias) {
        return copyBuilder().withAlias(alias).build();
    }

    @Override
    public PrimaryKeyColumn<T> qualifiedWith(String tableQualifier) {
        return copyBuilder().withTableQualifier(tableQualifier).build();
    }

    @Override
    public PrimaryKeyColumn<T> asCamelCase() {
        return copyBuilder()
                .withAlias("\"" + StringUtilities.toCamelCase(name) + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public <S> PrimaryKeyColumn<S> withTypeHandler(String typeHandler) {
        return cast(copyBuilder().withTypeHandler(typeHandler).build());
    }

    @Override
    public <S> PrimaryKeyColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        return cast(copyBuilder().withRenderingStrategy(renderingStrategy).build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> PrimaryKeyColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        return cast(copyBuilder().withParameterTypeConverter((ParameterTypeConverter<T, ?>) parameterTypeConverter).build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> PrimaryKeyColumn<S> withJavaType(Class<S> javaType) {
        return cast(copyBuilder().withJavaType((Class<T>) javaType).build());
    }

    @Override
    public <S> PrimaryKeyColumn<S> withJavaProperty(String javaProperty) {
        return cast(copyBuilder().withJavaProperty(javaProperty).build());
    }

    private Builder<T> copyBuilder() {
        return populateBaseBuilder(new Builder<>()).isPrimaryKeyColumn(isPrimaryKeyColumn);
    }

    public static class Builder<T> extends AbstractBuilder<T, Builder<T>> {
        private boolean isPrimaryKeyColumn;

        public Builder<T> isPrimaryKeyColumn(boolean isPrimaryKeyColumn) {
            this.isPrimaryKeyColumn = isPrimaryKeyColumn;
            return this;
        }

        public PrimaryKeyColumn<T> build() {
            return new PrimaryKeyColumn<>(this);
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }
    }
}
