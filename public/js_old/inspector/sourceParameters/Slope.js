
DSS.utils.addStyle('.source-parameters { background: #fff; border-radius: 2px;border-bottom-left-radius: 4px; border-bottom-right-radius: 4px}');

//------------------------------------------------------------------------------
Ext.define('DSS.inspector.sourceParameters.Slope', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_slope',
	
	cls: 'source-parameters',
	margin: '0 0 4 0',
	padding: '4 4 4 16',
	
	layout: 'fit',//DSS.utils.layout('vbox', 'start', 'center'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'radiogroup',
				itemId: 'dss-slope-parm',
				vertical: true,
				columns: 1,
				listeners: {
					change: function(self, newValue, oldValue) {
						if (newValue != oldValue) {
							DSS_RefilterDelayed(50);
						}
					}
				},
				items: [{
					boxLabel: 'as %', name: 'slope', inputValue: true, checked: true,
				},{
					boxLabel: 'as º (degrees)', name: 'slope', inputValue: false,
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		return {
			slope_as_percent: me.down('#dss-slope-parm').getValue()['slope'],
		}
	}
	
});
