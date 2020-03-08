package com.github.orql.core;

import com.github.orql.core.orql.OrqlNode.*;
import com.github.orql.core.orql.OrqlParser;
import com.github.orql.core.schema.SchemaManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OrqlParserTest extends TestBase {

    private OrqlRefItem parse(String orql) {
        OrqlParser parser = new OrqlParser(schemaManager);
        return parser.parse(orql).getRoot();
    }

    @Test
    public void testSimple() {
        OrqlRefItem item = parse("user: {id, name}");
        Assert.assertEquals("user", item.getName());
    }

    @Test
    public void testAllItem() {
        OrqlRefItem item = parse("user: {*}");
        Assert.assertTrue(item.getChildren().get(0) instanceof OrqlAllItem);
    }

    @Test
    public void testAllItemIgnore() {
        OrqlRefItem item = parse("user: {*, !password}");
        Assert.assertTrue(item.getChildren().get(0) instanceof OrqlAllItem);
        Assert.assertTrue(item.getChildren().get(1) instanceof OrqlIgnoreItem);
        Assert.assertEquals(item.getChildren().get(1).getName(), "password");
    }

    @Test
    public void testWhereSimple() {
        OrqlRefItem item = parse("user(id = $id)");
        Assert.assertTrue(item.getWhere() instanceof OrqlColumnExp);
    }
}
