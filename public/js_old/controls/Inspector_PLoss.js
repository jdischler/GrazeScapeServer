//DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');

//------------------------------------------------------------------------------
Ext.define('DSS.controls.Inspector_PLoss', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_p_loss',
	
	margin: '8 0 0 0',
	padding: '8 8 4 8',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	style: 'background-color: #fff; border-radius: 4px',
	DSS_units: '%',
	DSS_min: 0,
	DSS_step: 5,
	DSS_value: 20,
	DSS_max: 120,
	DSS_compare: 'less-than', // must also correspond to a font-awesome symbol...
	DSS_InspectorParent: false, // should set
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		let numberListener = { 
			keypress: function(self, evt) {
				setTimeout(function() {
					me.getOptions(true);
				}, 5);
				if (evt.getKey() == evt.ENTER) {
					DSS_Refilter(20);
				}
				else {
					DSS_RefilterDelayed(1000);
				}
			},
			spinup: function() {
				setTimeout(function() {
					me.getOptions(true);
				}, 5);
				DSS_RefilterDelayed(750);
			},
			spindown: function() {
				setTimeout(function() {
					me.getOptions(true);
				}, 5);
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
					change: function() { me.getOptions() }
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
					change: function() { me.getOptions() }
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
					change: function() { me.getOptions() }
				}
			},{
				xtype: 'checkbox',
				itemId: 'dss-snap-plus',
				boxLabel: 'Apply S+ transmission...',
				handler: function(self, checked) {
					me.getOptions();
				}
			}]
		});
		
		me.callParent(arguments);
		me.getOptions();
	},
	
	//------------------------------------------------------------
	getOptions: function(silent) {
		let me = this;
		
		if (typeof silent === 'undefined') silent = false;
		
		let options = {
			crop: 'soybeans' // FIXME: TODO: the crop yield inspector helper variant should be configuring this. Other helpers shouldn't be setting this at all				
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
		
		me.DSS_InspectorParent.inspectorOptionsChanged(options, silent);
	}
	
});

