package com.kite9.server.persistence.github;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MarketplaceEventController {
	
	public static final Logger LOG = LoggerFactory.getLogger(MarketplaceEventController.class);

	private ObjectMapper om = new ObjectMapper();
	
	@PostMapping(path="/api/marketplace-events", produces = {MediaType.TEXT_PLAIN_VALUE}, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public String handleEvent(RequestEntity<Map<String, Object>> in) {
		try {
			LOG.info("Marketplace Event: {}", om.writeValueAsString(in.getBody()));
			return "ok";
		} catch (JsonProcessingException e) {
			LOG.error("Couldn't record marketplace event", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Couldn't process event",  e);
		}
	}
}
