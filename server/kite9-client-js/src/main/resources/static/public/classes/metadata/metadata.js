import { onlyUnique } from '/public/bundles/api.js';

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
		
		const news = this.convert(d.querySelector("metadata"));
		this.metadata = { ...olds, ...news };
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



/**
 * Attach this to the transition object to allow Metadata to 
 * receive messages about document changes.
 */
export function initMetadataTransitionCallback(metadata) {
	
	return function(doc) {
		
		metadata.process(doc);
		
		metadata.callbacks.forEach(cb => {
			cb(metadata.metadata);
		});	
		
	}
} 

