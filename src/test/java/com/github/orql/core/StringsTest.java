package com.github.orql.core;

import com.github.orql.core.util.OrqlUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class StringsTest {

    private static final Logger logger = LoggerFactory.getLogger(StringsTest.class);

    @Test
    public void testGetSchema() {
        String name = OrqlUtil.getSchema("user:{id, name}");
        assertEquals("user", name);
    }

    @Test
    public void testPattern() {
        String typePatternString = "(.+?)\\((.+?)\\)";
        Pattern typePattern = Pattern.compile(typePatternString);
        Matcher m = typePattern.matcher("bigint(10)");
        if (m.find()) {
            assertEquals("bigint", m.group(1));
            assertEquals("10", m.group(2));
        }
    }

}
