import { getMainSvg } from '../../../bundles/screen.js'
import { createUniqueId, encodeADLElement, onlyLastSelected } from '../../../bundles/api.js'
import { ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { Command } from '../../../classes/command/command.js';
import { Metadata, MetadataUser } from '../../../classes/metadata/metadata.js';
import { Selector } from '../../../bundles/types.js';


export function voteableSelector() {
	const votables = Array.from(getMainSvg().querySelectorAll("[k9-ui~=vote].selected"));
	return votables;
}

export function initVoteContextMenuCallback(
	command: Command, 
	metadata: Metadata, 
	selector: Selector = null) : ContextMenuCallback {
	
	if (selector == undefined) {
		selector = voteableSelector;
	}
	
	function canVote(e: Element, up: boolean) {
		const adl = command.getADLDom(e.getAttribute("id"));
		const user = metadata.get('user') as MetadataUser;
		if (!user) {
			return false;
		}
		const name = user.login as string;
		const exists = adl.querySelector('vote[from='+name+']');
		return up ? exists == null : exists != null;
	}
	
	function addVote(e: Element) {
		const name = (metadata.get('user') as MetadataUser).login;
		const voteId = createUniqueId();
		const theVote = '<vote from="'+name+'" id="'+voteId+'" />';
		const base64Vote = encodeADLElement(theVote);
		command.push({
			"type": "InsertXML",
			"fragmentId": e.getAttribute("id"),
			"base64Element": base64Vote,
			"newId": voteId
		});
		
		// update on screen
		const countTag = e.querySelector(".count")
		const votes = parseInt(countTag.textContent);
		countTag.textContent = ""+(votes+1);
		const voteTag = e.querySelector("[k9-elem=votes]");
		voteTag.setAttribute("count", ""+(votes+1));
	}
	
	function removeVote(e: Element) {
		const adl = command.getADLDom(e.getAttribute("id"));
		const name = (metadata.get('user') as MetadataUser).login;
		const existing = adl.querySelector('vote[from='+name+']');
		const voteId = existing.getAttribute("id");
		const voteAdl = command.getAdl(voteId);

		command.push({
			"type": "Delete",
			"fragmentId": e.getAttribute("id"),
			"base64Element": voteAdl,
		});
		
		// update on screen
		const countTag = e.querySelector(".count")
		const votes = parseInt(countTag.textContent);
		countTag.textContent = ""+(votes-1);
		const voteTag = e.querySelector("[k9-elem=votes]");
		voteTag.setAttribute("count", ""+(votes-1));
	}
	
	/**
	 * Provides a vote option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElement = onlyLastSelected(selector());
		
		if (selectedElement) {
			if (canVote(selectedElement, true)) {
				contextMenu.addControl(event, "/public/behaviours/voting/vote/up.svg", "Vote Up", 
					function() {
						contextMenu.destroy();
						addVote(selectedElement);
						command.perform();
					});
			} else if (canVote(selectedElement, false)) {
				contextMenu.addControl(event, "/public/behaviours/voting/vote/down.svg", "Vote Down", 
					function() {
						contextMenu.destroy();
						removeVote(selectedElement);
						command.perform();
					});	
			}
		}
	}
}