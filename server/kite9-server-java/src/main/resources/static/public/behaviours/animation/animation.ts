import { parseTransform, number, handleTransformAsStyle } from '../../bundles/api.js'
import { getMainSvg } from '../../bundles/screen.js'
import { Timeline } from '../../classes/transition/timeline.js';
import { TransitionAnimationCallback } from '../../classes/transition/transition.js';

/**
 * Somehow, someday, make these extensible
 */
const numeric = ['width', 'height', 'x', 'y', 'rx', 'ry'];
const editorClasses = ['selected', 'lastSelected'];

function reconcileTransform(fromElement: SVGGraphicsElement, tl: Timeline, fromValue: string, toValue: string) {
	const fromT = parseTransform(fromValue);
	const toT = parseTransform(toValue);
	tl.transform(fromElement, fromT, toT);
}

function reconcileStyles(fromElement: SVGGraphicsElement, toElement: SVGGraphicsElement, tl: Timeline) {
	const toStyles = Array.from(toElement.style);
	const fromStyles = Array.from(fromElement.style);
	const toRemove = fromStyles.filter(a => -1 == toStyles.indexOf(a));

	toRemove.forEach(a => fromElement.style[a] = null);

	toStyles.forEach(a => {

		const fromValue = fromElement.style[a];
		const toValue = toElement.style[a];

		if (fromValue !== toValue) {
			if (numeric.indexOf(a) != -1) {
				tl.style(fromElement, a, number(fromValue), number(toValue));
			} else if (a == 'transform') {
				reconcileTransform(fromElement, tl, fromValue, toValue);
			} else {
				// just change text
				fromElement.style[a] = toValue;
			}
		}
	})
}

function reconcileClasses(fromElement: Element, toElement: Element) {
  const toClasses = Array.from(toElement.classList);
  const fromClasses = Array.from(fromElement.classList);
  
  let toRemove = fromClasses.filter(a => -1 == toClasses.indexOf(a));
  const toAdd = toClasses.filter(a => -1 == toClasses.indexOf(a));
  toRemove = toRemove.filter(a => -1 == editorClasses.indexOf(a));
  
  toRemove.forEach(a => fromElement.classList.remove(a));
  toAdd.forEach(a => fromElement.classList.add(a));
}

function reconcileAttributes(fromElement: Element, toElement: Element, tl: Timeline) {
	const toAtts = Array.from(toElement.attributes).map(a => a.name);
	const fromAtts = Array.from(fromElement.attributes).map(a => a.name);
	const toRemove = fromAtts.filter(a => -1 == toAtts.indexOf(a));

	if (!(fromElement instanceof SVGGraphicsElement)) {
		return;
	}

	toRemove.forEach(a => fromElement.removeAttribute(a));
	toAtts.forEach(a => {
		const fromValue = fromElement.getAttribute(a);
		const toValue = toElement.getAttribute(a);

		if ((a.startsWith("xlink:")) && (fromValue !== toValue)) {
			fromElement.setAttributeNS("http://www.w3.org/1999/xlink", a.substring(a.indexOf(':') + 1), toValue);
		} else if (fromValue == null) {
			fromElement.setAttribute(a, toValue);
		} else if (fromValue !== toValue) {
			if (numeric.indexOf(a) != -1) {
				tl.attribute(fromElement, a, number(fromValue), number(toValue));
			} else if ((a == 'style') && (toElement instanceof SVGGraphicsElement)) {
				if (fromElement.tagName != 'svg') {
					reconcileStyles(fromElement, toElement, tl);
				}
			} else if (a == 'class') {
				reconcileClasses(fromElement, toElement);
			} else if (a == 'd') {
				tl.path(fromElement, fromValue, toValue);
			} else {
				fromElement.setAttribute(a, toValue);
			}
		}

	});
}

function reconcileText(fromElement: Element, toElement: Element) {
	if ((fromElement.tagName == 'script') && ("adl:markup" != fromElement.getAttribute("id"))) {
		// we don't reconcile scripts as this means doing a lot of 
		// parsing / reloading js
		return;
	}

	if (fromElement.textContent != toElement.textContent) {
		fromElement.textContent = toElement.textContent;
	}
}

function getLocalTranslate(e: SVGGraphicsElement) {
	const t = parseTransform(e.style.transform);
	return {
		x: t.translateX,
		y: t.translateY
	}
}

function getTotalTranslate(e: Node | null) {
	if (e instanceof SVGGraphicsElement) {
		const t = getLocalTranslate(e);
		const pt = getTotalTranslate(e.parentElement);
		const out = {
			x: t.x + pt.x,
			y: t.y + pt.y
		}
		return out;
	} else {
		return { x: 0, y: 0 };
	}
}

