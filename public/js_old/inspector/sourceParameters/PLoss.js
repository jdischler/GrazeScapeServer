//DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
Ext.define('DSS.inspector.sourceParameters.PLoss', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_ploss',
	
	cls: 'source-parameters',
	margin: '0 0 4 0',
	padding: 4,
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		let numberListener = { 
			keypress: function(self, evt) {
				if (evt.getKey() == evt.ENTER) {
					DSS_Refilter(20);
				}
				else {
					DSS_RefilterDelayed(1000);
				}
			},
			spinup: function() {
				DSS_RefilterDelayed(750);
			},
			spindown: function() {
				DSS_RefilterDelayed(750);
			}
		};
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'numberfield',
				itemId: 'dss-soil-p',
				fieldLabel: 'Soil P',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 5, maxValue: 200,
				value: 35, step: 5,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'numberfield',
				itemId: 'dss-manure',
				fieldLabel: '% Manure',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 0, maxValue: 150,
				value: 20, step: 10,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'numberfield',
				itemId: 'dss-fertilizer',
				fieldLabel: '% Fert',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 0, maxValue: 150,
				value: 80, step: 10,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'radiogroup',
				itemId: 'dss-landcover',
				fieldLabel: 'Crop',
				labelAlign: 'right',
				labelWidth: 40,
				columns: 1,
				vertical: true,
				items: [{
					boxLabel: 'Pasture - Rot', name: 'landcover', inputValue: 'pr', checked: true
				},{
					boxLabel: 'Pasture - Low', name: 'landcover', inputValue: 'pl'
				},{
					boxLabel: 'Pasture - Hi', name: 'landcover', inputValue: 'ph'
				},{
					boxLabel: 'Continuous Corn', name: 'landcover', inputValue: 'cc'
				},{
					boxLabel: 'Dairy Rotation', name: 'landcover', inputValue: 'dr'
				},{
					boxLabel: 'Corn - Soy - Oats', name: 'landcover', inputValue: 'cso'
				},{
					boxLabel: 'Cash Grain', name: 'landcover', inputValue: 'cg'
				}],
				listeners: {
					change: function() {
						DSS_RefilterDelayed(50);
					}
				}
			},{
				xtype: 'radiogroup',
				itemId: 'dss-cover',
				fieldLabel: 'Cover',
				labelAlign: 'right',
				labelWidth: 40,
				columns: 2,
				vertical: false,
				items: [{
					boxLabel: 'Yes', name: 'cover', inputValue: 'cc', checked: true
				},{
					boxLabel: 'No', name: 'cover', inputValue: 'nc'
				}],
				listeners: {
					change: function() {
						DSS_RefilterDelayed(50);
					}
				}
			},{
				xtype: 'radiogroup',
				itemId: 'dss-tillage',
				fieldLabel: 'Till',
				labelAlign: 'right',
				labelWidth: 40,
				columns: 2,
				vertical: false,
				items: [{
					boxLabel: 'No-till', name: 'tillage', inputValue: 'nt', checked: true
				},{
					xtype: 'component'
				},{
					boxLabel: 'Fall C,D', name: 'tillage', inputValue: 'fc'
				},{
					boxLabel: 'Sp C,D', name: 'tillage', inputValue: 'sc'
				},{
					boxLabel: 'Fall MP', name: 'tillage', inputValue: 'fm'
				},{
					boxLabel: 'Sp MP', name: 'tillage', inputValue: 'sm'
				}],
				listeners: {
					change: function() {
						DSS_RefilterDelayed(50);
					}
				}
			},{
				xtype: 'checkbox',
				itemId: 'dss-snap-plus',
				boxLabel: 'Apply S+ transmission...',
				handler: function(self, checked) {
					DSS_RefilterDelayed(50);
				}
			}]
		});
		
		me.callParent(arguments);
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		let options = {
		};
		
		options['soil-p'] = me.down('#dss-soil-p').getValue();
		options['perc-manure'] = me.down('#dss-manure').getValue();
		options['perc-fertilizer'] = me.down('#dss-fertilizer').getValue();
		options['landcover'] = me.down('#dss-landcover').getValue()['landcover'];
		options['cover-crop'] = me.down('#dss-cover').getValue()['cover'];
		options['tillage'] = me.down('#dss-tillage').getValue()['tillage'];
		options['snap-plus-transmission'] = me.down('#dss-snap-plus').getValue();
		
		me.down('#dss-cover').setDisabled(options['landcover'][0] == 'p');
		me.down('#dss-tillage').setDisabled(options['landcover'][0] == 'p');
		
		return options;
	}
	
});

