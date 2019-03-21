package com.frame.base;

import com.frame.annotations.TestContextConfiguration;
import com.frame.config.AbstractTestBase;

@TestContextConfiguration(locations = {"classpath:springtest.properties","config/test.properties"})
public class BaseTest extends AbstractTestBase {
}
