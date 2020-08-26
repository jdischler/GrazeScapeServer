

//------------------------------------------------------------------------------
Ext.define('DSS.state.Manage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
    alternateClassName: 'DSS.OperationManage',
	alias: 'widget.operation_manage',

	requires: [
		'DSS.state.operation.FieldShapeMode',
		'DSS.state.Scenario',
	],
	
	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	statics: {
		get: function() {
			let def = {
					xtype: 'operation_manage'
			};
			
			return def;
		}
	},
	
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
				html: 'Manage'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{ //------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Define or edit field boundaries'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Field Shapes',
//					allowDepress:
					toggleGroup: 'manage-operation',
					toggleHandler: function(self, pressed) {
						if (pressed) {
							AppEvents.triggerEvent('show_field_shape_mode')
						}
						else {
							AppEvents.triggerEvent('hide_field_shape_mode')
						}
					//	DSS.ApplicationFlow.instance.showNewOperationPage();
					}
				},{//------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Design a scenario'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Scenarios',
					handler: function(self) {
						AppEvents.triggerEvent('hide_field_shape_mode')
						DSS.ApplicationFlow.instance.showScenarioPage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);

//		Ext.create('DSS.controls.ApplicationState', {id: 'crap-state'}).showAt(400,-4);
		
	}

});

