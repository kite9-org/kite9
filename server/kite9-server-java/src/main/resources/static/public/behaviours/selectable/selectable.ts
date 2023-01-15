import { getMainSvg, currentTarget } from '../../bundles/screen.js'
import { getKite9Target } from '../../bundles/api.js'
import { Selector } from '../../bundles/types.js';
import { addMonikaEventListener } from '../../bundles/monika.js';


export function clearLastSelected(within: Element) : void {
	within.querySelectorAll(".lastSelected").forEach(c => {
		c.classList.remove("lastSelected")
	})
}

export function lastSelected(element: Element) : void {
	element.classList.add("lastSelected");
}

export function select(element: Element, within : Element = getMainSvg()) : void {
	const classes= element.classList;
	classes.add("selected");

	// unselect nested elements
	element.querySelectorAll(".selected").forEach(c => {
		c.classList.remove("selected")
	})
	
	// unselect parent elements
	let v = element;
	while (v) {
		v = v.parentElement;
		if (v != null) {
			v.classList.remove("selected")
		}
	}
	
	clearLastSelected(within);
	lastSelected(element);
}

export function isSelected(element : Element) : boolean {
	return element.classList.contains("selected");
}

export function isLastSelected(element : Element) : boolean {
	return element.classList.contains("lastSelected");
}

export function singleSelect(elements : Element[], within : Element = getMainSvg()) : void {
	within.querySelectorAll(".selected").forEach(c => {
		c.classList.remove("selected");
	})
	
	elements.forEach(e => e.classList.add("selected"));
	
	clearLastSelected(within);
	lastSelected(elements[0]);
}

export function unselect(element : Element) : void {
	element.classList.remove("selected")
}

// Adds .selected class when the user mouseups over an element.
// Adds .lastSelected class to a single element, which is the last one clicked on

export function initSelectable(
	within :Element = undefined, 
	isSingleSelect = false) {
	
	if (within == undefined) {
		within = getMainSvg();
	}
	
	function mouseup(event: Event) {
		if (event['handledSelect']) {
			return;
		}
		
		const v = getKite9Target(currentTarget(event));
		
		if (v == undefined) {
			return;
		}
		
		if (!isSelected(v)) {
			if (isSingleSelect) {
				// unselect all other elements
				singleSelect([v], within);
			} else {
				select(v, within);
			}
		} else {
			unselect(v);
		}
		
		event['handledSelect'] = true;
	}
	
	addMonikaEventListener(within, "mousedown", "selectable", mouseup);

}



