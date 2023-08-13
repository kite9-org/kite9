import { CommandCallback } from '../../../classes/command/command.js';
import { Metadata } from '../../../classes/metadata/metadata.js';
import { Transition } from '../../../classes/transition/transition.js';
import { ADLUpdateCallback, getAppropriateResolver, UpdateableResolver } from '../navigable.js';

/**
 * Makes sure that the websocket uses ws/wss where needed
 */
function ensureCorrectScheme(uri: string, contentType: string) {
	const url = new URL(document.location.href)
	const topicUri = new URL(uri)
	if (url.protocol == 'http:') {
		topicUri.protocol = 'ws:'
	} else if (url.protocol == 'https:') {
		topicUri.protocol = 'wss:'
	}

	topicUri.search = "contentType=" + contentType;

	const wsUrl = topicUri.toString()
	return wsUrl;
}


/**
 * there are three ways of processing updates:
 * 1.  Send the update as XML to the server via HTTP
 * 2.  Send the update over the web-socket, retrieving XML/SVG.
 * 3.  Apply the update locally to the XML, using javascript commands, retrieving XML/SVG.
 *
 * All the methods return a function returning a promise.
 */

export function initWebsocketUpdater(
	socketUri: string,
	pageUri: string,
	contentTypeResolver: UpdateableResolver,
	contentType: string): CommandCallback {

	const socket = new WebSocket(ensureCorrectScheme(socketUri, contentType));

	socket.onopen = function() {
		// console.log("command Websocket established")
	}

	socket.onmessage = (m) => {
		// console.log("received message "+m.data)
		contentTypeResolver(m.data)
	}

	socket.onerror = (e) => {
		alert("Problem with websocket: " + JSON.stringify(e))
	}

	socket.onclose = () => alert("Connection Closed - Please Reload Page")


	return (update) => {
		update.uri = new URL(pageUri)
		update.base64adl = null;
		const text = JSON.stringify(update)
		socket.send(text)
	};

}

export function initHttpUpdater(uri: string, contentType: string, contentTypeResolver: UpdateableResolver): CommandCallback {

	return async (update) => {
		update.uri = new URL(uri);

		return fetch(uri, {
			method: 'POST',
			body: JSON.stringify(update),
			headers: {
				"Content-Type": "application/json",
				"Accept": contentType
			}
		})
			.then(response => {
				if (!response.ok) {
					return Promise.reject(response)
				} else {
					return response.text()
				}
			})
			.then(text => contentTypeResolver(text))
			.catch(error => {
				if (typeof error.json === "function") {
					error.json().then(jsonError => {
						console.log("Json error from API");
						console.log(jsonError);
					}).catch(genericError => {
						console.log("Generic error from API");
						console.log(error.statusText);
					});
				} else {
					console.log("Update error");
					console.log(error);
					if (error.stack) {
					    console.log(error.stack);
					}
				}
			});
	}

}

/**
 * This version of the updater adapts depending on what the (initial) meta-data says to do.
 */
export function initMetadataBasedUpdater(
	metadata: Metadata,
	transition: Transition,
	adlCallback: ADLUpdateCallback = () => { /*no op*/} ): CommandCallback {

	const processViaWebSocket = metadata.get("topic") != null;

	const rd = getAppropriateResolver(transition, metadata, adlCallback);
	
	let delegate : CommandCallback; 

	if (processViaWebSocket) {
		// logged in, use websockets
		delegate = initWebsocketUpdater(
			metadata.get("topic") as string,
			metadata.get("self") as string,
			rd.resolver,
			rd.contentType);

	} else {
		// not using web-sockets
		delegate = initHttpUpdater(
			metadata.get("self") as string,
			rd.contentType,
			rd.resolver);
	}

	return (update) => {
		// detect meta change, maybe?
		delegate(update);
	}

}