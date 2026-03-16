package site.addzero.biz.spec.iot.tdengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL string plus ordered parameter list.
 */
public final class SqlStatement {

    private final String sql;
    private final List<Object> parameters;

    public SqlStatement(String sql, List<Object> parameters) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("sql must not be blank");
        }
        this.sql = sql;
        this.parameters = Collections.unmodifiableList(new ArrayList<Object>(parameters));
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
