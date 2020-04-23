
//------------------------------------------------------------------------------
Ext.define('DSS.inspector.RestrictResults', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_restrict_results',
	
	requires: [
		'DSS.inspector.restrictors.LimitValue',
		'DSS.inspector.restrictors.ActiveOperation',
		'DSS.inspector.restrictors.Landcover',
		'DSS.inspector.restrictors.LimitBySlope'			
	],
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				itemId: 'dss-restrictions',
				style: 'background-color: #666; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				margin: '8 4',
				padding: '2 8 10 8',
				defaults: {
					DSS_parent: me,
				},
				items: [{
					xtype: 'component',
					cls: 'information light-text text-drp-20',
					html: 'Restrict Results',
				},{
					xtype: 'inspector_restrict_value'
				},{
					xtype: 'inspector_restrict_landcover'
				},{
					xtype: 'inspector_restrict_by_slope'
				},{
					xtype: 'inspector_restrict_active_operation'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	getRestrictions: function() {
		let me = this;
		let restrictions = {};
		
		let r_items = me.down("#dss-restrictions").items;
		r_items.each(function(item) {
			if (item.getOptions && typeof item.getOptions === 'function') {
				restrictions = Ext.merge(restrictions, item.getOptions())
			}
		})
			
		return restrictions;
	}
	
});
