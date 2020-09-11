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
			defaults: {
				labelWidth: 50
			},
			items: [{
				xtype: 'numberfield',
				itemId: 'dss-soil-p',
				fieldLabel: 'Soil-Test P',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 5, maxValue: 200,
				value: 35, step: 5,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'numberfield',
				itemId: 'dss-fertilizer',
				fieldLabel: 'Applied P',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 0, maxValue: 150,
				value: 80, step: 10,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'numberfield',
				itemId: 'dss-manure',
				fieldLabel: '% DM',
				labelAlign: 'right',
				labelWidth: 90,
				maxWidth: 170,
				minValue: 0, maxValue: 50,
				value: 1, step: 0.1,
				enableKeyEvents: true,
				listeners: numberListener
			},{
				xtype: 'radiogroup',
				itemId: 'dss-landcover',
				fieldLabel: 'Crop',
				labelAlign: 'right',
				columns: 1,
				vertical: true,
				items: [{
					boxLabel: 'Pasture - Est', name: 'landcover', inputValue: 'ep'
				},{
					boxLabel: 'Pasture - Rot', name: 'landcover', inputValue: 'pr', checked: true
				},{
					boxLabel: 'Pasture - Cnt. Low', name: 'landcover', inputValue: 'pl'
				},{
					boxLabel: 'Pasture - Cnt. Hi', name: 'landcover', inputValue: 'ph'
				},{
					boxLabel: 'Continuous Corn', name: 'landcover', inputValue: 'cc'
				},{
					boxLabel: 'Dairy Rotation', name: 'landcover', inputValue: 'dr'
				},{
					boxLabel: 'Corn - Soy - Oats', name: 'landcover', inputValue: 'cso', disabled: true
				},{
					boxLabel: 'Cash Grain', name: 'landcover', inputValue: 'cg', disabled: true
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
				columns: 2,
				vertical: false,
				items: [{
					boxLabel: 'No-till', name: 'tillage', inputValue: 'nt', checked: true
				},{
					boxLabel: 'Sp Cult', name: 'tillage', inputValue: 'scu'
				},{
					boxLabel: 'Fall C+D', name: 'tillage', inputValue: 'fch'
				},{
					boxLabel: 'Sp C+D', name: 'tillage', inputValue: 'sch'
				},{
					boxLabel: 'Fall MBP', name: 'tillage', inputValue: 'fmb'
				},{
					boxLabel: 'Sp MBP', name: 'tillage', inputValue: 'smb'
				}],
				listeners: {
					change: function() {
						DSS_RefilterDelayed(50);
					}
				}
			},{
				xtype: 'radiogroup',
				itemId: 'dss-contour',
				fieldLabel: "Contour",
				labelAlign: 'right',
				columns: 2,
				vertical: false,
				items: [{
					boxLabel: 'Yes', name: 'contour', inputValue: 1, checked: true
				},{
					boxLabel: 'No', name: 'contour', inputValue: 0
				}],
				listeners: {
					change: function() {
						DSS_RefilterDelayed(50);
					}
				}
			},{
				xtype: 'checkbox',
				itemId: 'dss-snap-plus',
				boxLabel: 'Apply S+ transmission function',
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
		options['manure-dm'] = me.down('#dss-manure').getValue();
		options['total-p'] = me.down('#dss-fertilizer').getValue();
		options['landcover'] = me.down('#dss-landcover').getValue()['landcover'];
		options['cover-crop'] = me.down('#dss-cover').getValue()['cover'];
		options['tillage'] = me.down('#dss-tillage').getValue()['tillage'];
		options['on-contour'] = me.down('#dss-contour').getValue()['contour'];
		options['snap-plus-transmission'] = me.down('#dss-snap-plus').getValue();
		
		let isPasture = (options['landcover'][0] == 'p');
		let isPastureEst = (options['landcover'][0] == 'e');
		me.down('#dss-cover').setDisabled(isPasture | isPastureEst);
		me.down('#dss-tillage').setDisabled(isPasture);
		me.down('#dss-contour').setDisabled(isPasture);
		
		return options;
	}
	
});

