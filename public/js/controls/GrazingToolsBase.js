
//------------------------------------------------------------------------------
Ext.define('DSS.controls.GrazingToolsBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.grazing_tools_base',

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
				html: 'GrazeScape Toolbox'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Access tools and calculators not related to a specific operation',
				},{
					xtype: 'button',
					cls: 'information',
					text: 'Open Toolbox',
					margin: '8 72'
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
