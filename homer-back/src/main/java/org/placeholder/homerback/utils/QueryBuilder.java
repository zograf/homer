package org.placeholder.homerback.utils;

import java.time.LocalDateTime;

public class QueryBuilder {
    private String query;
    public QueryBuilder(String query){
        this.query = query;
    }

    public static QueryBuilder from(String bucket){
        return new QueryBuilder(String.format("from(bucket:\"%s\")", bucket));
    }
    public QueryBuilder range(String start) {
        this.query += String.format("|> range(start: %s)", start);
        return this;
    }
    public QueryBuilder range(LocalDateTime start, LocalDateTime end) {
        this.query += String.format("|> range(start: %s:00+01:00, stop: %s:00+01:00)", start.toString(), end.toString());
        return this;
    }
    public QueryBuilder filterByMeasurement(String measurement){
        this.query += String.format("|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" )", measurement);
        return this;
    }
    public QueryBuilder filterByTag(String tagName, String tagValue){
        this.query += String.format("|> filter(fn: (r) => r[\"%s\"] == \"%s\")", tagName, tagValue);
        return this;
    }
    public QueryBuilder count() {
        this.query += "|> count()";
        return this;
    }
    public QueryBuilder sortByTime(boolean descending) {
        this.query += String.format("|> sort(columns: [\"_time\"], desc: %s)", descending ? "true" : "false");
        return this;
    }
    public QueryBuilder page(Integer page, Integer pageSize) {
        this.query += String.format("|> limit(n: %d, offset: %d)", pageSize, page * pageSize);
        return this;
    }
    public QueryBuilder aggregateWindow(String interval, String function) {
        this.query += String.format("|> aggregateWindow(every: %s, fn: %s)", interval, function);
        return this;
    }
    public QueryBuilder group() {
        this.query += "|> group()";
        return this;
    }
    public QueryBuilder duplicate() {
        return new QueryBuilder(this.query);
    }

    public String build(){
        return this.query;
    }

}