function removeNonElementContent(el: Element) {
  let child = el.firstChild;

  while (child) {
      const nextChild = child.nextSibling;
      if (child.nodeType == 3) {
          el.removeChild(child);
      }
      child = nextChild;
  }
}


function reconcileElement(inFrom: Element, inTo: Element, toDelete: Element, tl: Timeline) {
	const isMainSvg = (inFrom.tagName == 'svg');
	console.log("Reconciling " + inFrom.tagName + ' with ' + inTo.tagName + " " + inFrom.getAttribute("id") + " " + inTo.getAttribute("id"))

	if (!isMainSvg) {
		handleTransformAsStyle(inFrom);
		handleTransformAsStyle(inTo);
	}

	if (inTo.childElementCount == 0) {
		reconcileText(inFrom, inTo);
	} else {
		let ti = 0;
		let fi = 0;

		removeNonElementContent(inFrom);
		removeNonElementContent(inTo);

		while (ti < inTo.childElementCount) {
			const toElement = inTo.children.item(ti);
			const fromElement = (fi < inFrom.childElementCount) ? inFrom.children.item(fi) : null;

			if (toElement.hasAttribute("id")) {
				// ideally, we need to merge
				const toId = toElement.getAttribute("id");
				const fromId = fromElement == null ? null : fromElement.getAttribute("id");
				const missingFrom = inFrom.ownerDocument.getElementById(toId);

				if (toId == fromId) {
					// to/from correspond
					reconcileElement(fromElement, toElement, toDelete, tl);
					fi++;
					ti++;
				} else if (missingFrom instanceof SVGGraphicsElement) {
					// from element has moved
					handleTransformAsStyle(missingFrom);
					const parentFromTranslate = getTotalTranslate(missingFrom.parentElement);
					const parentToTranslate = getTotalTranslate(inFrom);
					const localFromTranslate = getLocalTranslate(missingFrom);
					const newTranslate = {
						x: localFromTranslate.x + parentFromTranslate.x - parentToTranslate.x,
						y: localFromTranslate.y + parentFromTranslate.y - parentToTranslate.y
					};
					//console.log("from moving" + newTranslate);
					inFrom.insertBefore(missingFrom, fromElement);
					missingFrom.style.transform = "translateX(" + newTranslate.x + "px) translateY(" + newTranslate.y + "px)";
					reconcileElement(missingFrom, toElement, toDelete, tl);
					ti++;
					fi++;
				} else {
					// to element is new  
					//console.log("creating new element " + toElement.tagName)
					const newFromElement = document.createElementNS(toElement.namespaceURI, toElement.tagName);
					inFrom.insertBefore(newFromElement, fromElement);
					reconcileElement(newFromElement, toElement, toDelete, tl);
					fi++;
					ti++;
				}
			} else {
				// here, we have non-id elements, so we really just need 
				// to be sure there are the right number

				if ((fromElement == null) || (fromElement.tagName != toElement.tagName) || (fromElement.hasAttribute("id"))) {
					// treat as an insertion.
					//console.log("creating new element " + toElement.tagName)
					const newFromElement = document.createElementNS(toElement.namespaceURI, toElement.tagName);
					inFrom.insertBefore(newFromElement, fromElement);
					reconcileElement(newFromElement, toElement, toDelete, tl);
					fi++;
					ti++;
				} else {
					// assume it's the same element (tags match, after all)
					reconcileElement(fromElement, toElement, toDelete, tl);
					fi++;
					ti++;
				}
			}

		}

		while (fi < inFrom.childElementCount) {
			const fromElement = inFrom.children.item(fi);
			//console.log("removing " + fromElement);
			if (fromElement == toDelete) {
				toDelete.parentElement.removeChild(toDelete);
			} else if ((fromElement.tagName == 'g') && (fromElement instanceof SVGGraphicsElement)) {
				handleTransformAsStyle(fromElement);
				const totalTranslate = getTotalTranslate(fromElement);
				toDelete.appendChild(fromElement);
				fromElement.style.transform = "translateX(" + totalTranslate.x + "px) translateY(" + totalTranslate.y + "px) ";
			} else {
				toDelete.appendChild(fromElement);
			}
		}
	}

	if (!isMainSvg) {
		reconcileAttributes(inFrom, inTo, tl);
	}

}

/**
 * This handles the process of loading new SVG and animating between the old one and the new one.
 */
export function initTransitionAnimationCallback() : TransitionAnimationCallback {

	return function(changeTo: SVGSVGElement, animationTimeline: Timeline) {
		document.body.style.cursor = null;

		// this will store everything we'll eventually remove
		const svg = getMainSvg();
		const toDelete = svg.ownerDocument.createElementNS(svg.namespaceURI, "g");
		svg.appendChild(toDelete);
		toDelete.setAttribute('id', '_deleteGroup');
		reconcileElement(svg, changeTo, toDelete, animationTimeline);
		console.log("Finished Animation")
	}

}