import { hasLastSelected, isRectangular, parseInfo } from '/public/bundles/api.js'
import { parseStyle } from '/public/bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues } from '/public/bundles/form.js'
import { getMainSvg, getElementPageBBox, getElementHTMLBBox, canRenderClientSide } from '/public/bundles/screen.js';
import { numeric } from '/public/bundles/form.js'


function getCSSLengthPx(element, cssAttribute) {
	if (canRenderClientSide()) {
		const value = window['kite9-visualization-js'].getCssStyleDoubleProperty(cssAttribute, element);
		return value;
	}

	return 0;
}	

function addNumericControl(overlay, cssAttribute, name, style, horiz, inverse, sx, sy, inheritedLength) {
	var val = style[cssAttribute];
	var length = inheritedLength;
	if ((val) && val.endsWith("px")) {
		val = val.substring(0, val.length-2);
		length = parseFloat(val);
	}
	
	const box = numeric(name, val, {"min" : "0", "placeholder": "default"});
	const input = box.children[1];
	
	const sizer = overlay.createSizingArrow(sx, sy, length, horiz, inverse, (v) => input.value = v);

	// event for when the size is changed in the context menu
	input.addEventListener("input", (e) => {
		if (input.value) {
			sizer(Math.max(input.value,0));		
		} else {
			sizer(inheritedLength);
		}
	})
	
	return box;
}


function defaultSizingSelector() {
	return function() {
		return Array.from(getMainSvg().querySelectorAll("[id][k9-ui].selected"))
			.filter(e => isRectangular(e));
	}
}

function moveContextMenuAway(cm, e, event) {
	const hbbox = getElementHTMLBBox(e);
	// move context menu out of the way
	const menuDiv = cm.get(event);
	menuDiv.style.left = (hbbox.x + hbbox.width + 5)+"px";
	menuDiv.style.top = (hbbox.y + hbbox.height - 25)+"px";
}

function createStyleSteps(fields, element, form, prefix, style) {
	function styleEqual(a, b) {
		if (((!a) || (a.length == 0)) && ((!b) || (b.length == 0))) {
			return true;
		} else {
			return a+"px" == b;
		}
	}
	
	return fields.filter(f => !styleEqual(form[f],style[prefix +  f]))
		.map(f =>  { return {
			fragmentId: element.getAttribute("id"),
			type: 'ReplaceStyle',
			name: prefix + f,
			to: form[f] ? form[f]+"px" : undefined,
			from: style[prefix + f]
		}});
}

export function initMarginContextMenuCallback(command, overlay, selector) {
	
	if (selector == undefined) {
		selector = defaultSizingSelector();
	}
	
	

	return function(event, cm) {
		const selectedElement = hasLastSelected(selector(), true);
		if (selectedElement) {
			cm.addControl(event, "/public/behaviours/containers/size/margins.svg", 'Margins', () => {
				cm.clear();
				const initialMargins = parseInfo(selectedElement)['margin'].split(" ").map(x => parseFloat(x));
				const htmlElement = cm.get(event);
				const adlElement = command.getADLDom(selectedElement.getAttribute("id"))
				const style = parseStyle(adlElement.getAttribute("style"));
				const bbox = getElementPageBBox(selectedElement);
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-margin-left', 'Left', style, true, true, bbox.x, bbox.y + bbox.height / 2, initialMargins[3]),
					addNumericControl(overlay, '--kite9-margin-right', 'Right', style, true, false, bbox.x + bbox.width, bbox.y + bbox.height / 2, initialMargins[1]),
					addNumericControl(overlay, '--kite9-margin-top', 'Top', style, false, true, bbox.x + bbox.width / 2 ,bbox.y, initialMargins[0]),
					addNumericControl(overlay, '--kite9-margin-bottom', 'Bottom', style, false, false, bbox.x + bbox.width / 2, bbox.y + bbox.height, initialMargins[2]),
				]
				
				
				htmlElement.appendChild(form([
					...numericControls,
					inlineButtons([
						ok('ok', {}, (e) => {
							const selectedElements = hasLastSelected(selector());
							const fields = ['left', 'right', 'top', 'bottom']
							const values = formValues('margins');
							const steps = Array.from(selectedElements)
								.flatMap(e => createStyleSteps(fields, e, values, '--kite9-margin-', style));
							command.pushAllAndPerform(steps);
							overlay.destroy()	
							cm.destroy();
						}),
						cancel('cancel', [], () => {
							cm.destroy()
							overlay.destroy()	
						})
					])
				], 'margins'));
				
				moveContextMenuAway(cm, selectedElement, event)
			});
		}
	}
}


export function initPaddingContextMenuCallback(command, overlay, selector) {
	
	if (selector == undefined) {
		selector = defaultSizingSelector();
	}
	
	

	return function(event, cm) {
		const selectedElement = hasLastSelected(selector(), true);
		if (selectedElement) {
			cm.addControl(event, "/public/behaviours/containers/size/padding.svg", 'Padding', () => {
				cm.clear();
				const initialPadding = parseInfo(selectedElement)['padding'].split(" ").map(x => parseFloat(x));
				const htmlElement = cm.get(event);
				const adlElement = command.getADLDom(selectedElement.getAttribute("id"))
				const style = parseStyle(adlElement.getAttribute("style"));
				const bbox = getElementPageBBox(selectedElement);
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-padding-left', 'Left', style, true, true, bbox.x + initialPadding[3], bbox.y + bbox.height / 2, initialPadding[3]),
					addNumericControl(overlay, '--kite9-padding-right', 'Right', style, true, false, bbox.x + bbox.width - initialPadding[1], bbox.y + bbox.height / 2, initialPadding[1]),
					addNumericControl(overlay, '--kite9-padding-top', 'Top', style, false, true, bbox.x + bbox.width / 2 ,bbox.y + initialPadding[0], initialPadding[0]),
					addNumericControl(overlay, '--kite9-padding-bottom', 'Bottom', style, false, false, bbox.x + bbox.width / 2, bbox.y + bbox.height - initialPadding[2], initialPadding[2]),
				]
				
				
				htmlElement.appendChild(form([
					...numericControls,
					inlineButtons([
						ok('ok', {}, (e) => {
							const selectedElements = hasLastSelected(selector());
							const fields = ['left', 'right', 'top', 'bottom']
							const values = formValues('padding');
							const steps = Array.from(selectedElements)
								.flatMap(e => createStyleSteps(fields, e, values, '--kite9-padding-', style));
							command.pushAllAndPerform(steps);
							overlay.destroy()	
							cm.destroy();
						}),
						cancel('cancel', [], () => {
							cm.destroy()
							overlay.destroy()	
						})
					])
				], 'padding'));
				
				moveContextMenuAway(cm, selectedElement, event)
			});
		}
	}
}
