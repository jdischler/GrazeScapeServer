
//------------------------------------------------------------------------------
Ext.define('DSS.controls.FieldShapesBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_base',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'section-title',
				html: 'Field Shapes'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				defaults: {
					xtype: 'component',
					cls: 'information',
				},				
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Create or modify the field shapes for this operation'
				},{
					xtype: 'button',
					text: 'Edit Fields',
					margin: '8 72'
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
