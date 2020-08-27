
DSS.utils.addStyle('.underlined-input { border: none; border-bottom: 1px solid #ddd; display:table; width: 100%; height:100%; padding: 0 0 2px}')   
DSS.utils.addStyle('.underlined-input:hover { border-bottom: 1px solid #7ad;}')
DSS.utils.addStyle('.right-pad { padding-right: 32px }')   

//------------------------------------------------------------------------------
Ext.define('DSS.state.CreateNew', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.operation_create',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	DSS_singleText: '"Start by creating a new operation"',
					
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'start', 'begin'),
				items: [{
					xtype: 'component',
					cls: 'back-button',
					tooltip: 'Back',
					html: '<i class="fas fa-reply"></i>',
					listeners: {
						render: function(c) {
							c.getEl().getFirstChild().el.on({
								click: function(self) {
									DSS.ApplicationFlow.instance.showLandingPage();
								}
							});
						}
					}					
				},{
					xtype: 'component',
					flex: 1,
					cls: 'section-title accent-text right-pad',
					html: 'Create New'
				}]
			},{ 
				xtype: 'component',
				cls: 'information',
				html: '<b style="color:#27b">Select <i class="fas fa-map-marker-alt"></i></b> a location for this operation on the map'
			},{
				xtype: 'form',
				url: 'create_operation',
				jsonSubmit: true,
				header: false,
				border: false,
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				margin: '8 0',
				defaults: {
					xtype: 'textfield',
					labelAlign: 'right',
					labelWidth: 70,
					triggerWrapCls: 'underlined-input',
				},
				items: [{
					fieldLabel: 'Operation',
					name: 'operation',
					allowBlank: false,
					value: me.DSS_operation,
					margin: '10 0',
					padding: 4,
				},{
					fieldLabel: 'Owner',
					name: 'owner',
					allowBlank: false,
					margin: '10 0',
					padding: 4,
				},{
					fieldLabel: 'Address',
					name: 'address',
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
									AppEvents.triggerEvent('activate_operation')
									// TODO: centralize
									DSS.layer.markers.setOpacity(0.5);
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
		me.bindMapClick();
	},

	//------------------------------------------------------------------
	bindMapClick: function() {
		let me = this;
		
		DSS.mapClickFunction = function(evt, coords) {
			me.down('#location_x').setValue(coords[0]); 
			me.down('#location_y').setValue(coords[1]);
			DSS.MapState.setPinMarker(coords, 1);
		}
	}

});

