
DSS.utils.addStyle('.fa-hover {opacity: 0.3; right: 1rem}');
DSS.utils.addStyle('.fa-hover:hover {opacity: 1; cursor: pointer}');
DSS.utils.addStyle('.to-close {width: 1rem; height: 1rem; font-size: 1rem; color: red; transform: translate(0px,0px) rotate(-45deg); transition: color .3s, transform 0.3s }')
DSS.utils.addStyle('.to-add {width: 1rem; height: 1rem; font-size: 1rem; color: #48b; transform: translate(0px,0px) rotate(0deg); transition: color .3s, transform 0.3s }')
DSS.utils.addStyle('.restriction-widget { background: #fff; border-radius: 0.6rem; border-top-left-radius: 2px}');

//------------------------------------------------------------------------------
Ext.define('DSS.inspector.restrictors.ActiveOperation', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_restrict_active_operation',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_parent: false, // should set
	DSS_active: true,
	DSS_sectionHeight: 31,
	hidden: true,
	
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
					html: "to Operation Fields",
				},{
					xtype: 'component',
					itemId: 'dss-toggle-active',
					style: 'right: 1px; top: 1px',
					cls: 'accent-text fa-hover ' + (me.DSS_active ? 'to-close' : 'to-add'),
					html: '<i class="fas fa-plus-circle"></i>',
					listeners: {
						render: function(c) {
							let el = c.getEl().getFirstChild().el;
							el.on('click', function() {
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
							}, {passive: false});
						}
					}					
				}]
			},{
				xtype: 'container',
				itemId: 'contents',
				DSS_height: me.DSS_sectionHeight,
				layout: DSS.utils.layout('hbox', 'center'),//, 'stretch'),
				padding: '0 0 6 0',
				height: (me.DSS_active ? me.DSS_sectionHeight : 0),
				items: [{
					xtype: 'checkbox',
					itemId: 'aggregate',
					boxLabel: 'aggregate fields',
					checked: false,
					handler: function() {
						DSS_RefilterDelayed(50);
					}
				}]
			}]
		});
		
		me.callParent(arguments);
		
		AppEvents.registerListener("activate_operation", function() {
			me.setHidden(false);
			DSS_RefilterDelayed(50);
		})
		AppEvents.registerListener("deactivate_operation", function() {
			me.setHidden(true);
			DSS_RefilterDelayed(50);
		})
	},
	
	//------------------------------------------------------------
	getOptions: function() {
		let me = this;
		
		if (me.DSS_active && !me.isHidden()) {
			return {
				restrict_to_operation: {
					aggregate: me.down('#aggregate').getValue()
				}
			}
		};
	}
	
});
