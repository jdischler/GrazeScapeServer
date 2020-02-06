
//------------------------------------------------------------------------------
Ext.define('DSS.pages.OperationBasePage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.operation_base_page',

	requires: [
		'DSS.controls.TitleBase',
		'DSS.controls.FieldShapesBase',
		'DSS.controls.ScenarioBuilder'
	],
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center', 'stretch'),
				items: [{
					xtype: 'container',
					width: 48,
					padding: 4,
					cls: 'primary-title',
					html: '<i class="fas fa-bars"></i>',
					listeners: {
						render: function(c) {
							c.getEl().getFirstChild().el.on({
								click: function() {
									Ext.create({xtype: 'navigation_menu'}).showMenu();
								}
							});
						}
					}	
				},{
					xtype: 'title_base',
					flex: 1,
				},{
					xtype: 'container',
					width: 48,
					padding: 4,
					cls: 'primary-title',
					html: '<i class="fas fa-user"></i>'
				}]
			},{
				xtype: 'container',
				scrollable: true,
				flex: 1,
				items: [{
					xtype: 'field_shapes_base'
				},{
					xtype: 'scenario_builder'
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});

