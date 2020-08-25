
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.DrawAndApply', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_draw_apply',
    alternateClassName: 'DSS.DrawFieldShapes',
    singleton: true,	
	
    autoDestroy: false,
    
    scrollable: 'y',

	requires: [
		'DSS.field_shapes.apply.SoilP',
		'DSS.field_shapes.apply.Landcover',
		'DSS.field_shapes.apply.Tillage',
		'DSS.field_shapes.apply.SpreadManure',
		'DSS.field_shapes.apply.Fertilizer',
	],
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		if (!DSS['viewModel']) DSS['viewModel'] = {}
		DSS.viewModel.drawAndApply = new Ext.app.ViewModel({
			formulas: {
				tillageValue: { 
					bind: '{tillage.value}',
					get: function(value) { return {tillage: value }; 			},
					set: function(value) { this.set('tillage.value', value); 	}
				}
			},
			data: {
				soil_p: {
					is_active: true,
					value: 32,
				},
				crop: {
					is_active: true,
					value: 'ps',
				},
				tillage: {
					is_active: false,
					value: 'spcu'
				},
				manure: {
					is_active: false,
					value: 10
				},
				fertilizer: {
					is_active: false,
					n: 100,
					p: 30
				}
			}
		})
		
		me.setViewModel(DSS.viewModel.drawAndApply);
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				cls: 'section-title light-text text-drp-20',
				html: 'Field Shapes <i class="fas fa-draw-polygon fa-fw accent-text text-drp-50"></i>',
				height: 35
			},{
				xtype: 'container',
				style: 'background-color: #666; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				margin: '8 4',
				padding: '2 8 10 8',
				defaults: {
					DSS_parent: me,
				},
				items: [{
					xtype: 'component',
					cls: 'information light-text text-drp-20',
					html: 'Draw and Apply',
				},{
					xtype: 'field_shapes_apply_soil_p'
				},{
					xtype: 'field_shapes_apply_landcover'
				},{
					xtype: 'field_shapes_apply_tillage'
				},{
					xtype: 'field_shapes_apply_manure'
				},{
					xtype: 'field_shapes_apply_fertilizer'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	addModeControl: function() {
		let me = this;
		let c = DSS_viewport.down('#DSS-mode-controls');
		
		if (!c.items.has(me)) {
			Ext.suspendLayouts();
				c.removeAll(false);
				c.add(me);
			Ext.resumeLayouts(true);
		}
	}
	
});
