
//------------------------------------------------------------------------------
Ext.define('DSS.controls.ScenarioBuilder', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.scenario_builder',

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
				html: 'Scenarios'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				defaults: {
					xtype: 'component',
					cls: 'information',
				},				
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Create scenarios for this operation'
				},{
					xtype: 'button',
					text: 'Create Scenarios',
					margin: '8 72',
					handler: function() {
						DSS.mainViewport.doOperationBasePage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
