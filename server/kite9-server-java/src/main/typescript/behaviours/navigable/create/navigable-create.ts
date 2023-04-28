import { form, ok, text, hidden, formValues, formObject, select, div, fieldset, img, largeIcon, p } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { Metadata } from "../../../classes/metadata/metadata.js";
import { ContextMenuCallback } from "../../../classes/context-menu/context-menu.js";
import { Selector } from "../../../bundles/types.js";
import { onlyLastSelected } from "../../../bundles/api.js";

const LOADING = '/public/behaviours/rest/loading.svg';

export type TemplateSource = () => Promise<unknown>

export function initTemplateSource(metadata: Metadata): TemplateSource {

	return async () => {
		const uri = metadata.get("templates") as string;

		const response = await fetch(uri, {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Accept': 'application/json'
			}
		});
		return await response.json();
	}
}



export function initNewDocumentContextMenuCallback(
	metadata: Metadata, 
	templateSource: TemplateSource, selector: Selector = undefined) 
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
    
    templateSource()
      .then(json => {
        json['documents'].forEach(d => {
        
          if (templateField.value == '') {
            templateField.value = d._links.self.href;
          }
        
          const img = largeIcon('x', d.title, d.icon, () => {
            templateField.value = d._links.self.href;
            if (selected) {
              selected.classList.remove("selected");
            }
            img.classList.add("selected");
            selected = img;
          });
        
          into.appendChild(img);
        });
        into.removeChild(spinner);
      })
      .catch(e => {
        alert("Couldn't collect templates: ."+ e);
      });    
  }

  function createDiv(id: string) {
    return div({
      'id': id,
      'style': 'overflow: scroll; display: block; height: 140px; '
    }, []);
  }

	/**
	 * Provides a "New Document" option for the context menu
	 */
  return function(event, cm) {

    const e = onlyLastSelected(selector());

    function createNewDocument() {
      if (formObject().checkValidity()) {
        const values = formValues('newDocumentForm');
        const newUri = fullUri(e.getAttribute('subject-uri') + "/" + values['fileName'] + "." + values['format']);
        cm.destroy();
        window.location.href = newUri + "?templateUri=" + fullUri(values.templateUri);
      }
    }

    if (e) {
      cm.addControl(event, "/public/behaviours/navigable/create/add.svg", "New Document",
        function() {
          cm.clear();
          const templateUri = text('Template Uri', undefined, { 'required': true });
          const templates = createDiv('templates');
    
          // add the form to the contextMenu
          const formArea = cm.get(event);
          formArea.style.width = '500px';
          formArea.appendChild(
            fieldset('New Document', [
              text('File Name', undefined, { 'required': true, 'pattern': '[A-Za-z0-9_-]+', title: 'Please use alphanumeric characters, _ or - and no spaces' }),
              templateUri,
              fieldset('Templates', [ templates, ], { style: 'padding: 2px; ' }),
              p("Add new custom templates by placing diagrams in "+metadata.get("templates"),  {style: 'font-weight: lighter; '}),
              select('Format', 'png', {}, ['svg', 'png', 'adl']),
              hidden('type', 'NewDocument'),
              ok('ok', {}, () => createNewDocument())
            ]));
    
          // populate templates
          loadTemplates(templates, templateUri.children[1] as HTMLInputElement);
     
        });
    }
  }
}

