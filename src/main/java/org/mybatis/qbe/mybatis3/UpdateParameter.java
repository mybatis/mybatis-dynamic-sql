package org.mybatis.qbe.mybatis3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.qbe.sql.set.render.RenderedSetClause;
import org.mybatis.qbe.sql.where.render.RenderedWhereClause;

/**
 * This class combines a "set" clause and a "where" clause into one parameter object
 * that can be sent to a MyBatis3 mapper method.
 * 
 * @author Jeff Butler
 *
 */
public class UpdateParameter {
    private String setClause;
    private String whereClause;
    private Map<String, Object> parameters;

    private UpdateParameter (String setClause, String whereClause, Map<String, Object> parameters) {
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

    public static UpdateParameter of(RenderedSetClause setClause, RenderedWhereClause whereClause) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(setClause.getParameters());
        parameters.putAll(whereClause.getParameters());
        return new UpdateParameter(setClause.getSetClause(), whereClause.getWhereClause(), parameters);
    }
}
