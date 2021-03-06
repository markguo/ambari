/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.applicationhistoryservice.metrics.timeline;

import com.sun.xml.bind.v2.util.QNameMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulate all metrics related SQL queries.
 */
public class PhoenixTransactSQL {

  static final Log LOG = LogFactory.getLog(PhoenixTransactSQL.class);
  /**
   * Create table to store individual metric records.
   */
  public static final String CREATE_METRICS_TABLE_SQL = "CREATE TABLE IF NOT " +
    "EXISTS METRIC_RECORD (METRIC_NAME VARCHAR, " +
    "HOSTNAME VARCHAR, " +
    "SERVER_TIME UNSIGNED_LONG NOT NULL, " +
    "APP_ID VARCHAR, " +
    "INSTANCE_ID VARCHAR, " +
    "START_TIME UNSIGNED_LONG, " +
    "UNITS CHAR(20), " +
    "METRIC_SUM DOUBLE, " +
    "METRIC_COUNT UNSIGNED_INT, " +
    "METRIC_MAX DOUBLE, " +
    "METRIC_MIN DOUBLE, " +
    "METRICS VARCHAR CONSTRAINT pk " +
    "PRIMARY KEY (METRIC_NAME, HOSTNAME, SERVER_TIME, APP_ID, " +
    "INSTANCE_ID)) DATA_BLOCK_ENCODING='%s', IMMUTABLE_ROWS=true, " +
    "TTL=%s, COMPRESSION='%s'";

  public static final String CREATE_METRICS_AGGREGATE_HOURLY_TABLE_SQL =
    "CREATE TABLE IF NOT EXISTS METRIC_RECORD_HOURLY " +
      "(METRIC_NAME VARCHAR, " +
      "HOSTNAME VARCHAR, " +
      "APP_ID VARCHAR, " +
      "INSTANCE_ID VARCHAR, " +
      "SERVER_TIME UNSIGNED_LONG NOT NULL, " +
      "UNITS CHAR(20), " +
      "METRIC_SUM DOUBLE," +
      "METRIC_COUNT UNSIGNED_INT, " +
      "METRIC_MAX DOUBLE," +
      "METRIC_MIN DOUBLE CONSTRAINT pk " +
      "PRIMARY KEY (METRIC_NAME, HOSTNAME, APP_ID, INSTANCE_ID, " +
      "SERVER_TIME)) DATA_BLOCK_ENCODING='%s', IMMUTABLE_ROWS=true, " +
      "TTL=%s, COMPRESSION='%s'";

  public static final String CREATE_METRICS_AGGREGATE_MINUTE_TABLE_SQL =
    "CREATE TABLE IF NOT EXISTS METRIC_RECORD_MINUTE " +
      "(METRIC_NAME VARCHAR, " +
      "HOSTNAME VARCHAR, " +
      "APP_ID VARCHAR, " +
      "INSTANCE_ID VARCHAR, " +
      "SERVER_TIME UNSIGNED_LONG NOT NULL, " +
      "UNITS CHAR(20), " +
      "METRIC_SUM DOUBLE," +
      "METRIC_COUNT UNSIGNED_INT, " +
      "METRIC_MAX DOUBLE," +
      "METRIC_MIN DOUBLE CONSTRAINT pk " +
      "PRIMARY KEY (METRIC_NAME, HOSTNAME, APP_ID, INSTANCE_ID, " +
      "SERVER_TIME)) DATA_BLOCK_ENCODING='%s', IMMUTABLE_ROWS=true, TTL=%s," +
      " COMPRESSION='%s'";

