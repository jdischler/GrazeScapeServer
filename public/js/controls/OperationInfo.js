
DSS.utils.addStyle('.underlined-input { border: none; border-bottom: 1px solid #ddd; display:table; width: 100%; height:100%; padding: 0 0 2px}')   
DSS.utils.addStyle('.underlined-input:hover { border-bottom: 1px solid #7ad;}')
//DSS.utils.addStyle('.x-form-text-default {font-size: 1rem}')
//DSS.utils.addStyle('.x-form-text-default:focus { background:#ebf4f7; border-top-left-radius: 4px; border-top-right-radius: 4px}')

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
				html: '<b style="color:#27b">Select <i class="fas fa-map-marker-alt"></i></b> a location for this operation on the map'
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
				},
				items: [{
					fieldLabel: 'Operation',
					name: 'operation',
					allowBlank: false,
					value: me.DSS_operation,
				//	width: 250,
					margin: '10 0',
					padding: 4,
				},{
					fieldLabel: 'Owner',
					name: 'owner',
					allowBlank: false,
				//	width: 250,
					margin: '10 0',
					padding: 4,
				},{
					fieldLabel: 'Address',
					name: 'address',
				//	width: 250,
					margin: '12 0',
					padding: 4,
				},{
					itemId: 'location_x',
					fieldLabel: 'Location x',
					name: 'location_x',
					allowBlank: false,
					hidden: true,
				},{
					itemId: 'location_y',
					fieldLabel: 'Location y',
					name: 'location_y',
					allowBlank: false,
					hidden: true,
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Create',
					formBind: true,
					handler: function() {
						var form = this.up('form').getForm();
						if (form.isValid()) {
							form.submit({
								success: function(form, action) {
									var obj = JSON.parse(action.response.responseText);
									
									DSS.activeFarm = obj.farm.id;
									DSS.ApplicationFlow.instance.showManageOperationPage();
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
		me.controlMap();
	},
	
	controlMap: function() {
		let me = this;
		
		DSS.mouseMoveFunction = DSS.MapState.mouseoverFarmHandler();
		DSS.mapClickFunction = function(evt, coords) {
			
			me.down('#location_x').setValue(coords[0]); 
			me.down('#location_y').setValue(coords[1]);
			DSS.MapState.setPinMarker(coords, 1);
		}

		DSS.layer.farms.setOpacity(0.5);
		
	}
		

});

