
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
				name: 'landcover',
				handler: function(self, checked) {
					if (checked) {
						me.down('#dss-grazing-block').setVisible(true);
						me.down('#dss-pasture-check').setVisible(true);
						me.down('#dss-cover-crop-block').setVisible(false);
						let chk = me.down('#dss-pasture-check').getValue(); // derf, checkbox doesn't have getChecked but radio does?
						console.log(chk);
						me.down('#dss-tillage-block').setVisible(!chk);
					}
					else {
						me.down('#dss-grazing-block').setVisible(false)
						me.down('#dss-pasture-check').setVisible(false);
						me.down('#dss-cover-crop-block').setVisible(true);
						me.down('#dss-tillage-block').setVisible(true);
					}
				}
			},{
				xtype: 'radio',
				fieldLabel: 'Crop Rotation',
				labelWidth: 100,
				boxLabel: 'Continuous Corn',
				name: 'landcover',
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Dairy Rotation', 
				name: 'landcover',
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Corn - Soy', 
				name: 'landcover',
			},{
				xtype: 'radio',
				fieldLabel: ' ',
				labelWidth: 100,
				labelSeparator: '',
				boxLabel: 'Corn - Soy - Oats', 
				name: 'landcover',
			},{
				xtype: 'checkbox',
				itemId: 'dss-pasture-check',
				boxLabel: 'Pasture already established?', 
				padding: '2 4',
				checked: true,
				handler: function(self, checked) {
					me.down('#dss-tillage-block').setVisible(!checked)
				}
			},{
				xtype: 'container',
				itemId: 'dss-tillage-block',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				defaults: {
					labelAlign: 'right'
				},
				hidden: true,
				items: [{
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
				}]
			},{
				xtype: 'radiogroup',
				itemId: 'dss-cover-crop-block',
				fieldLabel: 'Cover Crop',
				labelWidth: 100,
				columns: 2, 
				vertical: true,
				hidden: true,
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
				itemId: 'dss-grazing-block',
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

