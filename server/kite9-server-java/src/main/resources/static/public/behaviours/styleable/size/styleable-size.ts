import { hasLastSelected, isRectangular, parseInfo } from '../../../bundles/api.js'
import { parseStyle } from '../../../bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues } from '../../../bundles/form.js'
import { getMainSvg, getElementPageBBox, getElementHTMLBBox, canRenderClientSide } from '../../../bundles/screen.js';
import { numeric, fieldset } from '../../../bundles/form.js'
import { addNumericControl, moveContextMenuAway } from '/public/behaviours/styleable/styleable.js'


export function containerSizingSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=size].selected"))
			.filter(e => isRectangular(e));
}

export const marginsIcon = "/public/behaviours/styleable/size/margins.svg";

export function initMarginsBuildControls() {
	return function(selectedElement, style, overlay, cm, event) {
		const margins = parseInfo(selectedElement)['margin'].split(" ").map(x => parseFloat(x));
		const bbox = getElementPageBBox(selectedElement);
		
		const innerMove = overlay.createSizingRect(bbox.x, bbox.y, bbox.width, bbox.height, 
			0, 0, 0, 0);
		const outerMove = overlay.createSizingRect(bbox.x, bbox.y, bbox.width, bbox.height,
			margins[0], margins[1], margins[2], margins[3]);
		
		const numericControls = [
			addNumericControl(overlay, '--kite9-margin-top', style, false, true, bbox.x + bbox.width / 2 ,bbox.y, margins[0], outerMove[0]),
			addNumericControl(overlay, '--kite9-margin-right', style, true, false, bbox.x + bbox.width, bbox.y + bbox.height / 2, margins[1], outerMove[1]),
			addNumericControl(overlay, '--kite9-margin-bottom', style, false, false, bbox.x + bbox.width / 2, bbox.y + bbox.height, margins[2], outerMove[2]),
			addNumericControl(overlay, '--kite9-margin-left', style, true, true, bbox.x, bbox.y + bbox.height / 2, margins[3], outerMove[3])
		]
		
		moveContextMenuAway(cm, selectedElement, event)
		return [ fieldset("Margins", numericControls) ];
	}
}

export const paddingIcon = "/public/behaviours/styleable/size/padding.svg";


export function initPaddingBuildControls() {
	return function(selectedElement, style, overlay, cm, event) {
		const padding = parseInfo(selectedElement)['padding'].split(" ").map(x => parseFloat(x));
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
			addNumericControl(overlay, '--kite9-padding-top', style, false, true, ibox.x + ibox.width / 2 ,ibox.y, padding[0], outerMove[0]),
			addNumericControl(overlay, '--kite9-padding-right', style, true, false, ibox.x + ibox.width, ibox.y + ibox.height / 2, padding[1], outerMove[1]),
			addNumericControl(overlay, '--kite9-padding-bottom', style, false, false, ibox.x + ibox.width / 2, ibox.y + ibox.height, padding[2], outerMove[2]),
			addNumericControl(overlay, '--kite9-padding-left', style, true, true, ibox.x, ibox.y + ibox.height / 2, padding[3], outerMove[3])
		]

		moveContextMenuAway(cm, selectedElement, event)

		return [ fieldset("Padding", numericControls) ];
	}
}

export const minSizeIcon = "/public/behaviours/styleable/size/size.svg";


export function initMinSizeBuildControls() {
	return function(selectedElement, style, overlay, cm, event) {
		const minSize = parseInfo(selectedElement)['min-size'].split(" ").map(x => parseFloat(x));
		const htmlElement = cm.get(event);
		const bbox = getElementPageBBox(selectedElement);
		
		const numericControls = [
			addNumericControl(overlay, '--kite9-min-width', style, true, false, bbox.x,bbox.y, minSize[0], () => {}),
			addNumericControl(overlay, '--kite9-min-height', style, false, false, bbox.x, bbox.y, minSize[1],  () => {}),
		]
				
		moveContextMenuAway(cm, selectedElement, event)
		return [ fieldset("Minimum Size", numericControls) ]
	}
}

export const sizingEnumProperties = {
	'--kite9-horizontal-sizing' : 'Horizontal',
	'--kite9-vertical-sizing' : 'Vertical'
}

export const sizingEnumValues = [ 'maximize', 'minimize' ];

export const sizingIcon = '/public/behaviours/styleable/size/sizing.svg';
