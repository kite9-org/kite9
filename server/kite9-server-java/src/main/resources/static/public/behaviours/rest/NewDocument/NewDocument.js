import { hasLastSelected } from "/public/bundles/api.js";
import { form, ok, cancel, text, hidden, formValues, formObject, select, div, fieldset, img, largeIcon, p } from '/public/bundles/form.js'

const LOADING = '/public/behaviours/rest/loading.svg';

const DEFAULT_TEMPLATES = {
  "documents": [
    {
      "title": "Basic Example",
      "icon": "/public/templates/basic/example.adl",
      "_links": {
        "self": {
          "href": "/public/templates/basic/example.adl"
        }
      }
    },
    {
      "title": "Risk-First Diagram",
      "icon": "/public/templates/risk-first/example.adl",
      "_links": {
        "self": {
          "href": "/public/templates/risk-first/example.adl"
        }
      }
    }, {
      "title": "Designer",
      "icon": "/public/templates/designer/example.adl",
      "_links": {
        "self": {
          "href": "/public/templates/designer/example.adl"
        }
      }
    }, {
      "title": "Flow Chart",
      "icon": "/public/templates/flowchart/example.adl",
      "_links": {
        "self": {
          "href": "/public/templates/flowchart/example.adl"
        }
      }
    }, {
      "title": "UML",
      "icon": "/public/templates/uml/example.adl",
      "_links": {
        "self": {
          "href": "/public/templates/uml/example.adl"
        }
      }
    }
  ]
};

export function initTemplateSource() {
  
  return (currentUri) => {
    const parts = currentUri.pathname.split("/");
    if (parts < 4) {
      return new Promise(() => DEFAULT_TEMPLATES);
    } else {
      const templatePath = "/" + parts[1]+"/" +parts[2]+"/"+parts[3]+"/.kite9/templates";
      currentUri.pathname = templatePath;
      return fetch(currentUri.href, {
         method: 'GET', 
         credentials: 'include', 
         headers: {
           'Accept': 'application/json'
        }
      })
      .then (response => {
        if (!response.ok) {
          return DEFAULT_TEMPLATES;
        } else {
          return response.json();
        }
      })
    }
  }
}

export function initNewDocumentContextMenuCallback(command, metadata, templateSource, selector) {

  if (selector == undefined) {
    selector = function() {
      return document.querySelectorAll("[id][k9-ui~='NewDocument']")
    }
  }
  
  function fullUri(target) {
     var currentUri = new URL(target, metadata.get("self"));
     return currentUri;
  }

  function loadTemplates(into, templateField, uri) {
    var selected = null;
    const spinner = img('status', LOADING, { width: '80px' });
    into.appendChild(spinner);
    
    templateSource(fullUri(uri))
      .then(json => {
        json.documents.forEach(d => {
        
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

  function createDiv(id) {
    return div({
      'style': 'overflow: scroll; display: block; height: 140px; '
    }, []);
  }

	/**
	 * Provides a delete option for the context menu
	 */
  return function(event, cm) {

    const e = hasLastSelected(selector(), true);

    function createNewDocument() {
      if (formObject('newDocumentForm').checkValidity()) {
        const values = formValues('newDocumentForm');
        var newUri = fullUri(e.getAttribute('subject-uri') + "/" + values['fileName'] + "." + values['format']);
        cm.destroy();
        window.location = newUri + "?templateUri=" + fullUri(values.templateUri);
      }
    }

    if (e) {
      cm.addControl(event, "/public/behaviours/rest/NewDocument/add.svg", "New Document",
        function(e2, selector) {
          cm.clear(event);
          const templateUri = text('Template Uri', undefined, { 'required': true });
          const templates = createDiv('templates');
    
          // add the form to the contextMenu
          const formArea = cm.get(event);
          formArea.style.width = '500px';
          formArea.appendChild(
            form([
              text('File Name', undefined, { 'required': true, 'pattern': '[A-Za-z0-9_-]+', title: 'Please use alphanumeric characters, _ or - and no spaces' }),
              templateUri,
              fieldset('Templates', [ templates, ], { style: 'padding: 2px; ' }),
              p("Add new custom templates by placing diagrams in .kite9/templates in the repo",  {style: 'font-weight: lighter; '}),
              select('Format', 'png', {}, ['svg', 'png', 'adl']),
              hidden('type', 'NewDocument'),
              ok('ok', {}, () => createNewDocument())
            ],
            'newDocumentForm'));
    
          // populate templates
          loadTemplates(templates, templateUri.children[1], e.getAttribute("subject-uri"));
     
        });
    }
  }
}

