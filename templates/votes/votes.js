import { contextMenu, command, metadata } from '/github/kite9-org/kite9/templates/editor/editor.js';
import { initVoteContextMenuCallback } from '/github/kite9-org/kite9/behaviours/voting/vote/vote.js'; 
import { once } from '/github/kite9-org/kite9/bundles/ensure.js';

once(() => contextMenu.add(initVoteContextMenuCallback(command, metadata)));