
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Tillage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_tillage',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 148,
	
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
					getToggle(me, 'tillage.is_active') // Helper defined in DrawAndApply.js
				]
			},{
				xtype: 'radiogroup',
				itemId: 'contents',
				style: 'padding: 0px; margin: 0px', // fixme: eh...
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				bind: { value: '{tillageValue}' },
				defaults: {
					name: 'tillage'
				},
				items: [{ 
					boxLabel: 'No-till', 			inputValue: 'NT',
				},{
					boxLabel: 'Spring Cultivation',	inputValue: 'SCU',
				},{
					boxLabel: 'Spring Chisel + Disk',inputValue: 'SCH',
				},{
					boxLabel: 'Spring Moldboard', 	inputValue: 'SMB'
				},{
					boxLabel: 'Fall Chisel + Disk',inputValue: 'FCH',
				},{
					boxLabel: 'Fall Moldboard', 	inputValue: 'FMB'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
