
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
					html: "Apply Fertilizer",
				},
					getToggle(me, 'fertilizer.is_active')
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				layout: DSS.utils.layout('vbox', 'start'),//, 'stretch'),
				padding: '0 0 6 0',
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-fert-n',
					fieldLabel: 'lbs N / acre',
					labelWidth: 90,
					labelAlign: 'right',
					bind: { value: '{fertilizer.n}' },
					minValue: 0,
					maxValue: 200,
					width: 160,
					step: 5
				},{
					xtype: 'numberfield',
					itemId: 'dss-fert-p',
					fieldLabel: 'lbs P / acre',
					labelWidth: 90,
					labelAlign: 'right', 
					bind: { value: '{fertilizer.p}' },
					minValue: 0,
					maxValue: 200,
					width: 160,
					step: 5
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
