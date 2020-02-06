
DSS.utils.addStyle('.information { padding: 0.5rem; color: #aaa; font-size: 1rem; text-align: center}')
DSS.utils.addStyle('.section-title { padding: 0.5rem; color: #48b; font-size: 1.2rem; text-align: center; font-weight: bold}');
DSS.utils.addStyle('.section { margin: 1rem; padding: 1rem; background-color: #fff; border: 1px solid #bbb; border-radius: 0.3rem; box-shadow: 0px 4px 8px rgba(0,0,0,0.25) }')

//------------------------------------------------------------------------------
Ext.define('DSS.controls.OperationsBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.operations_base',

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
				html: 'Manage Operations'
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
					html: 'Select an existing operation on the map or create a new one'
				},{
					xtype: 'button',
					text: 'Create New',
					margin: '8 72',
					handler: function() {
						DSS.mainViewport.doNewOperationPage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
