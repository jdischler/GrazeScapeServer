
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.GrazeAnimals', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_graze_animals',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 75,
	
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
					html: "Graze Animals",
				},
					getToggle(me, 'graze_animals.is_active') // Helper defined in DrawAndApply.js
				]
			},{
				xtype: 'checkboxgroup',
				itemId: 'contents',
				style: 'padding: 0px; margin: 0px', // fixme: eh...
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				items: [{ 
					boxLabel: 'Dairy - Lactating', 		bind: '{graze_animals.dairy-lactating}',
				},{
					boxLabel: 'Dairy - Non-Lactating', 	bind: '{graze_animals.dairy-nonlactating}',
				},{
					boxLabel: 'Beef Cows', 			bind: '{graze_animals.beef}',
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
