import { createSVGResolver } from '/public/behaviours/updatable/svgResolver.js';
import { createAdlToSVGResolver } from '/public/behaviours/updatable/adlResolver.js';

/**
 * there are three ways of processing updates:
 * 1.  Send the update as XML to the server via HTTP
 * 2.  Send the update over the web-socket, retrieving XML/SVG.
 * 2.  Apply the update locally to the XML, using javascript commands, retrieving XML/SVG.
 *
 * All the methods return a function returning a promise.
 */
 
export function initWebsocketUpdater(uri, contentTypeResolver) {
	
	const socket = new WebSocket(uri.replace('http', 'ws'));
	
	socket.onopen = function(e) {
		console.log("command Websocket established")
	}
	
	
	socket.onmessage = (m) => {
		return new Promise(m)
			.then(p => contentTypeResolver(p))
			.then(svg => transition(svg));

	}
	
	socket.onerror = (e) => {
		alert("Problem with websocket: "+ JSON.stringify(e))
	}
	
	socket.onclose = () => alert("Connection Closed - Please Reload Page")
	
	
	return (update) => {
		update.uri = new URL(uri)
		update.base64adl = null;
		const text = JSON.stringify(update)
		socket.send(text)
	};
	
} 
 
export function initHttpUpdater(uri, contentType, contentTypeResolver) {
	
	/** Really basic error handler */
	function handleErrors(response) {
		if (!response.ok) {
			return response.json().then(j => {
				//console.log(JSON.stringify(j));
				throw new Error(j.message);
			});
		}
		return response;
	}
	
	return (update) => {
		update.uri = new URL(uri);
	
		try {
            return fetch(uri, {
                	method: 'POST',
	                body: JSON.stringify(update),
	                headers: {
	                    "Content-Type": "application/json",
	                    "Accept": contentType
	                }
	            })
	            .then(handleErrors)
				.then(response => response.text())
	            .then(text => contentTypeResolver(text))
        } catch (e) {
            alert(e);
        }
	}
	
} 

/**
 * This updater applies the commands locally
 */
export function initLocalUpdater(adl, contentTypeResolver) {
	
	
	
}



/**
 * This version of the updater adapts depending on what the meta-data says to do.
 * It also figures out what 
 */
export function initMetadataBasedUpdater(command, metadata, transition, renderServerSide) {
	
	var resolver = renderServerSide ? 
			createSVGResolver(transition, metadata) :
			createAdlToSVGResolver(transition, metadata);
	var contentType = renderServerSide ? 
			"image/svg+xml;purpose=editable, application/json" :
			"text/xml;purpose=adl";

	var delegate;
	
	if (metadata.get("user") != undefined) {
		// logged in, use websockets
		delegate = initWebsocketUpdater(
			metadata.get("self"), 
			resolver);
		
	} else {
		// not using web-sockets
		delegate = initHttpUpdater(
			metadata.get("self"), 
			contentType, 
			resolver);
	}
	
	return (update) => {
		// detect meta change, maybe?
		delegate(update);
	}
	
}