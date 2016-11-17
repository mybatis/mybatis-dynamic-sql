package org.mybatis.qbe.mybatis3.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.qbe.Criterion;

public class CriterionRenderer<T> extends AbstractCriterionRenderer<T> {
    
    private <S> CriterionRenderer(Criterion<T> criterion, AtomicInteger sequence) {
        super(criterion, sequence);
    }
    
    @Override
    protected <S> AbstractCriterionRenderer<S> newRenderer(Criterion<S> criterion, AtomicInteger sequence) {
        return CriterionRenderer.of(criterion, sequence);
    }

    @Override
    protected String fieldName(Criterion<T> criterion) {
        return criterion.fieldName();
    }

    public static <T> CriterionRenderer<T> of(Criterion<T> criterion, AtomicInteger sequence) {
        return new CriterionRenderer<>(criterion, sequence);
    }
}
