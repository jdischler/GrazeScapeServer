
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.DrawAndApply', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_draw_apply',
	
	requires: [
		'DSS.field_shapes.apply.SoilP',
		'DSS.field_shapes.apply.Landcover',
		'DSS.field_shapes.apply.Tillage',
		'DSS.field_shapes.apply.SpreadManure',
		'DSS.field_shapes.apply.Fertilizer',
	],
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				itemId: 'dss-draw-apply',
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
	
});
