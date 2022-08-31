import { hasLastSelected, isRectangular, parseInfo } from '/public/bundles/api.js'
import { parseStyle } from '/public/bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues } from '/public/bundles/form.js'
import { getMainSvg, getElementPageBBox, getElementHTMLBBox, canRenderClientSide } from '/public/bundles/screen.js';
import { numeric, fieldset } from '/public/bundles/form.js'


function addNumericControl(overlay, cssAttribute, name, style, horiz, inverse, sx, sy, inheritedLength, boxMove) {
	var val = style[cssAttribute];
	var length = inheritedLength;
	if ((val) && val.endsWith("px")) {
		val = val.substring(0, val.length-2);
		length = parseFloat(val);
	}
	
	const box = numeric(name, val, {"min" : "0", "placeholder": "inherited ("+inheritedLength.toFixed(1)+")"});
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
				const margins = parseInfo(selectedElement)['margin'].split(" ").map(x => parseFloat(x));
				const htmlElement = cm.get(event);
				const adlElement = command.getADLDom(selectedElement.getAttribute("id"))
				const style = parseStyle(adlElement.getAttribute("style"));
				const bbox = getElementPageBBox(selectedElement);
				
				const innerMove = overlay.createSizingRect(bbox.x, bbox.y, bbox.width, bbox.height, 
					0, 0, 0, 0);
				const outerMove = overlay.createSizingRect(bbox.x, bbox.y, bbox.width, bbox.height,
					margins[0], margins[1], margins[2], margins[3]);
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-margin-top', 'Top', style, false, true, bbox.x + bbox.width / 2 ,bbox.y, margins[0], outerMove[0]),
					addNumericControl(overlay, '--kite9-margin-right', 'Right', style, true, false, bbox.x + bbox.width, bbox.y + bbox.height / 2, margins[1], outerMove[1]),
					addNumericControl(overlay, '--kite9-margin-bottom', 'Bottom', style, false, false, bbox.x + bbox.width / 2, bbox.y + bbox.height, margins[2], outerMove[2]),
					addNumericControl(overlay, '--kite9-margin-left', 'Left', style, true, true, bbox.x, bbox.y + bbox.height / 2, margins[3], outerMove[3])
				]
				
				
				htmlElement.appendChild(form([
					fieldset("Margins", numericControls),
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
				const padding = parseInfo(selectedElement)['padding'].split(" ").map(x => parseFloat(x));
				const htmlElement = cm.get(event);
				const adlElement = command.getADLDom(selectedElement.getAttribute("id"))
				const style = parseStyle(adlElement.getAttribute("style"));
				const bbox = getElementPageBBox(selectedElement);
				const ibox = {
					x : bbox.x + padding[3],
					y : bbox.y + padding[0],
					width: bbox.width -padding[1] - padding[3],
					height: bbox.height - padding[0] - padding[2]
				}
				
				const innerMove = overlay.createSizingRect(ibox.x, ibox.y, ibox.width, ibox.height, 
					0, 0, 0, 0);
					
				const outerMove = overlay.createSizingRect(ibox.x, ibox.y, ibox.width, ibox.height,
					padding[0], padding[1], padding[2], padding[3]);
				
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-padding-top', 'Top', style, false, true, ibox.x + ibox.width / 2 ,ibox.y, padding[0], outerMove[0]),
					addNumericControl(overlay, '--kite9-padding-right', 'Right', style, true, false, ibox.x + ibox.width, ibox.y + ibox.height / 2, padding[1], outerMove[1]),
					addNumericControl(overlay, '--kite9-padding-bottom', 'Bottom', style, false, false, ibox.x + ibox.width / 2, ibox.y + ibox.height, padding[2], outerMove[2]),
					addNumericControl(overlay, '--kite9-padding-left', 'Left', style, true, true, ibox.x, ibox.y + ibox.height / 2, padding[3], outerMove[3])
				]
				
				htmlElement.appendChild(form([
					fieldset("Padding", numericControls),
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

export function initMinimumSizeContextMenuCallback(command, overlay, selector) {
	
	if (selector == undefined) {
		selector = defaultSizingSelector();
	}
	
	

	return function(event, cm) {
		const selectedElement = hasLastSelected(selector(), true);
		if (selectedElement) {
			cm.addControl(event, "/public/behaviours/containers/size/size.svg", 'Minimum Size', () => {
				cm.clear();
				const minSize = parseInfo(selectedElement)['min-size'].split(" ").map(x => parseFloat(x));
				const htmlElement = cm.get(event);
				const adlElement = command.getADLDom(selectedElement.getAttribute("id"))
				const style = parseStyle(adlElement.getAttribute("style"));
				const bbox = getElementPageBBox(selectedElement);
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-min-width', 'Width', style, true, false, bbox.x,bbox.y, minSize[0], () => {}),
					addNumericControl(overlay, '--kite9-min-height', 'Height', style, false, false, bbox.x, bbox.y, minSize[1],  () => {}),
				]
				
				htmlElement.appendChild(form([
					fieldset("Minimum Size", numericControls),
					inlineButtons([
						ok('ok', {}, (e) => {
							const selectedElements = hasLastSelected(selector());
							const fields = ['width', 'height']
							const values = formValues('min-size');
							const steps = Array.from(selectedElements)
								.flatMap(e => createStyleSteps(fields, e, values, '--kite9-min-', style));
							command.pushAllAndPerform(steps);
							overlay.destroy()	
							cm.destroy();
						}),
						cancel('cancel', [], () => {
							cm.destroy()
							overlay.destroy()	
						})
					])
				], 'min-size'));
				
				moveContextMenuAway(cm, selectedElement, event)
			});
		}
	}
}

