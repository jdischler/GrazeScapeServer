
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.SoilP', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_soil_p',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 28,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				width: '100%',
				layout: 'absolute',
				items: [{
					xtype: 'component',
					x: 0, y: -6,
					width: '100%',
					height: 28,
					cls: 'information accent-text bold',
					html: "Set Soil P",
				},
					getToggle(me, 'soil_p.is_active')
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				layout: 'center',
				padding: '0 0 6 0',
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-soil-p',
					fieldLabel: 'Soil test value',
					labelWidth: 90,
					labelAlign: 'right',
					bind: { value: '{soil_p.value}' },
					minValue: 1,
					maxValue: 200,
					width: 160,
					step: 5
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
