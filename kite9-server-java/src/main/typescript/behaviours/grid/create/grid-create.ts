import { getDocumentParam } from "../../../bundles/api.js";
import { Command } from "../../../classes/command/command.js";
import { CellCreator } from "../layout/grid-layout.js";



export function initCellCreator(command: Command, templateUri: string = undefined) : CellCreator {
	
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
	}
}
