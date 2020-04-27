
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Landcover', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_landcover',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_parent: false, // should set
	DSS_active: false,
	DSS_sectionHeight: 64,
	
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
					getToggle(me)
				]
			},{
				xtype: 'radiogroup',
				itemId: 'contents',
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				DSS_height: me.DSS_sectionHeight,
				//layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				//padding: '0 0 6 0',
				height: (me.DSS_active ? me.DSS_sectionHeight : 0),
				items: [{ 
					boxLabel: 'Bluegrass-wc', name: 'rb', inputValue: '1',
					tooltip: 'Bluegrass with white clover',
					checked: true,
				},{
					boxLabel: 'Orchardgrass-al', name: 'rb', inputValue: '2',
					tooltip: 'Orchardgrass-alsike',
				},{
					boxLabel: 'Orchardgrass-rc', name: 'rb', inputValue: '3',
					tooltip: 'Orchardgrass with red clover',
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
