
Ext.create('Ext.data.Store', {
    storeId: 'economicAssumptions',
    fields:[ 'name', 'user', 'data'],
    data: [
        { name: 'Default (2008 - 2012 ave)', 	user: false, data: {} },
        { name: '2012 (Expensive Feed)', 		user: false, data: {} },
        { name: '2016 (Expensive Fuel Costs)', 	user: false, data: {} },
        { name: '2018 (Expensive Fertilizer)', 	user: false, data: {} },
        { name: '2030 (Hypothetical future)', 	user: true, data: {} },
    ]
});

Ext.create('Ext.data.Store', {
    storeId: 'laborAssumptions',
    fields:[ 'name', 'user', 'data'],
    data: [
        { name: 'Default (2008 - 2012 ave)', 	user: false, data: {} },
        { name: '2012 (Inexpensive Labor)', 	user: false, data: {} },
        { name: '2030 (Expensive Labor)', 		user: true, data: {} },
    ]
});

Ext.create('Ext.data.Store', {
    storeId: 'climateAssumptions',
    fields:[ 'name', 'user', 'data'],
    data: [
        { name: 'Default (2008 - 2012 ave)', 	user: false, data: {} },
        { name: '2012 (Hot / Wet)', 			user: false, data: {} },
        { name: '2016 (Cold / Wet)', 			user: false, data: {} },
        { name: '2030 (Hot / Dry)', 			user: true, data: {} },
    ]
});

//------------------------------------------------------------------------------
Ext.define('DSS.pages.ManageAssumptionsPage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.manage_assumptions_page',

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
						html: 'Manage Assumptions'
					}]
				},{
					xtype: 'container',
					width: 48,
					padding: 4,
					cls: 'primary-title',
					html: '<i class="fas fa-user"></i>'
				}]
			},{
				//----------------------------------------------
				xtype: 'container',
				flex: 1,
				scrollable: true,
				items: [{
					//----------------------------------------------
					xtype: 'container',
					layout: DSS.utils.layout('vbox', 'center', 'stretch'),
					cls: 'section',
					defaults: {
						margin: 2,
					},
					items: [{
						xtype: 'component',
						cls: 'section-title',
						html: 'Economics'
					},{
						xtype: 'component',
						cls: 'information',
						html: 'External'
					},{
						xtype: 'grid',
						header: false,
						style: 'border-radius: 0',
						store: Ext.data.StoreManager.lookup('economicAssumptions'),
						columns: [
						    { text: 'Name', dataIndex: 'name', flex: 1 },
						    { text: 'User', xtype: 'booleancolumn', dataIndex: 'user' }
						],
					},{
						xtype: 'component',
						cls: 'information',
						html: 'Internal'
					},{
						xtype: 'grid',
						header: false,
						style: 'border-radius: 0',
						store: Ext.data.StoreManager.lookup('laborAssumptions'),
						columns: [
						    { text: 'Name', dataIndex: 'name', flex: 1 },
						    { text: 'User', xtype: 'booleancolumn', dataIndex: 'user' }
						],
					}]
				},{
					//----------------------------------------------
					xtype: 'container',
					layout: DSS.utils.layout('vbox', 'center', 'stretch'),
					cls: 'section',
					defaults: {
						margin: '2rem',
					},
					items: [{
						xtype: 'component',
						cls: 'section-title',
						html: 'Climate'
					},{
						xtype: 'grid',
						header: false,
						style: 'border-radius: 0',
						store: Ext.data.StoreManager.lookup('climateAssumptions'),
						columns: [
						    { text: 'Name', dataIndex: 'name', flex: 1 },
						    { text: 'User', xtype: 'booleancolumn', dataIndex: 'user' }
						],
					}]
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});

