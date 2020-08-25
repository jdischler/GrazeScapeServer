
// TODO: move to a better place...
let getToggle = function(owner, stateRef, activatedHandler, deactivatedHandler) {
	
	return {
		xtype: 'component',
		style: 'right: 1px; top: 2px',
		cls: 'accent-text fa-hover',
		html: '<i class="far fa-plus-circle"></i>',
		listeners: {
			afterrender: function(c) {
				let vm = owner.lookupViewModel();
				let active = vm.get(stateRef) ;
				c.addCls(active ? 'to-close' : 'to-add')
				
				let ct = owner.down('#contents');
				let ht = owner.DSS_sectionHeight;
				ct.setHeight(active ? ht : 0);
				
				c.getEl().getFirstChild().el.on({
					click: function(self) {
						if (c.hasCls('to-add')) {
							c.removeCls('to-add')
							c.addCls('to-close');
							vm.set(stateRef, true);
							ct.animate({
								duration: 300,
								dynamic: true,
								to: {
									height: ht
								}
							});
							if (typeof activatedHandler === 'function') {
								activatedHandler.call()
							}
						}
						else {
							c.addCls('to-add')
							c.removeCls('to-close');
							vm.set(stateRef, false);
							ct.animate({
								duration: 300,
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
	
	DSS_sectionHeight: 28,
	
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
					getToggle(me, 'manure.is_active')
				]
			},{
				xtype: 'container',
				itemId: 'contents',
				layout: 'center',
				padding: '0 0 6 0',
				items: [{
					xtype: 'numberfield',
					itemId: 'dss-soil-p',
					fieldLabel: 'Tons / acre',
					labelWidth: 90,
					labelAlign: 'right',
					bind: { value: '{manure.value}' },
					minValue: 1,
					maxValue: 200,
					width: 160,
					step: 5
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
