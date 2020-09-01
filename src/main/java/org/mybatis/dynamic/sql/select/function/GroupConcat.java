package org.mybatis.dynamic.sql.select.function;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class GroupConcat extends AbstractUniTypeFunction<String, GroupConcat>{

    private GroupConcat(BindableColumn<String> column) {
        super(column);
    }

    @Override
    protected GroupConcat copy() {
        return new GroupConcat(column);
    }

    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return "group_concat("  //$NON-NLS-1$
                + column.renderWithTableAlias(tableAliasCalculator)
                +")";           //$NON-NLS-1$
    }

    public static GroupConcat of(BindableColumn<String> column) {
        return new GroupConcat(column);
    }

}
