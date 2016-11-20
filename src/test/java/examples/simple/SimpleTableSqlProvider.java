package examples.simple;

import org.mybatis.qbe.sql.render.RenderedWhereClause;

public class SimpleTableSqlProvider {
    
    public String selectByExample(RenderedWhereClause renderedWhereClause) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.id, a.first_name as firstName, a.last_name as lastName, a.birth_date as birthDate, a.occupation ");
        sb.append("from SimpleTable a ");
        sb.append(renderedWhereClause.getWhereClause());
        
        return sb.toString();
    }

    public String deleteByExample(RenderedWhereClause renderedWhereClause) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("delete from SimpleTable ");
        sb.append(renderedWhereClause.getWhereClause());
        
        return sb.toString();
    }
}
