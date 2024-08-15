package org.placeholder.homerback.internal_dtos;

import org.placeholder.homerback.utils.QueryBuilder;

public class QueryWithInterval {
    private QueryBuilder query;
    private String interval;

    public QueryWithInterval(QueryBuilder query, String interval) {
        this.query = query;
        this.interval = interval;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public void setQuery(QueryBuilder query) {
        this.query = query;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
