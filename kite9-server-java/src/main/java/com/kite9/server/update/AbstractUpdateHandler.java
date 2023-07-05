package com.kite9.server.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.logging.Logable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.command.Command;
import com.kite9.pipeline.command.CommandContext;
import com.kite9.server.adl.holder.meta.MetaHelper;
import com.kite9.server.command.BatikCommandContext;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.topic.ChangeBroadcaster;
import com.kite9.server.update.Update.Type;

/**
 * Handles updates, provided by the {@link Update} class.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractUpdateHandler implements Logable, UpdateHandler, ApplicationContextAware {
	
	private final Logger LOG = LoggerFactory.getLogger(AbstractUpdateHandler.class);
	protected final CommandContext ctx = new BatikCommandContext();
	protected ApplicationContext appCtx;
	private ObjectMapper logMapper = new ObjectMapper();
		
	public AbstractUpdateHandler() {
		super();
	}
	
	@Override
	public ADLDom performDiagramUpdate(Update update, Authentication authentication) throws Exception {
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
					if (LOG.isDebugEnabled()) {
						LOG.debug("Completed {} with status {}", jsonify(command), explain(status));
					}
					if (status != null) {
						errors.add(status);
					}
				}
			} else {
				for (Command command : commands) {
					Command.Mismatch status = command.applyCommand(dom, ctx);
					LOG.debug("Completed {} with status {}", jsonify(command), explain(status));
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

			if (api.getModificationType(authentication) != ModifiableAPI.ModificationType.VIEWONLY) {
				api.commitRevision("Changed "+update.getUri()+" in Kite9 Editor", authentication, dom);
			}
			
			broadcastChange(dom);

			return dom;
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not a diagram: "+update.getUri());
		}
	}

	protected String explain(Command.Mismatch status) {
		return status == null ? "" : status.explain();
	}
	
	protected String jsonify(Command command) throws JsonProcessingException {
		return logMapper.writeValueAsString(command);
	}

	protected void broadcastChange(ADLDom dom) {
		Collection<ChangeBroadcaster> values = appCtx.getBeansOfType(ChangeBroadcaster.class).values();
		values.stream()
			.forEach(cb -> cb.broadcast(dom));
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

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appCtx = applicationContext;
	}

}