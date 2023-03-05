import { getAffordances } from '../../../bundles/api.js'
import { InstrumentationCallback } from '../../../classes/instrumentation/instrumentation.js';
import { icon } from '../../../bundles/form.js';
import { addNamedEventListener } from '../../../bundles/monika.js';

export const AUTOCONNECT_ALT_ON = 'autoconnect-alt-on', 
	AUTOCONNECT_ALT_OFF = 'autoconnect-alt-off';

enum AutoConnectMode { OFF, NEW, ON }

function modeText(a: AutoConnectMode) {
	switch (a) {
		case AutoConnectMode.OFF: return "Auto Connect Off ⇧";
		case AutoConnectMode.NEW: return "New Elements Only ⇧";
		case AutoConnectMode.ON: return "Connect On Drag ⇧";
	}
}

function nextMode(a: AutoConnectMode): AutoConnectMode {
	switch (a) {
		case AutoConnectMode.OFF: return AutoConnectMode.NEW;
		case AutoConnectMode.NEW: return AutoConnectMode.ON;
		case AutoConnectMode.ON: return AutoConnectMode.OFF;
	}
}

function getStyle(a: AutoConnectMode): string {
	switch (a) {
		case AutoConnectMode.OFF: return "opacity: .5; ";
		case AutoConnectMode.NEW: return "background-color: #b7c0fe66;";
		case AutoConnectMode.ON: return "background-color: #fd987066;";
	}
}	

let mode : AutoConnectMode = AutoConnectMode.OFF;

export type UriCallback = () => string
export type TemplateSelector = (e: Element) => string

export function initAutoConnectTemplateSelector(
	alignTemplateUriCallback: UriCallback,
	linkTemplateUriCallback: UriCallback): TemplateSelector {

	return function(element: Element): string {
		const alignLink = (element != null) && (!getAffordances(element).includes("autoconnect"));
		return alignLink ? alignTemplateUriCallback() : linkTemplateUriCallback();
	}
}

export function initAutoConnectInstrumentationCallback() : InstrumentationCallback {

	let acIcon = null; 
	let altMode : AutoConnectMode = null;

	function updateMode() {
		acIcon.setAttribute("aria-label", modeText(mode));
		acIcon.children[0].setAttribute("style", getStyle(mode));
	}
	
	function toggleState() {
		mode = nextMode(mode);
		updateMode();
	}
	
	function altOn() {
		if (altMode == null) {
			altMode = mode;
			switch(mode) {
				case AutoConnectMode.ON:
				case AutoConnectMode.NEW:
					mode = AutoConnectMode.OFF;
					break;
				default:
					mode = AutoConnectMode.ON;
			}
			
			updateMode();
		}
	}
	
	function altOff() {
		mode = altMode;
		updateMode();
		altMode = null;
	}
	
	addNamedEventListener(document, "keydown", AUTOCONNECT_ALT_ON, (e: KeyboardEvent) => { if (e.key == 'Shift') altOn() });
	addNamedEventListener(document, "keyup", AUTOCONNECT_ALT_OFF,  (e: KeyboardEvent) => { if (e.key == 'Shift') altOff() })

	
	return function(nav) {
		if (acIcon == null) {
			acIcon = icon('_autoconnect-toggle', "Toggle AutoConnect", "/public/behaviours/links/autoconnect/autoconnect.svg", toggleState);
			acIcon.classList.remove("hint--bottom");
			acIcon.classList.add("hint--bottom-left")
			nav.appendChild(acIcon);
			toggleState();
		}
	}
}