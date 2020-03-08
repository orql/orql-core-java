package com.github.orql.core;

import com.github.orql.core.sql.OrqlToSql;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class OrqlToSqlTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(OrqlToSqlTest.class);

    private OrqlToSql orqlToSql = new OrqlToSql();

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

    @Test
    public void testDelete() {
        String sql = orqlToSql.toDelete(parse("user(id = $id)"));
        logger.info("sql: {}", sql);
        assertEquals("delete from user where id = $id", sql);
    }

    @Test
    public void testDeleteIgnoreColumn() {
        String sql = orqlToSql.toDelete(parse("user(id = $id): {name}"));
        logger.info("sql: {}", sql);
        assertEquals("delete from user where id = $id", sql);
    }

    @Test
    public void testUpdate() {
        String sql = orqlToSql.toUpdate(parse("user(id = $id): {name}"));
        logger.info("sql: {}", sql);
        assertEquals("update user set name = $name where id = $id", sql);
    }

    @Test
    public void testUpdateAll() {
        String sql = orqlToSql.toUpdate(parse("user(id = $id): {*}"));
        logger.info("sql: {}", sql);
        assertEquals("update user set id = $id, name = $name, password = $password where id = $id", sql);
    }

    @Test
    public void testUpdateAllAndIgnoreId() {
        String sql = orqlToSql.toUpdate(parse("user(id = $id): {*, !id}"));
        logger.info("sql: {}", sql);
        assertEquals("update user set name = $name, password = $password where id = $id", sql);
    }

    @Test
    public void testUpdateBelongsTo() {
        String sql = orqlToSql.toUpdate(parse("user(id = $id) : {name, password, role}"));
        logger.info("sql: {}", sql);
        assertEquals("update user set name = $name, password = $password, role_id = $role.id where id = $id", sql);
    }

    @Test
    public void testQuerySimple() {
        String sql = orqlToSql.toQuery(QueryOp.QueryOne, parse("user(id = $id) : {name}"), false, null);
        logger.info("sql: {}", sql);
        assertEquals("select user.name as name from user as user where user.id = $id limit 1", sql);
    }

    @Test
    public void testQueryBelongsTo() {
        String sql = orqlToSql.toQuery(QueryOp.QueryOne, parse("user(id = $id) : {name, role: {id, name}}"), false, null);
        logger.info("sql: {}", sql);
        assertEquals("select user.name as name, role.id as role_id, role.name as role_name from user as user left join role as role on role.id = user.role_id where user.id = $id limit 1", sql);
    }

    @Test
    public void testQueryHasOne() {
        String sql = orqlToSql.toQuery(QueryOp.QueryOne, parse("user(id = $id) : {name, info : {birthday}}"), false, null);
        logger.info("sql: {}", sql);
        assertEquals("select user.name as name, info.birthday as info_birthday from user as user inner join user_info as info on info.userId = user.id where user.id = $id limit 1", sql);
    }

}
