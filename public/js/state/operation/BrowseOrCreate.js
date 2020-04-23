

//------------------------------------------------------------------------------
Ext.define('DSS.state.operation.BrowseOrCreate', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
    alternateClassName: 'DSS.BrowseOrCreate',
	alias: 'widget.operation_browse_create',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	statics: {
		get: function() {
			let def = {
					xtype: 'operation_browse_create'
			};
			let totalFarmCount = 0;
			if (totalFarmCount <= 0) {
//				def['DSS_text'] = 'Start by creating a new operation';
			}
			
			return def;
		}
	},
	
	DSS_text: 'Select <i class="accent-text fas fa-hand-pointer"></i> an operation on the map... or create a new one',
	
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
				html: 'Operations'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information med-text',
					html: me.DSS_text
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
