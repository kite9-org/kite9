package com.kite9.server.controllers;

import org.kite9.diagram.logging.Logable;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.update.Update;

/**
 * Applies commands to given xml url, but no persistence done.
 * 
 * @author robmoffat
 *
 */
@Controller 
public class CommandController extends AbstractNegotiatingController implements Logable {
		
	public CommandController(FormatSupplier fs, SourceAPIFactory factory) {
		super(fs, factory);
	}

	public static final String CHANGE_URL = "/command/v1";
	
	@RequestMapping(method={RequestMethod.POST}, path=CHANGE_URL, consumes= {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody ADLOutput applyCommandOnStatic (
			RequestEntity<Update> req,
			@RequestParam(required=true, name="on") String sourceUri) throws Exception {
		DiagramWriteFormat df = getOutputFormat(req);
		Update update = req.getBody();
		update.setUri(resolveAndWrap(req, sourceUri));
		update.addHeaders(req.getHeaders());
		
		return performDiagramUpdate(update, null, df)
			.process(update.getUri(), df);
	}

}
