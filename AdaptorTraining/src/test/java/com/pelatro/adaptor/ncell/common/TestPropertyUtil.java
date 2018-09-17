package com.pelatro.adaptor.ncell.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestPropertyUtil {
	private PropertyUtil propertyUtil;
	@Before
		public void setup()
		{
			propertyUtil=PropertyUtil.getInstance();
		}
	@Test
		public void checkForSameInstance()
		{
			PropertyUtil pu=PropertyUtil.getInstance();
			assertEquals(propertyUtil, pu);
		}
	@Test
		public void getProperty()
		{
			assertEquals("Run A Specific Adaptor",
			propertyUtil.getProperty("message.adaptortype"));
		}
}
