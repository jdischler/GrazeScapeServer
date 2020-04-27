
// TODO: move to a better place...
let getToggle = function(owner, activatedHandler, deactivatedHandler) {
	
	return {
		xtype: 'component',
		itemId: 'dss-toggle-active',
		style: 'right: 1px; top: 1px',
		cls: 'accent-text fa-hover ' + (owner.DSS_active ? 'to-close' : 'to-add'),
		html: '<i class="far fa-plus-circle"></i>',
		listeners: {
			render: function(c) {
				c.getEl().getFirstChild().el.on({
					click: function(self) {
						if (c.hasCls('to-add')) {
							c.removeCls('to-add')
							c.addCls('to-close');
							owner.DSS_active = true;
							let ct = owner.down('#contents');
							ct.animate({
								duration: 300,
								dynamic: true,
								to: {
									height: ct.DSS_height
								}
							});
							if (typeof activatedHandler === 'function') {
								activatedHandler.call()
							}
						}
						else {
							c.addCls('to-add')
							c.removeCls('to-close');
							owner.DSS_active = false;
							let ct = owner.down('#contents');
							ct.animate({
								duration: 250,
								dynamic: true,
								to: {
									height: 0
								}
							});
							if (typeof deactivatedHandler === 'function') {
								deactivatedHandler.call()
							}
						}	
					}
				});
			}
		}					
	}	
}
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.SpreadManure', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_manure',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_parent: false, // should set
	DSS_sectionHeight: 31,
	
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
					html: "Spread Manure",
				},
					getToggle(me)
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				DSS_height: me.DSS_sectionHeight,
				layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				padding: '0 0 6 0',
				height: (me.DSS_active ? me.DSS_sectionHeight : 0),
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-soil-p',
					fieldLabel: 'Soil test value',
					labelWidth: 90,
					labelAlign: 'right',
					value: 32,
					minValue: 1,
					maxValue: 200,
					width: 150,
					step: 5
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		if (me.DSS_active && !me.isHidden()) {
			return {
				restrict_to_fields: {
					// FIXME:
					farm_id: DSS.activeFarm,
					aggregate: me.down('#aggregate').getValue()
				}
			}
		};
		
//		return {};
	}
	
});
