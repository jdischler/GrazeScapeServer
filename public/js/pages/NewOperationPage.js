
DSS.utils.addStyle('.underlined-input { border: none; border-bottom: 1px solid #ddd; display:table; width: 100%; height:100%; padding: 0 0 2px}')   
DSS.utils.addStyle('.underlined-input:hover { border-bottom: 1px solid #7ad;}')
DSS.utils.addStyle('.x-form-text-default {font-size: 1rem}')
DSS.utils.addStyle('.x-form-text-default:focus { background:#ebf4f7; border-top-left-radius: 4px; border-top-right-radius: 4px}')


//------------------------------------------------------------------------------
Ext.define('DSS.pages.NewOperationPage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.new_operation_page',

	requires: [
		'DSS.controls.TitleBase',
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
					xtype: 'container',
					flex: 1,
					layout: DSS.utils.layout('vbox', 'center', 'stretch'),
					padding: 4,
					items: [{
						xtype: 'component',
						cls: 'primary-title',
						margin: '2rem',
						html: 'New Operation'
					}]
				},{
					xtype: 'container',
					width: 48,
					padding: 4,
					cls: 'primary-title',
					html: '<i class="fas fa-user"></i>'
				}]
			},{
				xtype: 'container',
				flex: 1,
				scrollable: true,
				items: [{
					xtype: 'container',
					layout: DSS.utils.layout('vbox', 'center', 'stretch'),
					cls: 'section',
					defaults: {
						margin: '2rem',
					},
					items: [{
						xtype: 'component',
						cls: 'information',
						html: 'Select a location for this operation on the map'
					},{
						xtype: 'container',
						margin: '8 0',
						defaults: {
							xtype: 'textfield',
							labelAlign: 'right',
							labelWidth: 80,
							triggerWrapCls: 'underlined-input',
							width: 280,
							margin: '12 0',
							padding: 4,
						},
						items: [{
							fieldLabel: 'Operation',
						},{
							fieldLabel: 'Owner',
						},{
							fieldLabel: 'Address',
						}]
					},{
						xtype: 'button',
						text: 'Create',
						margin: '12 72',
						handler: function() {
							DSS.mainViewport.doOperationBasePage();
						}
					}]
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});

