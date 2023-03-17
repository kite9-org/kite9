import { getMainSvg } from '../../../bundles/screen.js'
import { onlyLastSelected } from '../../../bundles/api.js'
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { Command } from '../../../classes/command/command.js';
import { Selector } from '../../../bundles/types.js';
import { MetadataCallback } from '../../../classes/metadata/metadata.js';
import { Transition } from '../../../classes/transition/transition.js';


export function initFocusContextMenuCallback(
	command: Command,
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

		if (e) {
			contextMenu.addControl(event, "/public/behaviours/navigable/focus/focus.svg", "Focus On",
				function() {
					contextMenu.destroy();
					let url = e.getAttribute("id");
					const projection = url.indexOf('{');
					if (projection != -1) {
						url = url.substring(0, projection);
					}
					command.get(url);
				});
		}
	}
}

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

export function initFocus(transition : Transition) {

	function popState(event: Event) {
		state = event.state;
		document.title = event.state.title;
		command.get(event.state.page);
	}

	window.removeEventListener('popstate', popState);
	window.addEventListener('popstate', popState);
}
