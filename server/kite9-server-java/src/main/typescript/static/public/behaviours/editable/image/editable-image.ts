import { hasLastSelected, onlyLastSelected } from '../../../bundles/api.js'
import { getMainSvg } from '../../../bundles/screen.js'
import { icon, form, text, p, inlineButtons, ok, cancel, formValues, img, fieldset, div } from '../../../bundles/form.js'
import { Command } from '../../../classes/command/command.js';
import { Metadata } from '../../../classes/metadata/metadata.js';
import { Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';

const LOADING = '/public/behaviours/editable/image/loading.svg';
const SUCCESS = '/public/behaviours/editable/image/success.svg';
const FAIL = '/public/behaviours/editable/image/fail.svg';

export function initEditableImageContextMenuCallback(
	command: Command, 
	metadata: Metadata, 
	selector: Selector = null) : ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=image].selected"));
		}
	}

	function setAttribute(element, key, value, newValue) {
		if ((value) && (newValue) && (value != newValue)) {
			command.push({
				type: 'ReplaceAttr',
				fragmentId: element.getAttribute("id"),
				name: key,
				from: value,
				to: newValue
			});
		}
	}

	function uploadImage(blob : string, path : string, spinner : Element) {
		const fd = new FormData();
		fd.append('file', blob);

		fetch(path, {
			method: 'POST',
			body: fd
		}).catch(() => {
			spinner.setAttribute("src", FAIL);
		}).then(() => {
			spinner.setAttribute("src", SUCCESS);
		})
	}

	function canUpload() {
		return metadata.get('uploads');
	}

	function loadImages(into, onClick) {
		const spinner = img('pasteStatus', LOADING, { width: '80px' });
		into.appendChild(spinner);
		const path = metadata.get('uploads') + '?format=json';
		fetch(path).catch(() => {
			spinner.setAttribute("src", FAIL);
		}).then(r => {
			into.removeChild(spinner);
			return r ? r.json() : undefined;
		}).then(json => {
			if (json) {
				json.documents.forEach(d => {
					const img = icon('x', d.title, d.icon, onClick);
					into.appendChild(img);
				});
				console.log(json);
			}
		});
	}

	function createP(id : string) {
		return p('', {
			'contentEditable': 'true',
			'id': id,
			'style': 'height: 100px; padding: 12px; margin: 5px; user-select: auto; -webkit-user-select: auto; display: block; ',
			'class': 'hint--bottom hint--bounce',
			'aria-label': 'Paste Image Here To Upload'
		});
	}

	function createDiv() {
		return div({
			'style': 'overflow: scroll; display: block; height: 140px; '
		}, []);
	}

	return function(event: Event, contextMenu: ContextMenu) {

		const elements = hasLastSelected(selector());

		if (elements.length > 0) {
			const last = onlyLastSelected(elements);
			contextMenu.addControl(event, "/public/behaviours/editable/image/edit.svg", 'Edit Image', () => {
				const href = last.getAttribute("href");

				contextMenu.clear();
				const htmlElement = contextMenu.get(event);

				const hrefField = text('Image URI', href);
				let pasteField = createP('pasteField');
				let existingField = null;

				if (canUpload()) {
					let pasteImage = null;
					let pasteData = null;
					let pastePath = null;
					let pasteStatus = null;

					pasteField.addEventListener('paste', function(event) {
						const items = event.clipboardData.items;
						console.log(JSON.stringify(items)); 
						// find pasted image among pasted items
						let blob = null;
						for (let i = 0; i < items.length; i++) {
							if (items[i].type.indexOf("image") === 0) {
								blob = items[i].getAsFile();
							}
						}

						// load image if there is a pasted image
						if (blob !== null) {
							const reader = new FileReader();
							reader.onload = function(event) {
								console.log(event.target.result); // data url!

								if (pasteImage != null) {
									pasteField.removeChild(pasteImage);
									pasteField.removeChild(pasteStatus);
								}
								pasteData = event.target.result;
								pasteImage = img('pastedImage', pasteData, { width: '80px' });
								pasteField.appendChild(pasteImage);
								pasteField.appendChild(pasteStatus);
							};

							pasteStatus = img('pasteStatus', LOADING, { width: '80px' });
							pastePath = metadata.get('uploads') + "/" + blob.name;
							reader.readAsDataURL(blob);
							(hrefField.children[1] as HTMLInputElement).value = pastePath;

							uploadImage(blob, pastePath, pasteStatus);
						}

						event.stopPropagation();
						event.preventDefault();
					});

					existingField = createDiv();
					loadImages(existingField, function(event) {
						const selected = event.currentTarget.children[0];
						if (pasteImage != null) {
							pasteField.removeChild(pasteImage);
							pasteField.removeChild(pasteStatus);
						}
						pastePath = selected.getAttribute("src");
						pasteImage = img('pastedImage', pastePath, { width: '80px' });
						pasteField.appendChild(pasteImage);
						(hrefField.children[1] as HTMLInputElement).value = pastePath;
					});

				} else {
					pasteField = p("You can't upload here");
					existingField = p('No existing images');
				}


				htmlElement.appendChild(fieldset('Image Properties', [
					hrefField,
					fieldset('New Image', [pasteField], { style: 'padding: 2px;' }),
					fieldset('Existing Images', [existingField], { style: 'padding: 2px; ' }),
					inlineButtons([
						ok('ok', {}, () => {
							const newValues = formValues();
							Array.from(elements).forEach(e => setAttribute(e, 'href', href, newValues.imageURI));
							command.perform();
							contextMenu.destroy();
						}),
						cancel('cancel', [], () => contextMenu.destroy())
					])
				]));
			});
		}
	};
}