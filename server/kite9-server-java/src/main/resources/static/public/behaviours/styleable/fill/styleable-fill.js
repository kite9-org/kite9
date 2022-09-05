import { hasLastSelected } from '/public/bundles/api.js'
import { parseStyle, formatStyle } from '/public/bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues, fieldset, colour, numeric } from '/public/bundles/form.js'
import { getMainSvg, canRenderClientSide } from '/public/bundles/screen.js';

const rgba2hex = (rgba) => `#${rgba.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+\.{0,1}\d*))?\)$/).slice(1).map((n, i) => (i === 3 ? Math.round(parseFloat(n) * 255) : parseFloat(n)).toString(16).padStart(2, '0').replace('NaN', '')).join('')}`

function getColour(propName, e) {
	if (canRenderClientSide()) {
		return rgba2hex(e.computedStyleMap().get(propName).toString());
	} else {
		return '';
	}
}

function getOpacity(propName, e) {
	if (canRenderClientSide()) {
		return e.computedStyleMap().get(propName).toString();
	} else {
		return '';
	}
}

function getIntegerPx(propName, e) {
	if (canRenderClientSide()) {
		return window['kite9-visualization-js'].getCssStyleDoubleProperty(propName, e)
	} else {
		return '1';
	}
}


export function initFillContextMenuCallback(command, overlay, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~=fill].selected");
		}
	}
	
	function extractFormValues(formName) {
		const asArray = Object.entries(formValues(formName));
		const filtered = asArray.filter(([key, value]) => !((value == '') || (key=='ok') || (key=='cancel')));
		return Object.fromEntries(filtered);
	}
	
	function createStyleSteps(e, oldValues, newValues) {
		function styleEqual(a, b) {
			if (((!a) || (a.length == 0)) && ((!b) || (b.length == 0))) {
				return true;
			} else {
				return a == b;
			}
		}
		
		const out = Object.keys(newValues)
			.filter(s => !styleEqual(oldValues[s],newValues[s]))
			.map(f =>  { return {
			fragmentId: e.getAttribute("id"),
			type: 'ReplaceStyle',
			name: f,
			to: newValues[f],
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

	return function(event, cm) {
		
		const selectedElement = hasLastSelected(selector(), true);

		if (selectedElement) {
			
			cm.addControl(event, "/public/behaviours/styleable/fill/paintbrush.svg", 'Fill & Stroke', () => {
				cm.clear();
				overlay.ensureOverlay();
				const selectedElements = Array.from(hasLastSelected(selector()));
				const originalStyleMap = createStyleMap(command, selectedElements);
				const style = originalStyleMap[selectedElement.getAttribute("id")];
				const originalSvgStyle = selectedElement.getAttribute("style")
				const svgStyle = parseStyle(originalSvgStyle);
				
				const fill = colour("fill", style['fill']);
				const stroke = colour("stroke", style['stroke']);
				const fillOpacity = numeric('fill-opacity', style['fill-opacity'], getOpacity('fill-opacity', selectedElement), {min: 0, max: 1, step: 0.1});
				const strokeOpacity = numeric('stroke-opacity', style['stroke-opacity'], getOpacity('stroke-opacity', selectedElement), {min: 0, max: 1, step: 0.1});
				const strokeWidth = numeric('stroke-width', style['stroke-width'], getIntegerPx('stroke-width', selectedElement), {min: 0});
				
				const fillControls = [
					fill,
					fillOpacity
				];
				const strokeControls = [
					stroke,
					strokeOpacity,
					strokeWidth
				];
				
				const theForm = form([				
					fieldset("Fill", fillControls),
					fieldset("Stroke", strokeControls),
					inlineButtons([
						ok('ok', {}, (e) => {
							const values = extractFormValues('fill');
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
				], 'fill');
				
				const changeEvent = e=> {
					const values = extractFormValues('fill');
					const newStyle = {...svgStyle, ...values};
					const formatted = formatStyle(newStyle);
					if (newStyle['fill']) {
						selectedElement.classList.remove('selected');
						selectedElement.classList.remove('mouseover');
					}

					selectedElement.setAttribute("style", formatted);
					
					// update swatches
					fill.children[1].children[0].value = getColour('fill', selectedElement);
					stroke.children[1].children[0].value = getColour('stroke', selectedElement);
					
				};
				
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