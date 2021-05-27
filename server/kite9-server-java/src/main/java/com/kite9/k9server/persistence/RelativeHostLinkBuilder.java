package com.kite9.k9server.persistence;

import java.util.List;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.server.core.LinkBuilderSupport;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Allows for the construction of HATEAOS links, but without hostnames. 
 * This is preferable, since it's always going to be on the same machine, 
 * and means that we don't store absolute urls: those are problematic when 
 * running locally. 
 * 
 * @author robmoffat
 *
 */
public class RelativeHostLinkBuilder extends LinkBuilderSupport<RelativeHostLinkBuilder> {

	private static final UriComponents RELATIVE_HOST = UriComponentsBuilder.fromPath("/").build();

	private RelativeHostLinkBuilder(UriComponents builder) {
		super(builder);
	}
	
	private RelativeHostLinkBuilder(UriComponents components, List<Affordance> affordances) {
		super(components, affordances);
	}

	@Override
	protected RelativeHostLinkBuilder getThis() {
		return this;
	}
	
	public static RelativeHostLinkBuilder linkToCurrentMapping() {
		return new RelativeHostLinkBuilder(RELATIVE_HOST);
	}

	@Override
	protected RelativeHostLinkBuilder createNewInstance(UriComponents components, List<Affordance> affordances) {
		return new RelativeHostLinkBuilder(components, affordances);
	}

}
