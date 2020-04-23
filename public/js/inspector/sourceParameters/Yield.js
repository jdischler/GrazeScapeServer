
//------------------------------------------------------------------------------
Ext.define('DSS.inspector.sourceParameters.Yield', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_yield',
	
	cls: 'source-parameters',
	margin: '0 0 4 0',
	padding: 4,
	
	layout: 'fit',//DSS.utils.layout('vbox', 'start', 'center'),
	
	
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
					boxLabel: 'Bluegrass-white clover', name: 'yield', inputValue: 'bluegrassWhiteClover', checked: true,
				},{
					boxLabel: 'Orchardgrass-alsike', name: 'yield', inputValue: 'orchardgrassAlsike',
				},{
					boxLabel: 'Orchardgrass-red clover', name: 'yield', inputValue: 'orchardgrassRedClover',
				},{
					boxLabel: 'Timothy-alsike', name: 'yield', inputValue: 'timothyAlsike'
				},{
					boxLabel: 'Generic grass', name: 'yield', inputValue: 'genericGrass'
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
					change: function(self, newValue, oldValue) {
						if (newValue != oldValue) {
							DSS_RefilterDelayed(50);
						}
					}
				}
			}]
		});
		
		me.callParent(arguments);
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		let options = {
			crop: me.down('#dss-yield').getValue()['yield']				
		};
		
		return options;
	}
	
});