  public static final String CREATE_METRICS_CLUSTER_AGGREGATE_TABLE_SQL =
    "CREATE TABLE IF NOT EXISTS METRIC_AGGREGATE " +
      "(METRIC_NAME VARCHAR, " +
      "APP_ID VARCHAR, " +
      "INSTANCE_ID VARCHAR, " +
      "SERVER_TIME UNSIGNED_LONG NOT NULL, " +
      "UNITS CHAR(20), " +
      "METRIC_SUM DOUBLE, " +
      "HOSTS_COUNT UNSIGNED_INT, " +
      "METRIC_MAX DOUBLE, " +
      "METRIC_MIN DOUBLE " +
      "CONSTRAINT pk PRIMARY KEY (METRIC_NAME, APP_ID, INSTANCE_ID, " +
      "SERVER_TIME)) DATA_BLOCK_ENCODING='%s', IMMUTABLE_ROWS=true, " +
      "TTL=%s, COMPRESSION='%s'";

  public static final String CREATE_METRICS_CLUSTER_AGGREGATE_HOURLY_TABLE_SQL =
    "CREATE TABLE IF NOT EXISTS METRIC_AGGREGATE_HOURLY " +
      "(METRIC_NAME VARCHAR, " +
      "APP_ID VARCHAR, " +
      "INSTANCE_ID VARCHAR, " +
      "SERVER_TIME UNSIGNED_LONG NOT NULL, " +
      "UNITS CHAR(20), " +
      "METRIC_SUM DOUBLE, " +
      "METRIC_COUNT UNSIGNED_INT, " +
      "METRIC_MAX DOUBLE, " +
      "METRIC_MIN DOUBLE " +
      "CONSTRAINT pk PRIMARY KEY (METRIC_NAME, APP_ID, INSTANCE_ID, " +
      "SERVER_TIME)) DATA_BLOCK_ENCODING='%s', IMMUTABLE_ROWS=true, " +
      "TTL=%s, COMPRESSION='%s'";

  /**
   * ALTER table to set new options
   */
  public static final String ALTER_SQL = "ALTER TABLE %s SET TTL=%s";

  /**
   * Insert into metric records table.
   */
  public static final String UPSERT_METRICS_SQL = "UPSERT INTO %s " +
    "(METRIC_NAME, HOSTNAME, APP_ID, INSTANCE_ID, SERVER_TIME, START_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "METRIC_MAX, " +
    "METRIC_MIN, " +
    "METRIC_COUNT, " +
    "METRICS) VALUES " +
    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String UPSERT_CLUSTER_AGGREGATE_SQL = "UPSERT INTO " +
    "METRIC_AGGREGATE (METRIC_NAME, APP_ID, INSTANCE_ID, SERVER_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "HOSTS_COUNT, " +
    "METRIC_MAX, " +
    "METRIC_MIN) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String UPSERT_CLUSTER_AGGREGATE_TIME_SQL = "UPSERT INTO" +
    " %s (METRIC_NAME, APP_ID, INSTANCE_ID, SERVER_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "METRIC_COUNT, " +
    "METRIC_MAX, " +
    "METRIC_MIN) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


  public static final String UPSERT_AGGREGATE_RECORD_SQL = "UPSERT INTO " +
    "%s (METRIC_NAME, HOSTNAME, APP_ID, INSTANCE_ID, " +
    "SERVER_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "METRIC_MAX, " +
    "METRIC_MIN," +
    "METRIC_COUNT) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  /**
   * Retrieve a set of rows from metrics records table.
   */
  public static final String GET_METRIC_SQL = "SELECT %s METRIC_NAME, " +
    "HOSTNAME, APP_ID, INSTANCE_ID, SERVER_TIME, START_TIME, UNITS, " +
    "METRIC_SUM, " +
    "METRIC_MAX, " +
    "METRIC_MIN, " +
    "METRIC_COUNT, " +
    "METRICS " +
    "FROM %s";

  public static final String GET_METRIC_AGGREGATE_ONLY_SQL = "SELECT %s " +
    "METRIC_NAME, HOSTNAME, APP_ID, INSTANCE_ID, SERVER_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "METRIC_MAX, " +
    "METRIC_MIN, " +
    "METRIC_COUNT " +
    "FROM %s";

