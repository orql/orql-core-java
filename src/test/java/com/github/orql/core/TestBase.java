package com.github.orql.core;

import com.github.orql.core.schema.SchemaManager;
import org.junit.Before;

public class TestBase {

    protected SchemaManager schemaManager;

    @Before
    public void init() {
        schemaManager = new SchemaManager();
        schemaManager.scanPackage("com.github.orql.core.schema");
    }

}
