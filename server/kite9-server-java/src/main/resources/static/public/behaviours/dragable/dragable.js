import { getMainSvg, is_touch_device4 } from '/public/bundles/screen.js'
import { hasLastSelected } from '/public/bundles/api.js'

function defaultDragableSelector() {
	return getMainSvg().querySelectorAll("[k9-info][k9-ui~=drag]");
}

export function initDragable(dragger, selector) {
	
	if (selector == undefined) {
		selector = defaultDragableSelector;
	}
	
	const drag = (e) => dragger.drag(e);
	const drop = (e) => dragger.drop(e);
	const grab = (e) => dragger.grab(e);
	
	window.addEventListener('DOMContentLoaded', function(event) {

		document.querySelectorAll("svg").forEach(svg => {
			svg.removeEventListener("mousemove", drag);
			svg.addEventListener("mousemove", drag);
			
			svg.removeEventListener("touchmove", drag);
			svg.addEventListener("touchmove", drag, { passive: false});
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
export function initDragableDragLocator(selector) {
	
	if (selector == undefined) {
		selector = function() {
			var selectorText = "div.main [id][k9-ui~='drag'].selected";
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
		var out = selector();
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

export function initCompleteDragable(command) {
	
	return function() {
		command.perform();
	}
}

/**
 * Provides an icon in the context menu to move things 
 * without needing to hold the mouse down
 */
export function initDragContextMenuCallback(dragger, selector) {
	
	if (selector == undefined) {
		selector = defaultDragableSelector;
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

