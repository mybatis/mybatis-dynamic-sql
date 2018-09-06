package org.mybatis.dynamic.sql.insert.render;

import java.util.HashMap;
import java.util.List;

public class MultiInsertStatementProvider extends HashMap<String, Object> {
    
    private static final long serialVersionUID = -6122669380843485470L;
    private static final String STATEMENT_KEY = "insertStatement"; //$NON-NLS-1$
    private static final String LIST_KEY = "list"; //$NON-NLS-1$

    public void setInsertStatement(String insertStatement) {
        super.put(STATEMENT_KEY, insertStatement);
    }
    
    public String getInsertStatement() {
        return (String) super.get(STATEMENT_KEY);
    }
    
    public void setList(List<?> records) {
        put(LIST_KEY, records);
    }
    
    public List<?> getList() {
        return (List<?>) get(LIST_KEY);
    }
}
