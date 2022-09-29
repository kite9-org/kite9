


export function initCellCreator(command, templateUri) {
	
	if (templateUri == undefined) {
		templateUri = document.params['cell-template-uri'];
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
