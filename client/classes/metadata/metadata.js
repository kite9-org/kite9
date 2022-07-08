import { onlyUnique } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.5'

/**
 * Handles monitoring of document metadata, and callbacks for when it changes.
 */
export class Metadata {

	constructor() {
		this.callbacks = [];
		this.metadata = {};
		this.process(document);
	}
	
	convert(e) {
		if (e == null) {
			return {};
		}
		const children = Array.from(e.children);
		const tagName = e.tagName;
		const isMap = (tagName.endsWith('author')) || (tagName.endsWith('user')) || (tagName.endsWith('metadata'));
		
		if (children.length == 0) {
			return e.textContent;
		} else if (isMap) {
			// map mode
			const out = {};			
			children.forEach(e => out[e.tagName.substring(3)] = this.convert(e));
			return out;
		} else {
			// array mode
			return children.map(e => this.convert(e));
		}
	}
	
	process(d) {
		const olds = this.metadata;
		
		// remove these transient metadata elements.
		delete olds['notification'];
		delete olds['error'];
		
		const md_element = d.querySelector("metadata");
		const news = this.convert(md_element);
		this.metadata = { ...olds, ...news };
		
		if ((md_element != null) && (md_element.parentElement != null)) {
			md_element.parentElement.removeChild(md_element);
		}
		
		
		this.callbacks.forEach(cb => {
			cb(this.metadata);
		});	
	}
	
	add(cb) {
		this.callbacks.push(cb);
		cb(this.metadata);
	}
	
	get(item) {
		return this.metadata[item];
	}
	
	isEditor() {
		const role = this.get('role');
		
		// by only strictly ignoring viewers, we allow people to edit the public templates, 
		// even though they can't be saved.
		return (role != 'viewer');
	}
}