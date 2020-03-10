
//------------------------------------------------------------------------------
Ext.define('DSS.controls.Management', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Container',
	alias: 'widget.management',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				labelAlign: 'right'
			},
			items: [{
				xtype: 'radio',
				boxLabel: '>30% Legumes', 
				fieldLabel: 'Pasture',
				labelWidth: 100,
                checked: true,
				padding: '2 0',
				name: 'landcover'
			},{
				xtype: 'radio',
				fieldLabel: 'Crop Rotations',
				labelWidth: 100,
				boxLabel: 'Continuous Corn',
				name: 'landcover'
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Dairy Rotation', 
				name: 'landcover'
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Corn - Soy', 
				name: 'landcover'
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Corn - Soy - Oats', 
				name: 'landcover'
			},{
				xtype: 'checkbox',
				boxLabel: 'Pasture already established?', 
				padding: '2 0',
				checked: true,
			},{
				xtype: 'radiogroup',
				fieldLabel: 'Tillage',
				labelWidth: 100,
				columns: 1, 
				vertical: true,
				defaults: {
					padding: '2 0',
					group: 'tillage'
				},
				items: [{ 
					boxLabel: 'No-till', 
	                checked: true,
				},{
					boxLabel: 'Chisel, disked', 
				},{
					boxLabel: 'Moldboard plow', 
				}],
			},{
				xtype: 'radiogroup',
				fieldLabel: 'Season',
				vertical: true,
				labelWidth: 100,
				columns: 2, 
				defaults: {
					padding: '2 0',
					group: 'tillage-season'
				},
				items: [{ 
					boxLabel: 'Spring', 
	                checked: true,
				},{
					boxLabel: 'Fall', 
				}]
			},{
				xtype: 'radiogroup',
				fieldLabel: 'Cover Crop',
				labelWidth: 100,
				columns: 2, 
				vertical: true,
				defaults: {
					padding: '2 0',
					group: 'cover-crop'
				},
				items: [{ 
					boxLabel: 'Yes', 
	                checked: true,
				},{
					boxLabel: 'No', 
				}]
			},{
				xtype: 'radiogroup',
				fieldLabel: 'Grazing',
				columns: 1, 
				labelWidth: 100,
				vertical: true,
				defaults: {
					padding: '2 0',
					group: 'grazing'
				},
				items: [{ 
					boxLabel: 'Rotational', 
	                checked: true,
				},{
					boxLabel: 'Continuous - light (0.8AU/ac)', 
				},{
					boxLabel: 'Continuous - heavy (1.8AU/ac)', 
				}]
			}]
		});
		
		me.callParent(arguments);
	},

});

