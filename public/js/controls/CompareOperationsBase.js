
//------------------------------------------------------------------------------
Ext.define('DSS.controls.CompareOperationsBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.compare_operations_base',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'section-title',
				html: 'Compare Operations'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Compare how operations perform under different simulated conditions',
				},{
					xtype: 'button',
					cls: 'information',
					text: 'Compare',
					margin: '8 72',
					handler: function() {
						DSS.mainViewport.doChartWorkPanel();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
