package com.github.orql.core;

import com.github.orql.core.orql.OrqlNode;
import com.github.orql.core.orql.OrqlParser;
import com.github.orql.core.schema.SchemaManager;
import org.junit.Before;

public class TestBase {

    private SchemaManager schemaManager;

    private OrqlParser orqlParser;

    @Before
    public void init() {
        schemaManager = new SchemaManager();
        schemaManager.scanPackage("com.github.orql.core.schema");
        orqlParser = new OrqlParser(schemaManager);
    }

    public OrqlNode.OrqlRefItem parse(String orql) {
        return orqlParser.parse(orql).getRoot();
    }

}