  public static final String GET_CLUSTER_AGGREGATE_SQL = "SELECT %s " +
    "METRIC_NAME, APP_ID, " +
    "INSTANCE_ID, SERVER_TIME, " +
    "UNITS, " +
    "METRIC_SUM, " +
    "HOSTS_COUNT, " +
    "METRIC_MAX, " +
    "METRIC_MIN " +
    "FROM METRIC_AGGREGATE";

  public static final String METRICS_RECORD_TABLE_NAME = "METRIC_RECORD";
  public static final String METRICS_AGGREGATE_MINUTE_TABLE_NAME =
    "METRIC_RECORD_MINUTE";
  public static final String METRICS_AGGREGATE_HOURLY_TABLE_NAME =
    "METRIC_RECORD_HOURLY";
  public static final String METRICS_CLUSTER_AGGREGATE_TABLE_NAME =
    "METRIC_AGGREGATE";
  public static final String METRICS_CLUSTER_AGGREGATE_HOURLY_TABLE_NAME =
    "METRIC_AGGREGATE_HOURLY";
  public static final String DEFAULT_TABLE_COMPRESSION = "SNAPPY";
  public static final String DEFAULT_ENCODING = "FAST_DIFF";
  public static final long NATIVE_TIME_RANGE_DELTA = 120000; // 2 minutes

  /**
   * Filter to optimize HBase scan by using file timestamps. This prevents
   * a full table scan of metric records.
   *
   * @return Phoenix Hint String
   */
  public static String getNaiveTimeRangeHint(Long startTime, Long delta) {
    return String.format("/*+ NATIVE_TIME_RANGE(%s) */", (startTime - delta));
  }

