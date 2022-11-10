import { getMainSvg, is_touch_device4 } from '../../bundles/screen.js'
import { hasLastSelected } from '../../bundles/api.js'
import { Selector } from '../../bundles/types.js';
import { Dragger, DragLocatorCallback, DropCallback } from '../../classes/dragger/dragger.js';
import { Command } from '../../classes/command/command.js';
import { ContextMenuCallback } from '../../classes/context-menu/context-menu.js';

function defaultDragableSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=drag]"));
}

export function initDragable(dragger: Dragger, selector: Selector = undefined) {

	if (selector == undefined) {
		selector = defaultDragableSelector;
	}

	const drag = (e: MouseEvent) => dragger.drag(e);
	const drop = (e: MouseEvent) => dragger.drop(e);
	const grab = () => dragger.grab();

	window.addEventListener('DOMContentLoaded', function() {

		document.querySelectorAll("svg").forEach(svg => {
			svg.removeEventListener("mousemove", drag);
			svg.addEventListener("mousemove", drag);

			svg.removeEventListener("touchmove", drag);
			svg.addEventListener("touchmove", drag, { passive: false });
		})

		document.removeEventListener("mouseup", drop);
		document.addEventListener("mouseup", drop);

		document.removeEventListener("touchend", drop);
		document.addEventListener("touchend", drop);

		selector().forEach(function(v) {
			v.removeEventListener("mouseup", drop);
			v.removeEventListener("touchend", drop);

			v.removeEventListener("mousedown", grab);
			v.removeEventListener("touchstart", grab);

			v.addEventListener("mouseup", drop);
			v.addEventListener("touchend", drop);

			v.addEventListener("mousedown", grab);
			v.addEventListener("touchstart", grab);
		})
	})
}

/**
 * Returns the objects that are selected being dragged
 */
export function initDragableDragLocator(selector: Selector = undefined) : DragLocatorCallback {

	if (selector == undefined) {
		selector = function() {
			let selectorText = "div.main [id][k9-ui~='drag'].selected";
			if (is_touch_device4()) {
				// we only allow dragging of elements that are selected, otherwise we lose a lot
				// of resize 

			} else {
				// for mice, also allow element under the mouse
				selectorText += ", div.main [id][k9-ui~='drag'].mouseover";
			}

			return Array.from(document.querySelectorAll(selectorText))
		}
	}

	return function() {
		const out = selector();
		return out;
	}

}

/**
 * This makes sure that hoverable doesn't apply to elements being dragged
 */
export function initMainHoverableAllowed() {
	return function(element) {
		const parent = element == undefined ? undefined : element.parentElement;
		if (parent == undefined) {
			return false;
		}
		return !(parent.getAttribute("id") == "_moveLayer");
	}
}

export function initCompleteDragable(command: Command) : DropCallback {

	return function() {
		command.perform();
	}
}

/**
 * Provides an icon in the context menu to move things 
 * without needing to hold the mouse down
 */
export function initDragContextMenuCallback(dragger: Dragger, selector: Selector = null) : ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return defaultDragableSelector()
				.filter(e => e.classList.contains("selected"));
		}
	}

	/**
	 * Provides move option in context menu
	 */
	return function(event, cm) {

		const elements = hasLastSelected(selector());
		if (elements.length > 0) {
			cm.addControl(event, "/public/behaviours/dragable/drag.svg", 'Drag Selected Elements', () => {
				cm.destroy();
				dragger.beginMove(event, false)
			});
		}
	}
}

