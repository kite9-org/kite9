import { getMainSvg } from '../../../bundles/screen.js';
import { onlyLastSelected } from '../../../bundles/api.js';
export function initFocusContextMenuCallback(command, selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=focus]"));
        };
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        const e = onlyLastSelected(selector());
        if (e) {
            contextMenu.addControl(event, "/public/behaviours/navigable/focus/focus.svg", "Focus On", function () {
                contextMenu.destroy();
                let url = e.getAttribute("id");
                const projection = url.indexOf('{');
                if (projection != -1) {
                    url = url.substring(0, projection);
                }
                command.get(url);
            });
        }
    };
}
let state = {};
export function initFocusMetadataCallback() {
    return function (metadata) {
        const newUrl = metadata.self;
        if (state.page != newUrl) {
            const title = metadata.title;
            if (title) {
                document.title = title;
            }
            const newState = {
                title: title,
                page: newUrl
            };
            if (state.page == undefined) {
                // this is done for the initial page load
                history.replaceState(newState, title, newUrl);
            }
            else {
                history.pushState(newState, title, newUrl);
            }
            state = newState;
        }
    };
}
export function initFocus(transition) {
    function popState(event) {
        state = event.state;
        document.title = event.state.title;
        command.get(event.state.page);
    }
    window.removeEventListener('popstate', popState);
    window.addEventListener('popstate', popState);
}
