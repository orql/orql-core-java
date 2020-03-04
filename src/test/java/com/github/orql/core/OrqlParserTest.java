package com.github.orql.core;

import com.github.orql.core.orql.OrqlNode.*;
import com.github.orql.core.orql.Parser;
import com.github.orql.core.schema.SchemaManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OrqlParserTest {

    private SchemaManager schemaManager;

    @Before
    public void init() {
        schemaManager = new SchemaManager();
        schemaManager.scanPackage("com.github.orql.core.schema");
    }

    private OrqlRefItem parse(String orql) {
        Parser parser = new Parser(schemaManager);
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
}
