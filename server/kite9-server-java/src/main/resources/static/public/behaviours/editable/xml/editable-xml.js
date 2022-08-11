import { Modal } from '/public/classes/modal/modal.js'
import { hasLastSelected, encodeADLElement } from '/public/bundles/api.js'
import { form, ok, cancel, inlineButtons, formFields } from '/public/bundles/form.js'
import { getMainSvg } from '/public/bundles/screen.js';
import { ensureCss } from '/public/bundles/ensure.js'
import '/webjars/codemirror/5.58.3/lib/codemirror.js';
import '/webjars/codemirror/5.58.3/mode/xml/xml.js';

export function initXMLContextMenuCallback(command, selector, xmlCollector) {

  const xmlModal = new Modal('_xml-editor');

  ensureCss('/webjars/codemirror/5.58.3/lib/codemirror.css');

  function createUpdateStep(e, text) {
    const id = e.getAttribute('id');
    return {
      "type": 'ReplaceXML',
      "fragmentId": id,
      "to": encodeADLElement(text),
      "from": command.getAdl(id)
    }
  }

  if (selector == undefined) {
    selector = function() {
      return getMainSvg().querySelectorAll("[id][k9-ui].selected");
    }
  }

  if (xmlCollector == undefined) {
    xmlCollector = function(e) {
      const id = e.getAttribute("id");
      const adlElement = command.getADLDom(id)
      const adlElementText = new XMLSerializer().serializeToString(adlElement);
      return adlElementText;
    }
  }

  /**
   * Provides a text-edit option for the context menu
   */
  return function(event, cm) {

    const selectedElements = hasLastSelected(selector());

    if (selectedElements.length == 1) {
      const theElement = selectedElements[0];

      cm.addControl(event, "/public/behaviours/editable/xml/xml.svg", 'Edit XML', () => {
        const defaultText = xmlCollector(theElement);
        cm.destroy();
        xmlModal.clear();
        const editableArea = formFields([]);
        var mirror;

        xmlModal.getContent(event).appendChild(form([
          editableArea,
          inlineButtons([
            ok('ok', {}, (e) => {
              e.preventDefault();
              const steps = [createUpdateStep(theElement, mirror.getValue())];
              command.pushAllAndPerform(steps);
              xmlModal.destroy();
            }),
            cancel('cancel', [], () => xmlModal.destroy())
          ])
        ], 'editXml'));

        xmlModal.open(event)
        mirror = CodeMirror(editableArea, {
          mode: "application/xml",
          lineNumbers: true,
          value: defaultText
        });

        mirror.setSize(editableArea.clientWidth, editableArea.clientHeight);
      });
    }
  }
}
