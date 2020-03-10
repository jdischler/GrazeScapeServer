//DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');

//------------------------------------------------------------------------------
Ext.define('DSS.controls.Inspector_Limiter', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_limiter',
	
	margin: '8 0 0 0',
	padding: '0 8',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
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
				xtype: 'checkbox',
				itemId: 'dss-filter',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Restrict values to...',
				handler: function(self, checked) {
					me.getOptions();
					me.down('#DSS_filterHider').animate({
						dynamic: true,
						to: {
							height: checked ? 24 : 0
						}
					})
				}
			},{
				xtype: 'container',
				itemId: 'DSS_filterHider',
				height: 0,
				layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				items: [{
					xtype: 'button',
					width: 40,
					text: '<i class="fas fa-' + me.DSS_compare + '"></i>',
					tooltip: 'Click to toggle the comparison mode',
					handler: function(self) {
						
						if (me.DSS_compare == 'less-than') {
							me.DSS_compare = 'greater-than';
						} 
						else {
							me.DSS_compare = 'less-than';
						}
						self.setText('<i class="fas fa-' + me.DSS_compare + '"></i>');
						me.getOptions();
					}
				},{
					xtype: 'numberfield',
					itemId: 'dss-value',
					margin: '0 4',
					minValue: me.DSS_min,
					maxValue: me.DSS_max,
					value: me.DSS_value,
					step: me.DSS_step,
					enableKeyEvents: true,
					width: 72,
					listeners: { 
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
					}
				},{
					xtype: 'component',
					html: me.DSS_units,
					style: 'color: #bbb',
					padding: '2 0'
				}]
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
		
		if (me.down('#dss-filter').getValue()) {
			options['filter'] = {
				compare: me.DSS_compare,
				value: me.down('#dss-value').getValue(),
			};
		}
		me.DSS_InspectorParent.inspectorOptionsChanged(options, silent);
	}
	
});

