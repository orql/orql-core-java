package com.github.orql.core;

import com.github.orql.core.orql.OrqlNode;
import com.github.orql.core.orql.OrqlParser;
import com.github.orql.core.sql.OrqlToSql;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class OrqlToSqlTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(OrqlToSqlTest.class);

    private OrqlToSql orqlToSql = new OrqlToSql();

    private OrqlParser orqlParser = new OrqlParser(schemaManager);

    private OrqlNode.OrqlRefItem parse(String orql) {
        OrqlParser parser = new OrqlParser(schemaManager);
        return parser.parse(orql).getRoot();
    }

    @Test
    public void testAdd() {
        String sql = orqlToSql.toAdd(parse("user : {name, password}"));
        logger.info("sql: {}", sql);
        assertEquals("insert into user (name, password) values ($name, $password)", sql);
    }

    @Test
    public void testAddAll() {
        String sql = orqlToSql.toAdd(parse("user : {*}"));
        logger.info("sql: {}", sql);
        assertEquals("insert into user (id, name, password) values ($id, $name, $password)", sql);
    }

    @Test
    public void testAddAllAndIgnore() {
        String sql = orqlToSql.toAdd(parse("user : {*, !id}"));
        logger.info("sql: {}", sql);
        assertEquals("insert into user (name, password) values ($name, $password)", sql);
    }

    @Test
    public void testAddBelongsTo() {
        String sql = orqlToSql.toAdd(parse("user : {name, password, role}"));
        logger.info("sql: {}", sql);
        assertEquals("insert into user (name, password, role_id) values ($name, $password, $role.id)", sql);
    }

}
