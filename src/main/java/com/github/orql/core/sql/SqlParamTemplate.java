package com.github.orql.core.sql;

/**
 * sql参数模板
 * 用于定制不同生成器的sql参数
 * mybatis模板: sqlGenerator.setSqlParamTemplate(param -> "#{" + param.getName() + "}");
 */
@FunctionalInterface
public interface SqlParamTemplate {

    String gen(SqlNode.SqlParam param);

}
