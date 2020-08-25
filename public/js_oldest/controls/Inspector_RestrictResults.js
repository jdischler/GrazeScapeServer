
//------------------------------------------------------------------------------
Ext.define('DSS.controls.Inspector_RestrictResults', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_restrict_results',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				margin: '8 4',
				padding: '2 8 10 8',
				items: [{
					xtype: 'component',
					style: 'text-align: center; color: #ccc',
					padding: 4,
					html: 'Restrict Results'
				},{
					xtype: 'button',
					margin: '0 48',
					html: '<i class="fas fa-plus-circle"></i>',
				},{
					xtype: 'container',
					itemId: 'mode-options',
					layout: 'fit'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
