
//------------------------------------------------------------------------------
Ext.define('DSS.controls.ApplicationState', {
//------------------------------------------------------------------------------
	extend: 'Ext.button.Segmented', // Ext.container
	alias: 'widget.app_state',
	
//	padding: '0 6 6 6',
	floating: true,
	shadow: false,
	
	style: 'border-radius: 2px; box-shadow: 0 4px 8px rgba(0,0,0,0.5)',
	layout: DSS.utils.layout('hbox', 'start'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
 				xtype: 'button',
				toggleGroup: 'operation-mode',
				padding: '4 0 0 0',
				height: 30,
				allowDepress: false,
			},
			items: [{
				text: 'Crops',
				tooltip: 'Assign crops / tillage',
				width: 70, 
			},{
				text: 'Nutrients',
				tooltip: 'Apply nutrients',
				width: 96
			},{
				html: '<i class="fas fa-search"></i>',
				tooltip: 'Activate Inspector <i class="fas fa-search small"></i> Mode',
				width: 48,
				pressed: true
			}]
		});
		
		me.callParent(arguments);
	},
	
});
