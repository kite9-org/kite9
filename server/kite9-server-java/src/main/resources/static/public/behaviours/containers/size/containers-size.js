import { hasLastSelected, isRectangular, parseStyle } from '/public/bundles/api.js'
import { textarea, form, ok, cancel, inlineButtons, formValues } from '/public/bundles/form.js'
import { getMainSvg, getElementPageBBox, getElementHTMLBBox } from '/public/bundles/screen.js';
import { numeric } from '/public/bundles/form.js'

	

function addNumericControl(overlay, cssAttribute, name, element, style, horiz, inverse, sx, sy) {
	var val = style[cssAttribute];
	var length = 0;
	if ((val) && val.endsWith("px")) {
		val = val.substring(0, val.length-2);
		length = parseFloat(val);
	}
	
	const box = numeric(name, val, {"min" : "0", "placeholder": "default"});
	const input = box.children[1];
	
	const sizer = overlay.createSizingArrow(element, sx, sy, length, horiz, inverse, (v) => input.value = v);

	// event for when the size is changed in the context menu
	input.addEventListener("input", (e) => {
		sizer(Math.max(input.value,0));
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

export function initMarginContextMenuCallback(command, overlay, selector) {
	
	if (selector == undefined) {
		selector = defaultSizingSelector();
	}

	return function(event, cm) {
		const selectedElement = hasLastSelected(selector(), true);
		if (selectedElement) {
			cm.addControl(event, "/public/behaviours/containers/size/margins.svg", 'Margins', () => {
				cm.clear();
				var htmlElement = cm.get(event);
				const style = parseStyle(selectedElement);
				const bbox = getElementPageBBox(selectedElement);
				
				const numericControls = [
					addNumericControl(overlay, '--kite9-margin-left', 'Left', selectedElement, style, true, true, bbox.x, bbox.y + bbox.height / 2),
					addNumericControl(overlay, '--kite9-margin-right', 'Right', selectedElement, style, true, false, bbox.x + bbox.width, bbox.y + bbox.height / 2),
					addNumericControl(overlay, '--kite9-margin-top', 'Top', selectedElement, style, false, true, bbox.x + bbox.width / 2 ,bbox.y),
					addNumericControl(overlay, '--kite9-margin-bottom', 'Bottom', selectedElement, style, false, false, bbox.x + bbox.width / 2, bbox.y + bbox.height),
				]
				
				htmlElement.appendChild(form([
					...numericControls,
					inlineButtons([
						ok('ok', {}, (e) => {
							/*const values = formValues('editText');
							const steps = Array.from(selectedElements).map(e => createEditStep(e, values.enterText, defaultText));
							command.pushAllAndPerform(steps);*/
							overlay.destroy()	
							cm.destroy();
						}),
						cancel('cancel', [], () => {
							cm.destroy()
							overlay.destroy()	
						})
					])
				], 'editText'));
				
				moveContextMenuAway(cm, selectedElement, event)
			});
		}
	}
}
