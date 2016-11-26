package org.mybatis.qbe.sql.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class UpdateSupport {
    private String setClause;
    private String whereClause;
    private Map<String, Object> parameters;

    private UpdateSupport (String setClause, String whereClause, Map<String, Object> parameters) {
        this.setClause = setClause;
        this.whereClause = whereClause;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    public String getSetClause() {
        return setClause;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static UpdateSupport of(String setClause, String whereClause, Map<String, Object> parameters) {
        return new UpdateSupport(setClause, whereClause, parameters);
    }
}
