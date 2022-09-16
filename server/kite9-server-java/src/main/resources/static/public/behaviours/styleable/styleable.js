import { hasLastSelected } from '/public/bundles/api.js'
import { parseStyle, formatStyle } from '/public/bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues, fieldset, select, numeric } from '/public/bundles/form.js'
import { getMainSvg, getElementHTMLBBox, canRenderClientSide } from '/public/bundles/screen.js';

/**
 * Common, used by other styleable controls.
 */
export function addNumericControl(overlay, cssAttribute, style, horiz, inverse, sx, sy, inheritedLength, boxMove = (e) => {}) {
	var val = style[cssAttribute];
	var length = inheritedLength;
	var placeholderText;
	if ((val) && val.endsWith("px")) {
		val = val.substring(0, val.length-2);
		length = parseFloat(val);
		placeholderText = "revert to default"
	} else {
		placeholderText = "default ("+inheritedLength.toFixed(1)+")"
	}
	
	const box = numeric(cssAttribute, val, {"min" : "0", "placeholder": placeholderText});
	const input = box.children[1];
	
	const sizer = overlay.createSizingArrow(sx, sy, length, horiz, inverse, (v) => {
		input.value = v;
		boxMove(v)
	});

	// event for when the size is changed in the context menu
	input.addEventListener("input", (e) => {
		if (input.value) {
			const mpx = Math.max(input.value,0);
			sizer(mpx);	
			boxMove(mpx)
		} else {
			sizer(inheritedLength);
			boxMove(inheritedLength);
		}
	})
	
	return box;
}

export function extractFormValues() {
	const asArray = Object.entries(formValues("enum"));
	const filtered = asArray.filter(([key, value]) => !((key=='ok') || (key=='cancel')));
	return Object.fromEntries(filtered);
}

export function initStyleContextMenuCallback(command, overlay, icon, name, buildControlsCallback, selector, styleSuffix, initChangeEvent) {

	var originalStyleMap, style;
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui].selected");
		}
	}
	
	if (styleSuffix == undefined) {
		styleSuffix = function(prop) {
			 if ((prop.indexOf("length") > -1) || 
				(prop.indexOf("width")  > -1) ||
				(prop.indexOf("height")  > -1) ||
				(prop.indexOf("left")  > -1) ||
				(prop.indexOf("right")  > -1) ||
				(prop.indexOf("top")  > -1) ||
				(prop.indexOf("bottom")  > -1)) {
				return "px";
			} else {
				return '';
			}
		}
	}	
	
	function createStyleSteps(e, oldValues, newValues) {
		function styleEqual(a, b, suffix) {
			if (((!a) || (a.length == 0)) && ((!b) || (b.length == 0))) {
				return true;
			} else {
				return a+suffix == b;
			}
		}
		
		function addSuffix(p, v) {
			if ((v) && (v.length > 0)) {
				return v + styleSuffix(p);
			} else {
				return undefined;
			}
		}
		
		const out = Object.keys(newValues)
			.filter(s => !styleEqual(oldValues[s],newValues[s], styleSuffix(s)))
			.map(f =>  { return {
			fragmentId: e.getAttribute("id"),
			type: 'ReplaceStyle',
			name: f,
			to: addSuffix(f, newValues[f]),
			from: oldValues[f]
		}});
		
		return out;
	}
	
	function createStyleMap(command, selectedElements) {
		const out = {};
		selectedElements.forEach(e => {
			const adlElement = command.getADLDom(e.getAttribute("id"))
			const style = parseStyle(adlElement.getAttribute("style"));
			out[e.getAttribute("id")] = style;
		})
		
		return out;
	}
	
	if (initChangeEvent == undefined) {
		initChangeEvent = function(selectedElement, svgStyle) {
			return e => {
				const values = extractFormValues('enum');
				const newStyle = {...svgStyle, ...values};
				const formatted = formatStyle(newStyle);
	
				selectedElement.setAttribute("style", formatted);
			};	
		}
	}
	
	return function(event, cm) {
		
		const selectedElement = hasLastSelected(selector(), true);

		if (selectedElement) {
			
			cm.addControl(event, icon, name, () => {
				cm.clear();
				overlay.ensureOverlay();
				const selectedElements = Array.from(hasLastSelected(selector()));
				originalStyleMap = createStyleMap(command, selectedElements);
				style = originalStyleMap[selectedElement.getAttribute("id")];
				const originalSvgStyle = selectedElement.getAttribute("style")
				const svgStyle = parseStyle(originalSvgStyle);
				
				
				const theForm = form([				
					...buildControlsCallback(selectedElement, style, overlay, cm, event),
					inlineButtons([
						ok('ok', {}, (e) => {
							const values = extractFormValues('enum');
							const steps = selectedElements
								.flatMap(e => createStyleSteps(e, originalStyleMap[e.getAttribute("id")], values));
							selectedElement.classList.add('selected');
							command.pushAllAndPerform(steps);
							cm.destroy();
							overlay.destroy()
							event.stopPropagation();
  				 			event.preventDefault();
						}),
						cancel('cancel', [], () => {
							selectedElement.setAttribute("style", originalSvgStyle);
							selectedElement.classList.add('selected');
							cm.destroy()
							overlay.destroy();
							event.stopPropagation();
  				 			event.preventDefault();
						})
					])
				], 'enum');
				
				const changeEvent = initChangeEvent(selectedElement, svgStyle);
								
				theForm.addEventListener("change", changeEvent);
				theForm.addEventListener("textInput", changeEvent);
				theForm.addEventListener("input", changeEvent);
				
				const htmlElement = cm.get(event);
				htmlElement.appendChild(theForm);	
				changeEvent();
			});
				
		}
	}
}



export function moveContextMenuAway(cm, e, event) {
	const hbbox = getElementHTMLBBox(e);
	// move context menu out of the way
	const menuDiv = cm.get(event);
	menuDiv.style.left = (hbbox.x + hbbox.width + 5)+"px";
	menuDiv.style.top = (hbbox.y + hbbox.height - 25)+"px";
}

export function initBasicBuildControls(properties, values) {
	return function(selectedElement, style, overlay, cm, event) {
		return [ fieldset(name, Object.keys(properties).map(p => select(p, style[p], {}, [ '', ...values ] ))) ];
	}
}