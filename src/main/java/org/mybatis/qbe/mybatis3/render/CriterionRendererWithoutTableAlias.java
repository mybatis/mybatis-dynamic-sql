package org.mybatis.qbe.mybatis3.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;

public class CriterionRendererWithoutTableAlias<T> extends AbstractCriterionRenderer<T>{

    private CriterionRendererWithoutTableAlias(Criterion<T> criterion, AtomicInteger sequence) {
        super(criterion, sequence);
    }

    @Override
    protected <S> AbstractCriterionRenderer<S> newRenderer(Criterion<S> criterion, AtomicInteger sequence) {
        return CriterionRendererWithoutTableAlias.of(criterion, sequence);
    }

    @Override
    protected String fieldName(Criterion<?> criterion) {
        return criterion.fieldNameWithoutAlias();
    }

    public static <T> CriterionRendererWithoutTableAlias<T> of(Criterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRendererWithoutTableAlias<>(criterion, sequence);
    }
}
