package com.kite9.server.sources;

import java.util.List;

import com.kite9.server.update.Update;
import org.springframework.security.core.Authentication;

public class PlugableContentAPIFactory implements SourceAPIFactory {
	
	List<SourceAPIFactory> subordinateFactories;
	
	public PlugableContentAPIFactory(List<SourceAPIFactory> subordinateFactories) {
		this.subordinateFactories = subordinateFactories;
	}

	@Override
	public SourceAPI createAPI(Update u, Authentication a) throws Exception {
		for (SourceAPIFactory contentAPIFactory : subordinateFactories) {
			SourceAPI out = contentAPIFactory.createAPI(u, a);
			if (out != null) {
				return out;
			}
		}
		
		return null;
	}

}
