

//------------------------------------------------------------------------------
Ext.define('DSS.state.operation.Main', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
    alternateClassName: 'DSS.OperationMain',
	alias: 'widget.operation_main',

	requires: [
		'DSS.state.operation.CropNutrientMode',
		'DSS.state.operation.FieldShapeMode',
		'DSS.state.operation.AnimalGrazingMode'
	],
	
	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	statics: {
		get: function() {
			let def = {
					xtype: 'operation_main'
			};
			
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
					html: 'Assign crops and nutrients'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					toggleGroup: 'manage-operation',
					text: 'Crops / Nutrients',
					toggleHandler: function(self, pressed) {
						if (pressed) {
							AppEvents.triggerEvent('show_crop_nutrient_mode')
						}
						else {
							AppEvents.triggerEvent('hide_crop_nutrient_mode')
						}
//						DSS.ApplicationFlow.instance.showNewOperationPage();
					}
				},{//------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Manage animals and grazing'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					toggleGroup: 'manage-operation',
					text: 'Animals / Grazing',
					toggleHandler: function(self, pressed) {
						if (pressed) {
							AppEvents.triggerEvent('show_animal_grazing_mode')
						}
						else {
							AppEvents.triggerEvent('hide_animal_grazing_mode')
						}
					//	DSS.ApplicationFlow.instance.showNewOperationPage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);

//		Ext.create('DSS.controls.ApplicationState', {id: 'crap-state'}).showAt(400,-4);
		
	}

});

