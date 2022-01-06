import * as anime from '/webjars/animejs/3.0.1/lib/anime.es.js'

/*
 * This class handles the loading of new versions of the document from
 * either post requests or websockets, and orchestrates animation
 * between the versions.
 */
export class Transition {
	
	constructor(uri, topic) {
		this.loadCallbacks = [];
		this.animationCallbacks = [];
		this.documentCallbacks = [];
		const topicName = topic ? topic() : undefined;
		if (topicName) {
			this.socket = new WebSocket(topicName);
			this.socket.onopen = function(e) {
				console.log("command Websocket established");
			}
			this.socket.onmessage = (m) => {
				var parser = new DOMParser();
				var doc = parser.parseFromString(m.data, "image/svg+xml");
				if (doc.documentElement.tagName == 'svg') {
					this.change(doc.documentElement);
				}
				this.documentCallbacks.forEach(cb => cb(doc));
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
			})
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