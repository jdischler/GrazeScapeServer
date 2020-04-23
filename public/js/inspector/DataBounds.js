
//------------------------------------------------------------------------------
Ext.define('DSS.inspector.DataBounds', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_data_bounds',

	style: 'background-color: #666; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
	cls: 'drop',
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	margin: '8 4 16 4',
	padding: '2 8',
	hidden: true,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				cls: 'information light-text text-drp-20',
				html: 'Inspection Area'
			},{//------------------------------------------------------------------
				xtype: 'radiogroup',
				vertical: true,
				minWidth: 130, // awful resize / hidden workaround
				columns: 1,
				listeners: {
					change: function(self, newValue, oldValue) {
						if (newValue != oldValue) {
							DSS_RefilterDelayed(50);
						}
					}
				},
				items: [{
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Active operation', name: 'bounds', inputValue: 'true', checked: true,
				},{
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Custom bounds', name: 'bounds', inputValue: 'false',
					itemId: 'dss-custom-bounds'
				}]
			}]
		});
		
		me.callParent(arguments);
		
		AppEvents.registerListener("activate_operation", function() {
			me.setHidden(false);
		})
		AppEvents.registerListener("deactivate_operation", function() {
			me.setHidden(true);
		})
		AppEvents.registerListener("set_inspector_bounds", function() {
			me.down('#dss-custom-bounds').setValue(true);
		})
	},
	
});
