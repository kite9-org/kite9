package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHTreeEntry;
import org.springframework.util.StreamUtils;

import kotlin.text.Charsets;

public class TestGithubConfigLoader {

	@Test
	public void testConfig1LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config1.yml");
		ConfigLoader i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertEquals(".kite9/uploads", c.getUploads());
		Assertions.assertEquals(1, c.getSources().size());
		Assertions.assertEquals("**/*.adl", c.getSources().get(0).getPattern());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertEquals(Config.DEFAULT_TEMPLATE_DIR, c.getTemplates());
		Assertions.assertTrue(c.test(createTreeEntry("bob/abc.adl", false)));
		Assertions.assertTrue(c.test(createTreeEntry("abc.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/file.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/uploads", true)));
		Assertions.assertTrue(c.test(createTreeEntry("uploads", true)));
	}
	
	private GHTreeEntry createTreeEntry(String path, boolean tree) {
		return new GHTreeEntry() {

			@Override
			public String getPath() {
				return path;
			}

			@Override
			public String getType() {
				return tree ? "tree" : "blob";
			}
		};
	}

	@Test
	public void testConfig2LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config2.yml");
		ConfigLoader i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertEquals(".kite9/loader", c.getUploads());
		Assertions.assertEquals(2, c.getSources().size());
		Assertions.assertEquals("p1/**/*.adl", c.getSources().get(0).getPattern());
		Assertions.assertEquals("p2/*", c.getSources().get(1).getPattern());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertFalse(c.getSources().get(1).isShowDirectories());
		Assertions.assertEquals("a", c.getTemplates());
		Assertions.assertFalse(c.test(createTreeEntry("bob/abc.adl", false)));
		Assertions.assertTrue(c.test(createTreeEntry("p1/abc.adl", false)));
		Assertions.assertTrue(c.test(createTreeEntry("p1/pop/abc.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry("abc.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/file.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/uploads", true)));
		Assertions.assertTrue(c.test(createTreeEntry("uploads", true)));
	}
	
	@Test
	public void testConfig3LoadsCorrectly() throws IOException {
		InputStream contents = this.getClass().getResourceAsStream("/config/config1.yml");
		ConfigLoader i = new ConfigLoaderImpl();
		Config c = i.loadConfig(StreamUtils.copyToString(contents, Charsets.UTF_8));
		Assertions.assertSame(".kite9/uploads", c.getUploads());
		Assertions.assertEquals(1, c.getSources().size());
		Assertions.assertTrue(c.getSources().get(0).isShowDirectories());
		Assertions.assertEquals(Config.DEFAULT_TEMPLATE_DIR, c.getTemplates());
		Assertions.assertTrue(c.test(createTreeEntry("bob/abc.adl", false)));
		Assertions.assertTrue(c.test(createTreeEntry("abc.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/file.adl", false)));
		Assertions.assertFalse(c.test(createTreeEntry(".kite9/uploads", true)));
		Assertions.assertTrue(c.test(createTreeEntry("uploads", true)));
	}
}
