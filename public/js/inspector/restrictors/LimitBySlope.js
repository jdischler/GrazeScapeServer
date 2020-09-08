
//------------------------------------------------------------------------------
Ext.define('DSS.inspector.restrictors.LimitBySlope', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_restrict_by_slope',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_min: 0,
	DSS_step: 5,
	DSS_value: 20,
	DSS_max: 120,
	DSS_compare: 'less-than', // must also correspond to a font-awesome symbol...
	
	DSS_parent: false, // should set
	DSS_active: false,
	
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
					html: "by Slope",
				},{
					xtype: 'component',
					style: 'right: 1px; top: 1px',
					cls: 'accent-text fa-hover ' + (me.DSS_active ? 'to-close' : 'to-add'),
					html: '<i class="fas fa-plus-circle"></i>',
					listeners: {
						render: function(c) {
							c.getEl().getFirstChild().el.on({
								click: function(self) {
									if (c.hasCls('to-add')) {
										c.removeCls('to-add')
										c.addCls('to-close');
										me.DSS_active = true;
										let ct = me.down('#contents');
										ct.animate({
											duration: 300,
											dynamic: true,
											to: {
												height: ct.DSS_height
											}
										});
										DSS_RefilterDelayed(50);
									}
									else {
										c.addCls('to-add')
										c.removeCls('to-close');
										me.DSS_active = false;
										let ct = me.down('#contents');
										ct.animate({
											duration: 250,
											dynamic: true,
											to: {
												height: 0
											}
										});
										DSS_RefilterDelayed(50);
									}	
								}
							});
						}
					}					
				}]
			},{
				xtype: 'container',
				itemId: 'contents',
				DSS_height: 31,
				layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				padding: '0 0 6 0',
				height: 0,
				items: [{
					xtype: 'button',
					width: 32,
					text: '<i class="font-9 fas fa-' + me.DSS_compare + '"></i>',
					tooltip: 'Click to toggle the comparison mode',
					handler: function(self) {
						if (me.DSS_compare == 'less-than') {
							me.DSS_compare = 'greater-than';
						} 
						else {
							me.DSS_compare = 'less-than';
						}
						self.setText('<i class="fas fa-' + me.DSS_compare + '"></i>');
						DSS_Refilter(50);
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
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		if (me.DSS_active) {
			return {
				restrict_by_slope: {
					compare: me.DSS_compare,
					value: me.down('#dss-value').getValue(),
				}
			}
		};
		
	//	return {};
	}
	
});
