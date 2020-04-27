
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.SoilP', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_soil_p',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_parent: false, // should set
	DSS_active: true,
	DSS_sectionHeight: 31,
	
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
					getToggle(me)
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				DSS_height: me.DSS_sectionHeight,
				layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				padding: '0 0 6 0',
				height: (me.DSS_active ? me.DSS_sectionHeight : 0),
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-soil-p',
					fieldLabel: 'Soil test value',
					labelWidth: 90,
					labelAlign: 'right',
					value: 32,
					minValue: 1,
					maxValue: 200,
					width: 150,
					step: 5
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		if (me.DSS_active && !me.isHidden()) {
			return {
				restrict_to_fields: {
					// FIXME:
					farm_id: DSS.activeFarm,
					aggregate: me.down('#aggregate').getValue()
				}
			}
		};
		
//		return {};
	}
	
});
