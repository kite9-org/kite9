import { getMainSvg } from '../../../bundles/screen.js'
import { onlyLastSelected } from '../../../bundles/api.js'
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { Selector } from '../../../bundles/types.js';
import { Metadata, MetadataCallback } from '../../../classes/metadata/metadata.js';
import { Transition } from '../../../classes/transition/transition.js';
import { getAppropriateResolver, UpdateableResolver } from '../navigable.js';

function initFocusUpdater(contentType: string, contentTypeResolver: UpdateableResolver) {

	return async (uri) => {
		return fetch(uri, {
			method: 'GET',
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

export function initFocusContextMenuCallback(
	transition: Transition,
	metadata: Metadata,
	selector: Selector = undefined): ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=focus]"));
		}
	}

	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {

		const e = onlyLastSelected(selector());
		
		const rd = getAppropriateResolver(transition, metadata, () => {})
		const updater = initFocusUpdater(rd.contentType, rd.resolver);

		if (e) {
			contextMenu.addControl(event, "/public/behaviours/navigable/focus/focus.svg", "Navigate To",
				function() {
					contextMenu.destroy();
					let url = e.getAttribute("id");
					const projection = url.indexOf('{');
					if (projection != -1) {
						url = url.substring(0, projection);
					}
					updater(url);
				});
		}
	}
}

/**
 * Stuff below allows us to handle back and forward controls in the browser 
 * and animate between page states.
 * 
 * initFocusMetadataCallback: keeps track of the current page details
 */
type FocusState = { title?: string, page?: string }

let state: FocusState = {

};

export function initFocusMetadataCallback(): MetadataCallback {

	return function(metadata) {
		const newUrl = metadata.self as string;
		if (state.page != newUrl) {
			const title = metadata.title as string
			if (title) {
				document.title = title;
			}

			const newState: FocusState = {
				title: title,
				page: newUrl
			}

			if (state.page == undefined) {
				// this is done for the initial page load
				history.replaceState(newState, title, newUrl);
			} else {
				history.pushState(newState, title, newUrl);
			}

			state = newState;
		}
	}
}


export function initFocus(transition : Transition, metadata: Metadata) {

	const rd = getAppropriateResolver(transition, metadata, () => {})
	const updater = initFocusUpdater(rd.contentType, rd.resolver);


	function popState(event: PopStateEvent) {
		state = event.state;
		document.title = event.state.title;
		updater(event.state.page);
	}

	window.removeEventListener('popstate', popState);
	window.addEventListener('popstate', popState);
}