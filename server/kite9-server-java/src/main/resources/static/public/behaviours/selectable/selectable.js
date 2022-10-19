import { getMainSvg, currentTarget } from '/public/bundles/screen.js'
import { getKite9Target } from '/public/bundles/api.js'


export function clearLastSelected(within) {
	within.querySelectorAll(".lastSelected").forEach(c => {
		c.classList.remove("lastSelected")
	})
}

export function lastSelected(element) {
	element.classList.add("lastSelected");
}

export function select(element, within = getMainSvg()) {
	const classes= element.classList;
	classes.add("selected");

	// unselect nested elements
	element.querySelectorAll(".selected").forEach(c => {
		c.classList.remove("selected")
	})
	
	// unselect parent elements
	var v = element;
	while (v) {
		v = v.parentElement;
		if (v != null) {
			v.classList.remove("selected")
		}
	}
	
	clearLastSelected(within);
	lastSelected(element);
}

export function isSelected(element) {
	return element.classList.contains("selected");
}

export function isLastSelected(element) {
	return element.classList.contains("lastSelected");
}

export function singleSelect(element, within = getMainSvg()) {
	within.querySelectorAll(".selected").forEach(c => {
		c.classList.remove("selected");
	})
	
	element.classList.add("selected");
	
	clearLastSelected(within);
	lastSelected(element);
}

export function unselect(element) {
	element.classList.remove("selected")
}

// Adds .selected class when the user mouseups over an element.
// Adds .lastSelected class to a single element, which is the last one clicked on

export function initSelectable(selector, within, isSingleSelect) {
	
	if (within == undefined) {
		within = getMainSvg();
	}
	
	function mouseup(event) {
		if (event.handledSelect) {
			return;
		}
		
		var v = getKite9Target(currentTarget(event));
		
		if (v == undefined) {
			return;
		}
		
		if (!isSelected(v)) {
			if (isSingleSelect) {
				// unselect all other elements
				singleSelect(v, within);
			} else {
				select(v, within);
			}
		} else {
			unselect(v, within);
		}
		
		event.handledSelect = true;
	}
	
	if (selector == undefined) {
		selector = function() {
			return within.querySelectorAll("[id]");
		}
	}
	
	within.addEventListener("mousedown", mouseup);

}



