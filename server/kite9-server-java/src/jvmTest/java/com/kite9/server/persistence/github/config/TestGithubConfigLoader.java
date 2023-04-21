package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import kotlin.text.Charsets;

public class TestGithubConfigLoader {

	@Test
	public void testConfig1LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config1.yml");
		ConfigLoaderImpl i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertEquals(".kite9/uploads", c.getUploads());
		Assertions.assertEquals(1, c.getSources().size());
		Assertions.assertEquals("/", c.getSources().get(0).getPath());
		Assertions.assertEquals("**/*.(adl|svg)", c.getSources().get(0).getPattern());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertEquals(Collections.singletonList(Config.DEFAULT_TEMPLATE), c.getTemplates());
	}
	
	@Test
	public void testConfig2LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config2.yml");
		ConfigLoaderImpl i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertEquals(".kite9/loader", c.getUploads());
		Assertions.assertEquals(2, c.getSources().size());
		Assertions.assertEquals("p1", c.getSources().get(0).getPath());
		Assertions.assertEquals("p2", c.getSources().get(1).getPath());
		Assertions.assertEquals("**/*.(adl|svg)", c.getSources().get(0).getPattern());
		Assertions.assertEquals("*", c.getSources().get(1).getPattern());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertFalse(c.getSources().get(1).isShowDirectories());
		Assertions.assertEquals(Arrays.asList(new String[] {"a", "b", "c"}), c.getTemplates());
	}
	
	@Test
	public void testConfig3LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config1.yml");
		ConfigLoaderImpl i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertSame(".kite9/uploads", c.getUploads());
		Assertions.assertEquals(1, c.getSources().size());
		Assertions.assertEquals("/", c.getSources().get(0).getPath());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertEquals(Collections.singletonList(Config.DEFAULT_TEMPLATE), c.getTemplates());
	}
}
