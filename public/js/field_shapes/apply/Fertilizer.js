
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Fertilizer', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_fertilizer',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 60,
	
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
					html: "Manage Nutrients",
				},
					getToggle(me, 'fertilizer.is_active') // Helper defined in DrawAndApply.js
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				layout: DSS.utils.layout('vbox', 'start'),//, 'stretch'),
				padding: '0 0 6 0',
				defaults: {
					labelWidth: 110,
					labelAlign: 'right',
					width: 180,
				},
				items: [{
					xtype: 'checkbox',
					itemId: 'dss-fert-can-manure-pastures',
					fieldLabel: 'Manure Pastures',
					bind: { value: '{fertilizer.canManurePastures}' },
					inputAttrTpl: 'data-qtip="Allow pastures to accept confined manure"',
				},{
					xtype: 'numberfield',
					itemId: 'dss-fert-ext-recs',
					fieldLabel: 'Extensions Recs',
					inputAttrTpl: 'data-qtip="Nutrient application %, relative to UW Extension recommendations"',
					bind: { value: '{fertilizer.extRecs}' },
					minValue: 0,
					maxValue: 200,
					step: 10
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
