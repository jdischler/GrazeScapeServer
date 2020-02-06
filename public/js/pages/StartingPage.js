
DSS.utils.addStyle('.x-mask { background-color: rgba(0,0,0,0.5);}')
DSS.utils.addStyle('.footer-text { background: rgba(0,0,0,0.5);padding: 0.72rem; color: #fff; font-size: 0.8rem; text-align: center}')

DSS.utils.addStyle('.menu-title { padding: 0.5rem; color: #37a; font-size: 1rem; cursor: pointer}');
DSS.utils.addStyle('.menu-title:hover { color: #28c; text-decoration: underline}');

//------------------------------------------------------------------------------
Ext.define('DSS.pages.StartingPage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.starting_page',

	requires: [
		'DSS.controls.LogoBase',
		'DSS.controls.OperationsBase',
		'DSS.controls.CompareOperationsBase',
		'DSS.controls.GrazingToolsBase',
		'DSS.controls.NavigationMenu'
	],
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
//				margin: '2rem',
			},
			items: [{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
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
					xtype: 'logo_base',
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
					xtype: 'operations_base',
				},{
					xtype: 'compare_operations_base'
				},{
					xtype: 'grazing_tools_base'
				}]
			},{
				xtype: 'container',
				items: [{
					xtype: 'component',
					cls: 'footer-text',
					html: 'Footer / Copyright ©2020'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
