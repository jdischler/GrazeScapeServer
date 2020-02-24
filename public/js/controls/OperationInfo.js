
DSS.utils.addStyle('.underlined-input { border: none; border-bottom: 1px solid #ddd; display:table; width: 100%; height:100%; padding: 0 0 2px}')   
DSS.utils.addStyle('.underlined-input:hover { border-bottom: 1px solid #7ad;}')
DSS.utils.addStyle('.x-form-text-default {font-size: 1rem}')
DSS.utils.addStyle('.x-form-text-default:focus { background:#ebf4f7; border-top-left-radius: 4px; border-top-right-radius: 4px}')


//------------------------------------------------------------------------------
Ext.define('DSS.controls.OperationInfo', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.operation_info',

	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	statics: {
		get: function() {
			let def = { xtype: 'operation_info' };
			return def;
		}
	},
	
	// TODO: bind in existing data so this could be re-used for new or edit operation
	DSS_operation: undefined,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			xtype: 'container',
			layout: DSS.utils.layout('vbox', 'center', 'stretch'),
			cls: 'section',
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'information',
				html: '<b style="color:#27b">Select</b> a location for this operation on the map'
			},{
				xtype: 'form',
				url: 'create_operation',
				//standardSubmit: true,
				jsonSubmit: true,
				header: false,
				border: false,
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				margin: '8 0',
				defaults: {
					xtype: 'textfield',
					labelAlign: 'right',
					labelWidth: 80,
					triggerWrapCls: 'underlined-input',
					width: 280,
					margin: '12 0',
					padding: 4,
				},
				items: [{
					fieldLabel: 'Operation',
					name: 'operation',
					allowBlank: false,
					value: me.DSS_operation,
				},{
					fieldLabel: 'Owner',
					name: 'owner',
					allowBlank: false,
				},{
					fieldLabel: 'Address',
					name: 'address',
				},{
					// FIXME: TODO: Activate map in click point mode....
					fieldLabel: 'Location x',
					name: 'location_x',
					value: '-10118408.1516699',
					allowBlank: false,
					hidden: true,
				},{
					// FIXME: TODO: Activate map in click point mode....
					fieldLabel: 'Location y',
					name: 'location_y',
					value: '5370123.626836921',
					allowBlank: false,
					hidden: true,
				},{
					xtype: 'button',
					text: 'Create',
					formBind: true,
					margin: '24 72 12 72',
					handler: function() {
						var form = this.up('form').getForm();
						if (form.isValid()) {
							form.submit({
								success: function(form, action) {
									console.log( action.response.responseText);
									DSS.ApplicationFlow.showManageOperationPage();
								},
								failure: function(form, action) {
									console.log(form, action);
								}
							});
						}
			        }
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});

