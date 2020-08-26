
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.SpreadManure', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_manure',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 32,
	
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
					html: "Spread Manure",
				},
					getToggle(me, 'manure.is_active') // Helper defined in DrawAndApply.js
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				layout: DSS.utils.layout('vbox', 'center'),
				padding: '0 0 6 0',
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-soil-p',
					fieldLabel: 'Tons / acre',
					labelWidth: 90,
					labelAlign: 'right',
					bind: { value: '{manure.value}' },
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
