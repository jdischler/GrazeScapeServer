
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.Main', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_',
    alternateClassName: 'DSS.FieldShapes',
    singleton: true,	
	
    autoDestroy: false,
    
    requires: [
    	'DSS.field_shapes.DrawAndApply'
    ],
    
    scrollable: 'y',
    
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
				xtype: 'component',
			},
			items: [{
				cls: 'section-title light-text text-drp-20',
				html: 'Field Shapes <i class="fas fa-draw-polygon accent-text text-drp-50"></i>',
				height: 35
			},{
				xtype: 'field_draw_apply'
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	addModeControl: function() {
		let me = this;
		let c = DSS_viewport.down('#DSS-mode-controls');
		
		if (!c.items.has(me)) {
			Ext.suspendLayouts();
				c.removeAll(false);
				c.add(me);
			Ext.resumeLayouts(true);
		}
	}

});
