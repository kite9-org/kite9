import { form, ok, text, hidden, formValues, formObject, select, div, fieldset, img, namedIcon, p, icon } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { Metadata } from "../../../classes/metadata/metadata.js";
import { ContextMenuCallback } from "../../../classes/context-menu/context-menu.js";
import { Selector } from "../../../bundles/types.js";
import { onlyLastSelected } from "../../../bundles/api.js";

const LOADING = '/public/behaviours/rest/loading.svg';

export type Template = {
	url: string,
	icon: string,
	title: string
}

export type TemplateSource = () => Promise<Template[]>

export function initTemplateSource(metadata: Metadata): TemplateSource {

	const path = metadata.get("templatepath") as string;
	const uris = metadata.get("templates") as string[];

	async function createTemplateListPromise(): Promise<Template[]> {
		return new Promise((resolve, _reject) => {
			const out: Template[] = uris.map(u => {
				return {
					url: u,
					icon: "/public/behaviours/navigable/create/template.svg",
					title: u.substring(u.lastIndexOf("/") + 1)
				}
			});


			resolve(out);
		});
	}

	async function createTemplatePathPromise(): Promise<Template[]> {
		const response = fetch(path, {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Accept': 'application/json'
			}
		})

		return response.then(r => r.json())
			.then(c => c['contents'].map(d => {
				return {
					url: d._links.self.href,
					title: d.title,
					icon: d.icon
				}
			}))
	}

	return async () => {
		const promises = [];

		if (uris) {
			promises.push(createTemplateListPromise());
		}

		if (path) {
			promises.push(createTemplatePathPromise());
		}

		return Promise.all(promises).then(r => {
			return r.flat();
		});
	}

}



export function initNewDocumentContextMenuCallback(
	metadata: Metadata,
	templateSource: TemplateSource,
	selector: Selector = undefined)
	: ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='NewDocument']"))
		}
	}

	function fullUri(target: string) {
		const currentUri = new URL(target, metadata.get("self") as string);
		return currentUri;
	}

	function loadTemplates(into: HTMLElement, templateField: HTMLInputElement) {
		let selected = null;
		const spinner = img('status', LOADING, { width: '80px' });
		into.appendChild(spinner);
		
		function select(d: Template, e: HTMLElement) {
			templateField.value = fullUri(d.url).toString();
			if (selected) {
				selected.classList.remove("selected");
			}
			e.classList.add("selected");
			selected = e;
		}

		templateSource()
			.then(templates => {
				templates.forEach(d => {

					if (templateField.value == '') {
						templateField.value = d.url;
					}
					
					const shortTitle = d.title.substring(d.title.lastIndexOf("/"));

					const img = namedIcon('x', d.title, shortTitle, d.icon, () => {
						select(d, img);
					}); 
					
					// select the first template
					if (selected==null) {
						select(d, img)
					}

					into.appendChild(img);
				});
				into.removeChild(spinner);
			})
			.catch(e => {
				alert("Couldn't collect templates: ." + e);
			});
	}

	function createDiv(id: string) {
		return div({
			'id': id,
			'style': 'overflow: scroll;  height: 210px; ',
			'class': 'icons wide'
		}, []);
	}

	/**
	 * Provides a "New Document" option for the context menu
	 */
	return function(event, cm) {

		const e = onlyLastSelected(selector());

		function createNewDocument() {
			if (formObject().checkValidity()) {
				const values = formValues();
				const newUri = fullUri(e.getAttribute('id') + "/" + values['fileName'] + "." + values['format']);
				cm.destroy();
				window.location.href = newUri + "?templateUri=" + fullUri(values.templateUri);
			}
		}

		if (e && (metadata.get("templates") || metadata.get("templatepath"))) {
			cm.addControl(event, "/public/behaviours/navigable/create/add.svg", "New Diagram",
				function() {
					cm.clear();
					const templateUri = text('Template Uri', undefined, { 'required': true });
					const templates = createDiv('templates');

					// add the form to the contextMenu
					const formArea = cm.get(event);
					const defaultFormat = metadata.get("defaultformat") as string
					const formatOptions = metadata.get("allowedformats") as string[]
					formArea.appendChild(
						fieldset('New Document', [
							text('File Name', 'diagram-name', { 'required': true, 'pattern': '[A-Za-z0-9_-]+', title: 'Please use alphanumeric characters, _ or - and no spaces' }),
							templateUri,
							fieldset('Templates', [templates], { style: 'padding: 2px; ' }),
							select('Format', defaultFormat, {}, formatOptions),
							hidden('type', 'NewDocument'),
							ok('ok', {}, () => createNewDocument())
						]));

					// populate templates
					loadTemplates(templates, templateUri.children[1] as HTMLInputElement);

				});
		}
	}
}

