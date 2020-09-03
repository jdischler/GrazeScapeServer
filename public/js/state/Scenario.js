

//------------------------------------------------------------------------------
Ext.define('DSS.state.Scenario', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
    alternateClassName: 'DSS.StateScenario',
	alias: 'widget.state_scenario',

	requires: [
		'DSS.state.scenario.CropNutrientMode',
		'DSS.state.scenario.AnimalDialog'
	],
	
	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',

	statics: {
		get: function() {
			let def = {
					xtype: 'state_scenario'
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
									DSS.ApplicationFlow.instance.showManageOperationPage();
								}
							});
						}
					}					
				},{
					xtype: 'component',
					flex: 1,
					cls: 'section-title accent-text right-pad',
					// TODO: Dynamic name...
					html: '"Baseline"'
				}]
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{//------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Configure animals and grazing'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Animals',
					handler: function(self) {
						if (!DSS.dialogs) DSS.dialogs = {};
						if (!DSS.dialogs.AnimalDialog) {
							DSS.dialogs.AnimalDialog = Ext.create('DSS.state.scenario.AnimalDialog'); 
							DSS.dialogs.AnimalDialog.setViewModel(DSS.viewModel.scenario);		

						}
						DSS.dialogs.AnimalDialog.show().center().setY(0);
					}
				},{//------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Assign crops and nutrients'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					toggleGroup: 'create-scenario',
					allowDepress: true,
					text: 'Field Properties',
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
					height: 32
				},{//------------------------------------------
					xtype: 'component',
					cls: 'information med-text',
					html: 'Run simulations'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Compute',
					handler: function(self) {
					}
				}]
			}]
		});
		
		me.callParent(arguments);
		DSS.Inspector.addModeControl()
		DSS.MapState.disableFieldDraw();
		DSS.draw.setActive(false);
		DSS.modify.setActive(false);
		DSS.fieldStyleFunction = undefined;	DSS.layer.fields.changed();

		me.initViewModel();
	},
	
	//-----------------------------------------------------------------------------
	initViewModel: function() {
		if (DSS && DSS.viewModel && DSS.viewModel.scenario) return;
		
		if (!DSS['viewModel']) DSS['viewModel'] = {}
		DSS.viewModel.scenario = new Ext.app.ViewModel({
			formulas: {
				tillageValue: { 
					bind: '{tillage.value}',
					get: function(value) { return {tillage: value }; 			},
					set: function(value) { this.set('tillage.value', value); 	}
				}
			},
			data: {
				dairy: {
					// counts
					lactating: 10,
					dry: 20,
					heifers: 40,
					youngstock: 80,
					// milk yield
					'daily-yield': 50,
					// lactating cows / confinement in months / grazing
					'lactating-confined': 12,
					'lactating-graze-time': 20,
					'lactating-rotation-freq': 'R4',
					// non-lactating cows / confinement / grazing
					'non-lactating-confined': 3,
					'non-lactating-graze-time': 24,
					'non-lactating-rotation-freq': 'R2',
				},
				beef: {
					cows: 20,
					stockers: 40,
					finishers: 80,
					// average weight gain
					'daily-gain': 4,
					// confinement in months / grazing
					'confined': 3,
					'graze-time': 24,
					'rotation-freq': 'R2',
				}
			}
		})
	}

});

