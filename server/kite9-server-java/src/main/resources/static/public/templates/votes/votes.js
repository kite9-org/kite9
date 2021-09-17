import { contextMenu, command, metadata } from '/public/templates/editor/editor.js';
import { initVoteContextMenuCallback } from '/public/behaviours/voting/vote/vote.js'; 
import { once } from '/public/bundles/ensure.js';

once(() => contextMenu.add(initVoteContextMenuCallback(command, metadata)));