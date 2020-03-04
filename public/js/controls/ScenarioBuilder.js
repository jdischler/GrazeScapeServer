
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
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Create scenarios for this operation'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Create Scenarios',
					handler: function() {
						DSS.mainViewport.doOperationBasePage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
