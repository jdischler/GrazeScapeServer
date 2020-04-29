
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Tillage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_tillage',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 94,
	
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
					html: "Set Tillage",
				},
					getToggle(me, 'tillage.is_active')
				]
			},{
				xtype: 'radiogroup',
				itemId: 'contents',
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				bind: { value: { tillage: '{tillage.value}' }},
				defaults: {
					name: 'tillage'
				},
				items: [{ 
					boxLabel: 'No-till', 			inputValue: 'nt',
				},{
					boxLabel: 'Spring-cultivation',	inputValue: 'spcu',
				},{
					boxLabel: 'Chisel + disk',		inputValue: 'chdsk',
				},{
					boxLabel: 'Moldboard plow', 	inputValue: 'mp'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
