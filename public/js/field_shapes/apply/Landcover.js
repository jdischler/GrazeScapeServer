
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Landcover', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_landcover',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 190,
	
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
					html: "Set Crop / Landcover",
				},
					getToggle(me, 'crop.is_active')
				]
			},{
				xtype: 'radiogroup',
				itemId: 'contents',
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				bind: { value: { crop: '{crop.value}' }},
				defaults: {
					name: 'crop'
				},
				items: [{ 
					boxLabel: 'Bluegrass-wc', 	inputValue: 'bgwc',
				},{
					boxLabel: 'Orchardgrass-al',inputValue: 'oga',
				},{
					boxLabel: 'Orchardgrass-rc',inputValue: 'ogrc',
				},{
					boxLabel: 'Timothy-alsike', inputValue: 'ta'
				},{
					boxLabel: 'Continuous Corn',inputValue: 'cc',
				},{
					boxLabel: 'Cash Grain',		inputValue: 'cg',
				},{
					boxLabel: 'Dairy Rotation 1',inputValue: 'dr',
				},{
					boxLabel: 'Dairy Rotation 2', inputValue: 'cso'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
