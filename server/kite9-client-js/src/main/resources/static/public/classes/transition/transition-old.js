import * as anime from '/webjars/animejs/3.0.1/lib/anime.es.js';
import { ensureJs, once } from '/public/bundles/ensure.js';

/**
 * Replace with ES6 module as soon as possible
 */
once(function() {
	ensureJs('/webjars/kotlin/1.4.30/kotlin.js');	
	setTimeout(() => {
		ensureJs('/webjars/kite9-visualization-js/0.1-SNAPSHOT/kite9-visualization-js.js');
	}, 20)
//	ensureJs('/public/external/SaxonJS2.js');
})

const XSL_TEMPLATE_NAMESPACE = "http://www.kite9.org/schema/xslt";
const ADL_NAMESPACE = "http://www.kite9.org/schema/adl";

var update = null;	// will contain dom element for updated svg

/**
 * Resolves the URL for the template, contained in the template attribute of the doc.
 */
function getTemplateUri(doc) {

	const template = doc.documentElement.getAttributeNS(XSL_TEMPLATE_NAMESPACE, "template");
	
	if ((template == null) || (template.length() == 0)) {
		if (ADL_NAMESPACE == doc.documentElement.namespaceURI) {
			// default to the basic template
			return new URL("/public/templates/basic/basic-template.xsl", document.location.href).href;
		}
	}
	
	return new URL(template, document.location.href).href;
}

/*
 * This class handles the loading of new versions of the document from
 * either post requests or websockets, and orchestrates animation
 * between the versions.
 */
export class Transition {
	
	constructor(uri, resolver, updater) {
		this.loadCallbacks = [];
		this.animationCallbacks = [];
		this.documentCallbacks = [];
		this.transformer = null;
		const topicName = topic ? topic() : undefined;
		if (topicName) {
			this.socket = new WebSocket(topicName);
			this.socket.onopen = function(e) {
				console.log("command Websocket established");
			}
			this.socket.onmessage = (m) => {
			    // here, the message is in ADL format and needs to be
			    // transformed
				var parser = new DOMParser();
				var doc = parser.parseFromString(m.data, "text/xml");


				if (doc.documentElement.tagName == 'svg') {
					// old way - doc contains svg
					this.change(doc.documentElement);
					this.documentCallbacks.forEach(cb => cb(doc));
				} else if (doc.documentElement.namespaceURI == ADL_NAMESPACE) {
					// adl returned - do transform first
					this.xslt(doc, getTemplateUri(doc), result => {
						this.change(result.documentElement);
						this.documentCallbacks.forEach(cb => cb(doc));	
					});				
				} else {
					// probably just metadata
					this.documentCallbacks.forEach(cb => cb(doc));
				}
			};
			this.socket.onerror = (e) => {
				alert("Problem with websocket: "+ JSON.stringify(e));
			}
			this.socket.onclose = () => alert("Connection Closed - Please Reload Page");
		}
		
		this.uri = uri;
	}
	
	load(cb) {
		this.loadCallbacks.push(cb);
	}
	
	document(cb) {
		this.documentCallbacks.push(cb);
	}
	
	animation(cb) {
		this.animationCallbacks.push(cb);
	}
	
	
	change(documentElement) {
	

		// create the animation timeline
		var tl = anime.default.timeline({
			easing: 'easeOutExpo',
			duration: 10000,
			autoplay: false,
		});

    	this.animationCallbacks.forEach(cb => cb(documentElement, tl))
		tl.play();
		
		// force the DOMContentLoaded event to occur again
		var evt = document.createEvent('Event');
		evt.initEvent('DOMContentLoaded', false, false);
		window.dispatchEvent(evt);
	}

	handleErrors(response) {
		if (!response.ok) {
			return response.json().then(j => {
				//console.log(JSON.stringify(j));
				throw new Error(j.message);
			});
		}
		return response;
	}
	
	handleRedirect(response) {
		return response;
	}

	mainHandler(p) {
		return p
			.then(this.handleErrors)
			.then(this.handleRedirect)
			.then(response => {
				this.loadCallbacks.forEach(cb => cb(response));
				return response;
			})
			.then(response => response.text())
			.then(text => {
				var parser = new DOMParser();
				var doc = parser.parseFromString(text, "image/svg+xml");
				return doc;
			})
			.then(doc => {
				this.change(doc.documentElement);
				this.documentCallbacks.forEach(cb => cb(doc));
				return doc;
			})
	}
	
	xslt(doc, uri, callback) {
		if (this.transformer == null) {
			// using this to make sure document location is correct.
			var xhr = new XMLHttpRequest;
			xhr.open('GET', uri);
			xhr.responseType = 'document';
			xhr.overrideMimeType('text/xml');
			xhr.onload = function () {
			  	if (xhr.readyState === xhr.DONE && xhr.status === 200) {
					this.transformer = new XSLTProcessor();
					this.transformer.importStylesheet(xhr.responseXML);	
					const result = this.transformer.transformToDocument(doc);
					if (update == null) {
						window['kite9-visualization-js'].initCSS();
						
					 	update = document.createElement("div");
						update.setAttribute("id", "_update");
						document.body.appendChild(update);
						
						//var item = document.createElement("div");
    					//item.setAttribute("k9-palette", p.selector);
					}
					
					// we have to add the result to the main dom 
					// to get the computedStyleMap and format
					update.replaceChildren();
					const docElement = result.documentElement
					update.appendChild(docElement)
					window['kite9-visualization-js'].formatSVG(docElement);
					
					// put it back in the result 
					result.appendChild(docElement);
					callback(result);
				} else {
					// error
				}
			};
				
			xhr.send();		
		} else {
			return callback(this.transformer.transformToDocument(doc));
		}
  	}

	get(uri) {
		return this.mainHandler(fetch(uri, {
			method: 'GET',
			headers: this.getHeaders()
		}));
	}
	
	getHeaders() {
		var out = {
			"Content-Type": "application/json",
			"Accept": "image/svg+xml;purpose=editable, application/json"
		};
		
		return out;
	}
	
	setCredentials(jwt) {
		this.jwt = jwt;
	}

	update(update) {
		const uri = this.uri();
		const uriModified = new URL(uri);

		update.uri = uriModified;
		
		if (this.socket) {
			// use saved version
			update.base64adl = null;
			const text = JSON.stringify(update);
			this.socket.send(text);
		} else {
			return this.mainHandler(fetch(uriModified, {
				method: 'POST',
				body: JSON.stringify(update),
				headers: this.getHeaders()
			})).catch(e => {
				//this.get(this.uri());
				alert(e);
			});
		}
	}
		
	
	
}