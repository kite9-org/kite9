import { getAffordances } from '../../../bundles/api.js';
import { icon } from '../../../bundles/form.js';
import { addNamedEventListener } from '../../../bundles/monika.js';
import { isAutoconnectNew } from '../linkable.js';
export const AUTOCONNECT_ALT_ON = 'autoconnect-alt-on', AUTOCONNECT_ALT_OFF = 'autoconnect-alt-off';
export var AutoConnectMode;
(function (AutoConnectMode) {
    AutoConnectMode[AutoConnectMode["OFF"] = 0] = "OFF";
    AutoConnectMode[AutoConnectMode["NEW"] = 1] = "NEW";
    AutoConnectMode[AutoConnectMode["ON"] = 2] = "ON";
})(AutoConnectMode || (AutoConnectMode = {}));
function modeText(a) {
    switch (a) {
        case AutoConnectMode.OFF: return "Auto Connect: Off ⇧";
        case AutoConnectMode.NEW: return "Auto Connect: New Only ⇧";
        case AutoConnectMode.ON: return "Auto Connect: Always ⇧";
    }
}
function nextMode(a) {
    switch (a) {
        case AutoConnectMode.OFF: return AutoConnectMode.NEW;
        case AutoConnectMode.NEW: return AutoConnectMode.ON;
        case AutoConnectMode.ON: return AutoConnectMode.OFF;
    }
}
function getStyle(a) {
    switch (a) {
        case AutoConnectMode.OFF: return "opacity: .5; ";
        case AutoConnectMode.NEW: return "background-color: #b7c0fe66;";
        case AutoConnectMode.ON: return "background-color: #fd987066;";
    }
}
let mode = AutoConnectMode.OFF;
export function initAutoConnectTemplateSelector(alignTemplateUriCallback, linkTemplateUriCallback) {
    return function (element) {
        const ac = getAffordances(element).includes("autoconnect");
        switch (mode) {
            case AutoConnectMode.NEW:
                if (isAutoconnectNew(element) && ac) {
                    return linkTemplateUriCallback();
                }
                else {
                    return alignTemplateUriCallback();
                }
            case AutoConnectMode.OFF:
                return null;
            case AutoConnectMode.ON:
                if (ac) {
                    return linkTemplateUriCallback();
                }
                else {
                    return alignTemplateUriCallback();
                }
        }
        //		const alignLink = (element != null) && (!);
    };
}
export function initAutoConnectInstrumentationCallback(linker, dragger) {
    let acIcon = null;
    let altMode = null;
    function updateMode() {
        acIcon.setAttribute("aria-label", modeText(mode));
        acIcon.children[0].setAttribute("style", getStyle(mode));
        dragger.last_drag_replay();
    }
    function toggleState() {
        mode = nextMode(mode);
        updateMode();
    }
    function altOn() {
        if (altMode == null) {
            altMode = mode;
            switch (mode) {
                case AutoConnectMode.ON:
                case AutoConnectMode.NEW:
                    mode = AutoConnectMode.OFF;
                    linker.removeDrawingLinks();
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
    addNamedEventListener(document, "keydown", AUTOCONNECT_ALT_ON, (e) => { if (e.key == 'Shift')
        altOn(); });
    addNamedEventListener(document, "keyup", AUTOCONNECT_ALT_OFF, (e) => { if (e.key == 'Shift')
        altOff(); });
    return function (nav) {
        if (acIcon == null) {
            acIcon = icon('_autoconnect-toggle', "Toggle AutoConnect", "/public/behaviours/links/autoconnect/autoconnect.svg", toggleState);
            acIcon.classList.remove("hint--bottom");
            acIcon.classList.add("hint--bottom-left");
            nav.appendChild(acIcon);
            toggleState();
        }
    };
}
export function getAutoConnectMode() {
    return mode;
}
