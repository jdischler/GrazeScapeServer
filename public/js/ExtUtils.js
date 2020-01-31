//-----------------------------------------------------
// DSS.utils
//
//-----------------------------------------------------
Ext.define('DSS.utils', {
    extend: 'Ext.Base',
    
	statics: {
		layout: function(type, pack, align) {
			return {
				type: type,
				pack: pack || undefined,
				align: align || undefined
			}
		},
		
		// Quicktip singleton config overrides do not cover a few options I want globally...
		tooltip: function(text) {
			return {
				text: text,
	            trackMouse: false,
	            defaultAlign: 'b50-t50',
	            anchor: true,
	            showDelay: 250,
			}
		},
		
		// entry =  '.cssClass { color: #F00; }'
		addStyle: function(entry) {
			let style = document.createElement('style');
			style.type = 'text/css';
			style.innerHTML = entry;
			document.getElementsByTagName('head')[0].appendChild(style);
		}		
		
	},
		
});
