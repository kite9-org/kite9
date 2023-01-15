import { createSVGResolver } from './svgResolver.js'
import { createAdlToSVGResolver } from './adlResolver.js'
import { canRenderClientSide } from '../../bundles/screen.js'
import { Command, Update } from '../../classes/command/command.js';
import { Metadata } from '../../classes/metadata/metadata.js';
import { Transition } from '../../classes/transition/transition.js';

export type UpdateableResolver = (text: string) => void;
export type Updater = (u: Update) => void;

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
	contentType: string): Updater {

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

export function initHttpUpdater(uri: string, contentType: string, contentTypeResolver: UpdateableResolver): Updater {

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
					console.log("Fetch error");
					console.log(error);
				}
			});
	}

}

/**
 * This version of the updater adapts depending on what the (initial) meta-data says to do.
 */
export function initMetadataBasedUpdater(
	command: Command,
	metadata: Metadata,
	transition: Transition): Updater {

	const processViaWebSocket = metadata.get("topic") != null;

	// for now, all rendering done on the client
	const renderServerSide = !canRenderClientSide();

	const resolver = renderServerSide ?
		createSVGResolver(transition, metadata) :
		createAdlToSVGResolver(transition, command, metadata);

	const contentType = renderServerSide ?
		"image/svg+xml;purpose=editable" :
		"text/xml;purpose=editable_adl";

	let delegate : Updater; 

	if (processViaWebSocket) {
		// logged in, use websockets
		delegate = initWebsocketUpdater(
			metadata.get("topic") as string,
			metadata.get("self") as string,
			resolver,
			contentType);

	} else {
		// not using web-sockets
		delegate = initHttpUpdater(
			metadata.get("self") as string,
			contentType,
			resolver);
	}

	return (update) => {
		// detect meta change, maybe?
		delegate(update);
	}

}