import { createSVGResolver } from '/public/behaviours/updatable/svgResolver.js';
import { createAdlToSVGResolver } from '/public/behaviours/updatable/adlResolver.js';

/**
 * Makes sure that the websocket uses ws/wss where needed
 */
function ensureCorrectScheme(uri, contentType) {
	const url = new URL(document.location.href)
	const topicUri = new URL(uri)
	if (url.protocol == 'http:') {
		topicUri.protocol = 'ws:'
	} else if (url.protocol == 'https:') {
		topicUri.protocol = 'wss:'
	}
	
	topicUri.search = "contentType="+contentType;
	
	const wsUrl = topicUri.toString() 
	return wsUrl;
}


/**
 * there are three ways of processing updates:
 * 1.  Send the update as XML to the server via HTTP
 * 2.  Send the update over the web-socket, retrieving XML/SVG.
 * 2.  Apply the update locally to the XML, using javascript commands, retrieving XML/SVG.
 *
 * All the methods return a function returning a promise.
 */
 
export function initWebsocketUpdater(socketUri, pageUri, contentTypeResolver, contentType) {
	
	const socket = new WebSocket(ensureCorrectScheme(socketUri, contentType));
	
	socket.onopen = function(e) {
		// console.log("command Websocket established")
	}
	
	
	socket.onmessage = (m) => {
		// console.log("received message "+m.data)
		contentTypeResolver(m.data)
	}
	socket.onerror = (e) => {
		alert("Problem with websocket: "+ JSON.stringify(e))
	}
	
	socket.onclose = () => alert("Connection Closed - Please Reload Page")
	
	
	return (update) => {
		update.uri = new URL(pageUri)
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
 * Detect whether we can render on the client side
 */
function canRenderClientSide() {
	return (window.CSS.registerProperty != null);
}

/**
 * This version of the updater adapts depending on what the (initial) meta-data says to do.
 */
export function initMetadataBasedUpdater(command, metadata, transition) {
	
	const processViaWebSocket = metadata.get("topic") != null;
	
	// for now, all rendering done on the client
	const renderServerSide = !canRenderClientSide();	
	
	var resolver = renderServerSide ? 
			createSVGResolver(transition, metadata) :
			createAdlToSVGResolver(transition, command, metadata);
			
	var contentType = renderServerSide ? 
			"image/svg+xml;purpose=editable" :
			"text/xml;purpose=editable_adl";

	var delegate;
	
	if (processViaWebSocket) {
		// logged in, use websockets
		delegate = initWebsocketUpdater(
			metadata.get("topic"), 
			metadata.get("self"), 
			resolver, 
			contentType);
		
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