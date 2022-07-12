import { Modal } from '/github/kite9-org/kite9/client/classes/modal/modal.js?v=v0.6'
import { hasLastSelected, encodeADLElement } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.6'
import { form, ok, cancel, inlineButtons, formFields } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.6'
import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.6'
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
      return document.querySelectorAll("[id][k9-ui].selected");
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
