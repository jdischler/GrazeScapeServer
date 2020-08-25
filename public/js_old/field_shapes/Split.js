
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.Split', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_split',
    alternateClassName: 'DSS.SplitFieldShapes',
    singleton: true,	
	
    autoDestroy: false,
    
    scrollable: 'y',

	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				cls: 'section-title light-text text-drp-20',
				html: 'Field Shapes <i class="fas fa-cut fa-fw accent-text text-drp-50"></i>',
				height: 35
			},{
				xtype: 'container',
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
					html: 'Split Field',
				},{
					xtype: 'component',
					cls: 'information light-text text-drp-20',
					html: 'Click a single field to begin',
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Split',
					disabled: true
				}]
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
