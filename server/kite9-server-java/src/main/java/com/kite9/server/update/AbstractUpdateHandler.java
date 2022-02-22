package com.kite9.server.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.logging.Logable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.command.Command;
import com.kite9.pipeline.command.CommandContext;
import com.kite9.server.adl.holder.meta.MetaHelper;
import com.kite9.server.command.BatikCommandContext;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.update.Update.Type;

/**
 * Handles updates, provided by the {@link Update} class.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractUpdateHandler implements Logable, UpdateHandler {
	
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	protected final CommandContext ctx = new BatikCommandContext();
		
	public AbstractUpdateHandler() {
		super();
	}
	
	@Override
	public ADLDom performDiagramUpdate(Update update, Authentication authentication, DiagramWriteFormat f) throws Exception {
		ModifiableAPI a = getModifiableAPI(update, authentication);
		
		if (a instanceof ModifiableDiagramAPI) {
			ModifiableDiagramAPI api = (ModifiableDiagramAPI) a;
			ADLDom dom = api.getCurrentRevisionContent(authentication, update.getHeaders()).parse();
			
			List<Command> commands = update.getCommands();
			
			List<Command.Mismatch> errors = new ArrayList<>();
			
			if (update.getType() == Type.UNDO) {
				Collections.reverse(commands);
				for (Command command : commands) {
					Command.Mismatch status = command.undoCommand(dom, ctx);
					LOG.debug("Completed {} with status {}", command, status);
					if (status != null) {
						errors.add(status);
					}
				}
			} else {
				for (Command command : commands) {
					Command.Mismatch status = command.applyCommand(dom, ctx);
					LOG.debug("Completed {} with status {}", command, status);
					if (status != null) {
						errors.add(status);
					}
				}
			}
			
			if (errors.size() > 0) {
				dom.setError(errors.stream().map(s -> s.explain()).reduce("", (x, y) -> x + "\n" +y));
			}

			MetaHelper.setAuthorAndNotification(authentication, dom);
			api.addMeta(dom);
		
			if (LOG.isDebugEnabled()) {
				LOG.debug("Modified ADL: "+new XMLHelper().toXML(dom.getDocument(), true));
			}

			// this must come after processing, to make sure it renders correctly.
			if (api.getType(authentication) != ModifiableAPI.Type.VIEWONLY) {
				api.commitRevision("Changed "+update.getUri()+" in Kite9 Editor", authentication, dom);
			}

			return dom;
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not a diagram: "+update.getUri());
		}
	}

	
	protected abstract SourceAPI getSourceAPI(Update update, Authentication authentication) throws Exception;
	
	public ModifiableAPI getModifiableAPI(Update update, Authentication authentication) throws Exception {
		SourceAPI out = getSourceAPI(update, authentication);
		if (out instanceof ModifiableAPI) {
			return (ModifiableAPI) out;
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public String getProperCause(Throwable e) {
		if ((e instanceof Kite9ProcessingException) && (e.getCause() != null)) {
			return getProperCause(e.getCause());
		} else {
			return e.getClass().getCanonicalName()+": "+e.getMessage();
		}
	}

	@Override
	public String getPrefix() {
		return "UPDH";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}


}