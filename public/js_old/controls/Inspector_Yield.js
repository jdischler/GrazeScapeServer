
//------------------------------------------------------------------------------
Ext.define('DSS.controls.Inspector_Yield', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_yield',
	
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

		Ext.applyIf(me, {
			items: [{
				xtype: 'radiogroup',
				itemId: 'dss-yield',
				hideEmptyLabel: true, 
				columns: 1,
				vertical: true,
				items: [{
					boxLabel: 'Bluegrass White Clover', name: 'yield', inputValue: 'bluegrassWhiteClover', checked: true,
				},{
					boxLabel: 'Orchardgrass Alsike', name: 'yield', inputValue: 'orchardgrassAlsike',
				},{
					boxLabel: 'Orchardgrass Red Clover', name: 'yield', inputValue: 'orchardgrassRedClover',
				},{
					boxLabel: 'Timothy Alsike', name: 'yield', inputValue: 'timothyAlsike'
				},{
					boxLabel: 'Generic Grass', name: 'yield', inputValue: 'genericGrass'
				},{
					xtype: 'component',
					html: '<span style="color: #ccc">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&#9473;&#9473;&#9473;&#9473;&#9473;&#9473;&#9473;&#9473;</style>'
				},{
					boxLabel: 'Alfalfa', name: 'yield', inputValue: 'alfalfa'
				},{
					boxLabel: 'Corn Grain', name: 'yield', inputValue: 'corn'
				},{
					boxLabel: 'Corn Silage', name: 'yield', inputValue: 'cornSilage'
				},{
					boxLabel: 'Soybeans', name: 'yield', inputValue: 'soy'
				}],
				listeners: {
					change: function() { me.getOptions() }
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
			crop: me.down('#dss-yield').getValue()['yield']				
		};
		
		me.DSS_InspectorParent.inspectorOptionsChanged(options, silent);
	}
	
});

