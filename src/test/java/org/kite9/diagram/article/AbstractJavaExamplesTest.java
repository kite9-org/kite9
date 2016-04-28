package org.kite9.diagram.article;

import java.io.IOException;

import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.framework.model.ProjectModel;
import org.kite9.tool.model.ClassFileModelBuilder;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class AbstractJavaExamplesTest extends AbstractFunctionalTest {

	public AbstractJavaExamplesTest() {
		super(); 
	}

	protected ProjectModel buildProjectModel(String packageRoot) throws IOException {
		FileSystemResourceLoader loader = new FileSystemResourceLoader();
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(loader);
		String locationPattern = "classpath*:/"+packageRoot.replace(".", "/")+"/**/*.class";
		Resource[] res = resourcePatternResolver.getResources(locationPattern);
		ClassFileModelBuilder builder = new ClassFileModelBuilder();
		for (Resource resource : res) {
			builder.visit(resource);
		}
		
		ProjectModel pm = builder.getModel();
		return pm;
	}

	
}