  public static PreparedStatement prepareGetMetricsSqlStmt(
    Connection connection, Condition condition) throws SQLException {

    if (condition.isEmpty()) {
      throw new IllegalArgumentException("Condition is empty.");
    }
    String stmtStr;
    if (condition.getStatement() != null) {
      stmtStr = condition.getStatement();
    } else {
      stmtStr = String.format(GET_METRIC_SQL,
        getNaiveTimeRangeHint(condition.getStartTime(), NATIVE_TIME_RANGE_DELTA),
        METRICS_RECORD_TABLE_NAME);
    }

    StringBuilder sb = new StringBuilder(stmtStr);
    sb.append(" WHERE ");
    sb.append(condition.getConditionClause());
    String orderByClause = condition.getOrderByClause();

    if (orderByClause != null) {
      sb.append(orderByClause);
    } else {
      sb.append(" ORDER BY METRIC_NAME, SERVER_TIME ");
    }
    if (condition.getLimit() != null) {
      sb.append(" LIMIT ").append(condition.getLimit());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("SQL: " + sb.toString() + ", condition: " + condition);
    }
    PreparedStatement stmt = connection.prepareStatement(sb.toString());
    int pos = 1;
    if (condition.getMetricNames() != null) {
      for (; pos <= condition.getMetricNames().size(); pos++) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Setting pos: " + pos + ", value = " + condition.getMetricNames().get(pos - 1));
        }
        stmt.setString(pos, condition.getMetricNames().get(pos - 1));
      }
    }
    if (condition.getHostname() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getHostname());
      }
      stmt.setString(pos++, condition.getHostname());
    }
    if (condition.getAppId() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getAppId());
      }
      stmt.setString(pos++, condition.getAppId());
    }
    if (condition.getInstanceId() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getInstanceId());
      }
      stmt.setString(pos++, condition.getInstanceId());
    }
    if (condition.getStartTime() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getStartTime());
      }
      stmt.setLong(pos++, condition.getStartTime());
    }
    if (condition.getEndTime() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getEndTime());
      }
      stmt.setLong(pos, condition.getEndTime());
    }
    if (condition.getFetchSize() != null) {
      stmt.setFetchSize(condition.getFetchSize());
    }

    return stmt;
  }


  public static PreparedStatement prepareGetLatestMetricSqlStmt(
    Connection connection, Condition condition) throws SQLException {

    if (condition.isEmpty()) {
      throw new IllegalArgumentException("Condition is empty.");
    }

    if (condition.getMetricNames() == null
      || condition.getMetricNames().size() == 0) {
      throw new IllegalArgumentException("Point in time query without " +
        "metric names not supported ");
    }

    String stmtStr;
    if (condition.getStatement() != null) {
      stmtStr = condition.getStatement();
    } else {
      stmtStr = String.format(GET_METRIC_SQL,
        "",
        METRICS_RECORD_TABLE_NAME);
    }

    StringBuilder sb = new StringBuilder(stmtStr);
    sb.append(" WHERE ");
    sb.append(condition.getConditionClause());
    String orderByClause = condition.getOrderByClause();
    if (orderByClause != null) {
      sb.append(orderByClause);
    } else {
      sb.append(" ORDER BY SERVER_TIME DESC, METRIC_NAME  ");
    }

    sb.append(" LIMIT ").append(condition.getMetricNames().size());

    if (LOG.isDebugEnabled()) {
      LOG.debug("SQL: " + sb.toString() + ", condition: " + condition);
    }
    PreparedStatement stmt = connection.prepareStatement(sb.toString());
    int pos = 1;
    if (condition.getMetricNames() != null) {
      //IGNORE condition limit, set one based on number of metric names
      for (; pos <= condition.getMetricNames().size(); pos++) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Setting pos: " + pos + ", value = " + condition.getMetricNames().get(pos - 1));
        }
        stmt.setString(pos, condition.getMetricNames().get(pos - 1));
      }
    }
    if (condition.getHostname() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getHostname());
      }
      stmt.setString(pos++, condition.getHostname());
    }
    if (condition.getAppId() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getAppId());
      }
      stmt.setString(pos++, condition.getAppId());
    }
    if (condition.getInstanceId() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getInstanceId());
      }
      stmt.setString(pos++, condition.getInstanceId());
    }

    if (condition.getFetchSize() != null) {
      stmt.setFetchSize(condition.getFetchSize());
    }

    return stmt;
  }

  public static PreparedStatement prepareGetAggregateSqlStmt(
    Connection connection, Condition condition) throws SQLException {

    if (condition.isEmpty()) {
      throw new IllegalArgumentException("Condition is empty.");
    }

    StringBuilder sb = new StringBuilder(GET_CLUSTER_AGGREGATE_SQL);
    sb.append(" WHERE ");
    sb.append(condition.getConditionClause());
    sb.append(" ORDER BY METRIC_NAME, SERVER_TIME");
    if (condition.getLimit() != null) {
      sb.append(" LIMIT ").append(condition.getLimit());
    }

    String query = String.format(sb.toString(),
      PhoenixTransactSQL.getNaiveTimeRangeHint(condition.getStartTime(),
        NATIVE_TIME_RANGE_DELTA));
    if (LOG.isDebugEnabled()) {
      LOG.debug("SQL => " + query + ", condition => " + condition);
    }
    PreparedStatement stmt = connection.prepareStatement(query);
    int pos = 1;
    if (condition.getMetricNames() != null) {
      for (; pos <= condition.getMetricNames().size(); pos++) {
        stmt.setString(pos, condition.getMetricNames().get(pos - 1));
      }
    }
    // TODO: Upper case all strings on POST
    if (condition.getAppId() != null) {
      stmt.setString(pos++, condition.getAppId());
    }
    if (condition.getInstanceId() != null) {
      stmt.setString(pos++, condition.getInstanceId());
    }
    if (condition.getStartTime() != null) {
      stmt.setLong(pos++, condition.getStartTime());
    }
    if (condition.getEndTime() != null) {
      stmt.setLong(pos, condition.getEndTime());
    }

    return stmt;
  }

  public static PreparedStatement prepareGetLatestAggregateMetricSqlStmt(
    Connection connection, Condition condition) throws SQLException {

    if (condition.isEmpty()) {
      throw new IllegalArgumentException("Condition is empty.");
    }

    if (condition.getMetricNames() == null
      || condition.getMetricNames().size() == 0) {
      throw new IllegalArgumentException("Point in time query without " +
        "metric names not supported ");
    }

    String stmtStr;
    if (condition.getStatement() != null) {
      stmtStr = condition.getStatement();
    } else {
      stmtStr = String.format(GET_CLUSTER_AGGREGATE_SQL, "");
    }

    StringBuilder sb = new StringBuilder(stmtStr);
    sb.append(" WHERE ");
    sb.append(condition.getConditionClause());
    String orderByClause = condition.getOrderByClause();
    if (orderByClause != null) {
      sb.append(orderByClause);
    } else {
      sb.append(" ORDER BY SERVER_TIME DESC, METRIC_NAME  ");
    }

    sb.append(" LIMIT ").append(condition.getMetricNames().size());

    String query = sb.toString();
    if (LOG.isDebugEnabled()) {
      LOG.debug("SQL: " + query + ", condition: " + condition);
    }

    PreparedStatement stmt = connection.prepareStatement(query);
    int pos = 1;
    if (condition.getMetricNames() != null) {
      for (; pos <= condition.getMetricNames().size(); pos++) {
        stmt.setString(pos, condition.getMetricNames().get(pos - 1));
      }
    }
    if (condition.getAppId() != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting pos: " + pos + ", value: " + condition.getAppId());
      }
      stmt.setString(pos++, condition.getAppId());
    }
    if (condition.getInstanceId() != null) {
      stmt.setString(pos++, condition.getInstanceId());
    }

    return stmt;
  }

  static interface Condition {

    boolean isEmpty();

    List<String> getMetricNames();
    boolean isPointInTime();
    boolean isGrouped();
    void setStatement(String statement);
    String getHostname();
    String getAppId();
    String getInstanceId();
    String getConditionClause();
    String getOrderByClause();
    String getStatement();
    Long getStartTime();
    Long getEndTime();
    Integer getLimit();
    Integer getFetchSize();
    void setFetchSize(Integer fetchSize);
    void addOrderByColumn(String column);
    void setNoLimit();
  }

  static class DefaultCondition implements Condition {
    List<String> metricNames;
    String hostname;
    String appId;
    String instanceId;
    Long startTime;
    Long endTime;
    Integer limit;
    boolean grouped;
    boolean noLimit = false;
    Integer fetchSize;
    String statement;
    Set<String> orderByColumns = new LinkedHashSet<String>();

    DefaultCondition(List<String> metricNames, String hostname, String appId,
              String instanceId, Long startTime, Long endTime, Integer limit,
              boolean grouped) {
      this.metricNames = metricNames;
      this.hostname = hostname;
      this.appId = appId;
      this.instanceId = instanceId;
      this.startTime = startTime;
      this.endTime = endTime;
      this.limit = limit;
      this.grouped = grouped;
    }

    public String getStatement() {
      return statement;
    }

    public void setStatement(String statement) {
      this.statement = statement;
    }

    public List<String> getMetricNames() {
      return metricNames == null || metricNames.isEmpty() ? null : metricNames;
    }

    String getMetricsClause() {
      StringBuilder sb = new StringBuilder("(");
      if (metricNames != null) {
        for (String name : getMetricNames()) {
          if (sb.length() != 1) {
            sb.append(", ");
          }
          sb.append("?");
        }
        sb.append(")");
        return sb.toString();
      } else {
        return null;
      }
    }

    public String getConditionClause() {
      StringBuilder sb = new StringBuilder();
      boolean appendConjunction = false;

      if (getMetricNames() != null) {
        if (appendConjunction) {
          sb.append(" AND");
        }

        sb.append("METRIC_NAME IN ");
        sb.append(getMetricsClause());
        appendConjunction = true;
      }

      appendConjunction = append(sb, appendConjunction, getHostname(), " HOSTNAME = ?");
      appendConjunction = append(sb, appendConjunction, getAppId(), " APP_ID = ?");
      appendConjunction = append(sb, appendConjunction, getInstanceId(), " INSTANCE_ID = ?");
      appendConjunction = append(sb, appendConjunction, getStartTime(), " SERVER_TIME >= ?");
      append(sb, appendConjunction, getEndTime(), " SERVER_TIME < ?");

      return sb.toString();
    }

    protected static boolean append(StringBuilder sb,
                                     boolean appendConjunction,
                             Object value, String str) {
      if (value != null) {
        if (appendConjunction) {
          sb.append(" AND");
        }

        sb.append(str);
        appendConjunction = true;
      }
      return appendConjunction;
    }

    public String getHostname() {
      return hostname == null || hostname.isEmpty() ? null : hostname;
    }

    public String getAppId() {
      if (appId != null && !appId.isEmpty()) {
        if (!appId.equals("HOST")) {
          return appId.toLowerCase();
        } else {
          return appId;
        }
      }
      return null;
    }

    public String getInstanceId() {
      return instanceId == null || instanceId.isEmpty() ? null : instanceId;
    }

    /**
     * Convert to millis.
     */
    public Long getStartTime() {
      if (startTime == null) {
        return null;
      } else if (startTime < 9999999999l) {
        return startTime * 1000;
      } else {
        return startTime;
      }
    }

    public Long getEndTime() {
      if (endTime == null) {
        return null;
      }
      if (endTime < 9999999999l) {
        return endTime * 1000;
      } else {
        return endTime;
      }
    }

    public void setNoLimit() {
      this.noLimit = true;
    }

    public Integer getLimit() {
      if (noLimit) {
        return null;
      }
      return limit == null ? PhoenixHBaseAccessor.RESULTSET_LIMIT : limit;
    }

    public boolean isGrouped() {
      return grouped;
    }

    public boolean isPointInTime() {
      return getStartTime() == null && getEndTime() == null;
    }

    public boolean isEmpty() {
      return (metricNames == null || metricNames.isEmpty())
        && (hostname == null || hostname.isEmpty())
        && (appId == null || appId.isEmpty())
        && (instanceId == null || instanceId.isEmpty())
        && startTime == null
        && endTime == null;
    }

    public Integer getFetchSize() {
      return fetchSize;
    }

    public void setFetchSize(Integer fetchSize) {
      this.fetchSize = fetchSize;
    }

    public void addOrderByColumn(String column) {
      orderByColumns.add(column);
    }

    public String getOrderByClause() {
      String orderByStr = " ORDER BY ";
      if (!orderByColumns.isEmpty()) {
        StringBuilder sb = new StringBuilder(orderByStr);
        for (String orderByColumn : orderByColumns) {
          if (sb.length() != orderByStr.length()) {
            sb.append(", ");
          }
          sb.append(orderByColumn);
        }
        sb.append(" ");
        return sb.toString();
      }
      return null;
    }

    @Override
    public String toString() {
      return "Condition{" +
        "metricNames=" + metricNames +
        ", hostname='" + hostname + '\'' +
        ", appId='" + appId + '\'' +
        ", instanceId='" + instanceId + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", limit=" + limit +
        ", grouped=" + grouped +
        ", orderBy=" + orderByColumns +
        ", noLimit=" + noLimit +
        '}';
    }
  }

  static class LikeCondition extends DefaultCondition {

    LikeCondition(List<String> metricNames, String hostname,
                  String appId, String instanceId, Long startTime,
                  Long endTime, Integer limit, boolean grouped) {
      super(metricNames, hostname, appId, instanceId, startTime, endTime,
        limit, grouped);
    }

    @Override
    public String getConditionClause() {
      StringBuilder sb = new StringBuilder();
      boolean appendConjunction = false;

      if (getMetricNames() != null) {
        sb.append("(");
        for (String name : getMetricNames()) {
          if (sb.length() > 1) {
            sb.append(" OR ");
          }
          sb.append("METRIC_NAME LIKE ?");
        }

        sb.append(")");
        appendConjunction = true;
      }

      appendConjunction = append(sb, appendConjunction, getHostname(), " HOSTNAME = ?");
      appendConjunction = append(sb, appendConjunction, getAppId(), " APP_ID = ?");
      appendConjunction = append(sb, appendConjunction, getInstanceId(), " INSTANCE_ID = ?");
      appendConjunction = append(sb, appendConjunction, getStartTime(), " SERVER_TIME >= ?");
      append(sb, appendConjunction, getEndTime(), " SERVER_TIME < ?");

      return sb.toString();
    }
  }

  static class SplitByMetricNamesCondition implements Condition {
    private final Condition adaptee;
    private String currentMetric;

    SplitByMetricNamesCondition(Condition condition){
      this.adaptee = condition;
    }

    @Override
    public boolean isEmpty() {
      return adaptee.isEmpty();
    }

    @Override
    public List<String> getMetricNames() {
      return Collections.singletonList(currentMetric);
    }

    @Override
    public boolean isPointInTime() {
      return adaptee.isPointInTime();
    }

    @Override
    public boolean isGrouped() {
      return adaptee.isGrouped();
    }

    @Override
    public void setStatement(String statement) {
      adaptee.setStatement(statement);
    }

    @Override
    public String getHostname() {
      return adaptee.getHostname();
    }

    @Override
    public String getAppId() {
      return adaptee.getAppId();
    }

    @Override
    public String getInstanceId() {
      return adaptee.getInstanceId();
    }

    @Override
    public String getConditionClause() {
      StringBuilder sb = new StringBuilder();
      boolean appendConjunction = false;

      if (getMetricNames() != null) {
        for (String name : getMetricNames()) {
          if (sb.length() > 1) {
            sb.append(" OR ");
          }
          sb.append("METRIC_NAME = ?");
        }

        appendConjunction = true;
      }

      appendConjunction = DefaultCondition.append(sb, appendConjunction,
        getHostname(), " HOSTNAME = ?");
      appendConjunction = DefaultCondition.append(sb, appendConjunction,
        getAppId(), " APP_ID = ?");
      appendConjunction = DefaultCondition.append(sb, appendConjunction,
        getInstanceId(), " INSTANCE_ID = ?");
      appendConjunction = DefaultCondition.append(sb, appendConjunction,
        getStartTime(), " SERVER_TIME >= ?");
      DefaultCondition.append(sb, appendConjunction, getEndTime(),
        " SERVER_TIME < ?");

      return sb.toString();
    }

    @Override
    public String getOrderByClause() {
      return adaptee.getOrderByClause();
    }

    @Override
    public String getStatement() {
      return adaptee.getStatement();
    }

    @Override
    public Long getStartTime() {
      return adaptee.getStartTime();
    }

    @Override
    public Long getEndTime() {
      return adaptee.getEndTime();
    }

    @Override
    public Integer getLimit() {
      return adaptee.getLimit();
    }

    @Override
    public Integer getFetchSize() {
      return adaptee.getFetchSize();
    }

    @Override
    public void setFetchSize(Integer fetchSize) {
      adaptee.setFetchSize(fetchSize);
    }

    @Override
    public void addOrderByColumn(String column) {
      adaptee.addOrderByColumn(column);
    }

    @Override
    public void setNoLimit() {
      adaptee.setNoLimit();
    }

    public List<String> getOriginalMetricNames() {
      return adaptee.getMetricNames();
    }

    public void setCurrentMetric(String currentMetric) {
      this.currentMetric = currentMetric;
    }
  }
}
