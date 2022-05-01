import { createSVGResolver } from '/public/behaviours/updatable/svgResolver.js';

/**
 * there are three ways of processing updates:
 * 1.  Send the update as XML to the server via HTTP
 * 2.  Send the update over the web-socket, retrieving XML/SVG.
 * 2.  Apply the update locally to the XML, using javascript commands, retrieving XML/SVG.
 *
 * All the methods return a function returning a promise.
 */
 
export function initWebsocketUpdater(uri, contentTypeResolver, transition) {
	
	const socket = new WebSocket(uri);
	
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
 
export function initHttpUpdater(uri, contentType, contentTypeResolver, transition) {
	
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
	            .then(svg => transition.change(svg));
        } catch (e) {
            alert(e);
        }
	}
	
} 

export function initLocalUpdater(adl, contentTypeResolver, transition) {
	
	
	
}



/**
 * This version of the updater adapts depending on what the meta-data says to do.
 * It also figures out what 
 */
export function initMetadataBasedUpdater(metadata, transition) {
	
	var delegate;
	var resolver;
	
	if (metadata.get("user") != undefined) {
		// ok, maybe do something else.
		
	} else {
		const resolver = createSVGResolver();
		delegate = initHttpUpdater(
			metadata.get("self"), 
			"image/svg+xml;purpose=editable, application/json", 
			resolver, 
			transition);
	}
	
	return (update) => {
		// detect meta change, maybe?
		delegate(update);
	}
	
}