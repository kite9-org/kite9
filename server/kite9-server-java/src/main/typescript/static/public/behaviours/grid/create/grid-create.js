import { getDocumentParam } from "../../../bundles/api.js";
export function initCellCreator(command, templateUri = undefined) {
    if (templateUri == undefined) {
        templateUri = getDocumentParam('cell-template-uri');
    }
    return function (parentId, x, y, newId) {
        command.push({
            type: 'InsertUrl',
            fragmentId: parentId,
            uriStr: templateUri,
            newId: newId,
        });
        command.push({
            type: 'ReplaceStyle',
            fragmentId: newId,
            name: "--kite9-occupies-x",
            to: x + ' ' + x,
        });
        command.push({
            type: 'ReplaceStyle',
            fragmentId: newId,
            name: "--kite9-occupies-y",
            to: y + ' ' + y,
        });
        return newId;
    };
}
