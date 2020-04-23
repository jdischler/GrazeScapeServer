
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
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Create, modify, or upload the field shapes for this operation'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Edit Shapes',
					handler: function(self) {
						DSS.ApplicationFlow.instance.showManageFieldsPage();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
