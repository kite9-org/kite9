import { hasLastSelected } from '../../../bundles/api.js';
import { getMainSvg } from '../../../bundles/screen.js';
/**
 * Provides the palette-menu option for the context menu on the main diagram.
 */
export function initPaletteContextMenuCallback(palette, selector = undefined) {
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id].selected"));
        };
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        const selectedElements = hasLastSelected(selector());
        if (selectedElements.length > 0) {
            contextMenu.addControl(event, "/public/behaviours/palettes/menu/open-palette.svg", "Open Palette", function () {
                contextMenu.destroy();
                palette.open(event);
            });
        }
    };
}
/**
 * Given a URI, returns the element itself, which we can use as the template
 */
export function initPaletteFinder() {
    return function (uri) {
        const options = Array.from(document.querySelectorAll("div.palette-item"))
            .filter(pDiv => uri.startsWith(pDiv.getAttribute("k9-palette-uri")))
            .map(pDiv => {
            const paletteId = pDiv.getAttribute("id");
            const elementId = uri.substring(uri.lastIndexOf("#") + 1) + paletteId;
            return pDiv.querySelector('#' + elementId);
        });
        return options[0];
    };
}
/**
 * Allows elements on the palette to open up a context menu when clicked.
 */
export function initMenuPaletteCallback(paletteContextMenu, menuChoiceSelector = undefined) {
    if (menuChoiceSelector == undefined) {
        menuChoiceSelector = function (palettePanel) {
            return Array.from(palettePanel.querySelectorAll("[id]"));
        };
    }
    return function (palette, palettePanel) {
        function click(event) {
            paletteContextMenu.destroy();
            paletteContextMenu.handle(event);
            event.stopPropagation();
        }
        menuChoiceSelector(palettePanel).forEach(function (v) {
            v.removeEventListener("click", click);
            v.addEventListener("click", click);
        });
    };
}
