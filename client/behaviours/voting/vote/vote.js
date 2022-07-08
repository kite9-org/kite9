import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.3'
import { hasLastSelected, parseInfo, getContainingDiagram, reverseDirection, createUniqueId, encodeADLElement } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.3'


export function voteableSelector() {
	const votables = Array.from(getMainSvg().querySelectorAll("[k9-ui~=vote].selected"));
	return votables;
}

export function initVoteContextMenuCallback(command, metadata, selector) {
	
	if (selector == undefined) {
		selector = voteableSelector;
	}
	
	function canVote(e, up) {
		const adl = command.getADLDom(e.getAttribute("id"));
		const user = metadata.get('user');
		if (!user) {
			return false;
		}
		const name = user.login;
		const exists = adl.querySelector('vote[from='+name+']');
		return up ? exists == null : exists != null;
	}
	
	function addVote(e) {
		const name = metadata.get('user').login;
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
	
	function removeVote(e) {
		const adl = command.getADLDom(e.getAttribute("id"));
		const name = metadata.get('user').login;
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
		
		const selectedElement = hasLastSelected(selector(), true);
		
		if (selectedElement) {
			if (canVote(selectedElement, true)) {
				contextMenu.addControl(event, "/public/behaviours/voting/vote/up.svg", "Vote Up", 
					function(e2, selector) {
						contextMenu.destroy();
						addVote(selectedElement);
						command.perform();
					});
			} else if (canVote(selectedElement, false)) {
				contextMenu.addControl(event, "/public/behaviours/voting/vote/down.svg", "Vote Down", 
					function(e2, selector) {
						contextMenu.destroy();
						removeVote(selectedElement);
						command.perform();
					});	
			}
		}
	}
}