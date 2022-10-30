import { getContainingDiagram, hasLastSelected, getParentElement, getDocumentParam } from '../../../bundles/api.js'
import { getMainSvg, currentTarget } from '../../../bundles/screen.js'
import { getAlignElementsAndDirections } from '../linkable.js'
import { Command } from '../../../classes/command/command.js';
import { Linker, LinkerCallback } from '../../../classes/linker/linker.js';
import { Finder, Selector } from '../../../bundles/types.js';
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { initPaletteFinder } from '../../palettes/menu/palettes-menu.js';


function defaultLinkableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette~=link]");
}

export function getLinkTemplateUri() {
	return getDocumentParam('link-template-uri');
}

export function initLinkContextMenuCallback(
	linker: Linker,
	selector: Selector = undefined,
	linkFinder: Finder = undefined): ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='connect'].selected"));
		}
	}

	if (linkFinder == undefined) {
		linkFinder = initPaletteFinder();
	}

	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {

		const elements = hasLastSelected(selector());

		if (elements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/links/link/link.svg",
				"Draw Link", e => {
					contextMenu.destroy();
					linker.start(elements, linkFinder(getLinkTemplateUri()));
				});
		}
	};
}

/**
 * This is called when the user finishes doing a link operation, 
 * which will end up creating the link.
 */
export function initLinkLinkerCallback(command: Command): LinkerCallback {

	return function(linker, evt, perform = true) {
		const linkTarget = linker.getLinkTarget(currentTarget(evt));

		if (linkTarget == null) {
			linker.removeDrawingLinks();
		} else {
			linker.move(evt);
			const diagramId = getContainingDiagram(linkTarget).getAttribute("id");
			const linkTargetId = linkTarget.getAttribute("id");
			linker.get().forEach(e => {
				const fromId = e.getAttribute("temp-from");
				const aligns = getAlignElementsAndDirections(fromId, linkTargetId);
				const linkId = e.getAttribute("id");

				command.push({
					type: "InsertUrlWithChanges",
					fragmentId: diagramId,
					uriStr: getLinkTemplateUri(),
					xpathToValue: {
						"*[local-name()='from']/@reference": fromId,
						"*[local-name()='to']/@reference": linkTargetId
					},
					newId: linkId
				});

				/*
				 * If there is an align element, remove it and set the draw direction.
				 */

				if (aligns.length == 1) {
					const { element, direction } = aligns[0];
					const id = element.getAttribute("id");
					command.push({
						type: 'Delete',
						fragmentId: getParentElement(element).getAttribute('id'),
						base64Element: command.getAdl(id)
					});
					command.push({
						type: 'ReplaceAttr',
						fragmentId: linkId,
						name: 'drawDirection',
						to: direction
					})
				}

			});

			if (perform) {
				command.perform();
			}
			linker.clear();
		}
	};

}

