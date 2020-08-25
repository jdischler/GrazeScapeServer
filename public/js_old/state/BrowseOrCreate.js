

//------------------------------------------------------------------------------
Ext.define('DSS.state.BrowseOrCreate', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.operation_browse_create',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	DSS_singleText: '"Start by creating a new operation"',
					
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
				margin: '1rem',
			},
			items: [{
				xtype: 'component',
				cls: 'section-title accent-text',
				html: 'Farm Operations'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information med-text',
					bind: {
						html: '{browse_or_create}',
					},
					html: me.DSS_singleText
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Create New',
					handler: function() {
						DSS.ApplicationFlow.instance.showNewOperationPage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
