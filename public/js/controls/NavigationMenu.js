
DSS.utils.addStyle('.menu-title { padding: 0.5rem; color: #37a; font-size: 1rem; cursor: pointer}');
DSS.utils.addStyle('.menu-title:hover { color: #28c; text-decoration: underline}');

DSS.utils.addStyle('.menu-title-disabled { padding: 0.5rem; color: #000; opacity: 0.3; font-size: 1rem; cursor: default}');

//------------------------------------------------------------------------------
Ext.define('DSS.controls.NavigationMenu', {
//------------------------------------------------------------------------------
	extend: 'Ext.menu.Menu',
	alias: 'widget.navigation_menu',

    width: 400,
    padding: '16 48',
    style: 'background:white; box-shadow: 0 8px 12px rgba(0,0,0,0.3); border-radius: 6px',
    modal: true,
    plain: true,
    constrain: false,
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
 
    listeners: {
    	beforehide: function(self) {
			self.animate({
				duration: 300,
				to: {x: -420},
				callback: function() {
					self.destroy()
				}
			});
			self.zIndexManager.mask.animate({
				to: {opacity: 0},
				duration: 300
			});
			return false;
    	}
	},
    
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		let createClickHandlerFor = function(comp, handler) {
			comp.getEl().on({
				click: handler
			})
		};
		
		Ext.applyIf(me, {
		    defaults: {
		    	xtype: 'component',
		    	padding: 8,
		    	cls: 'menu-title'
		    },
			items: [
			{  //--------------------------------------------------------------------------
				style: 'background-image: url("assets/images/graze_logo.png"); background-size: contain; background-repeat: no-repeat;cursor: pointer',
				cls: undefined,
				height: 112,
		        margin: '0 0 0 48',
				listeners: {
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showLandingPage(); me.hide();
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		        html: 'Landing Page',
		        style: 'font-size: 1.2rem; font-weight: bold',
				listeners: { 
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showLandingPage(); me.hide();
						});
					}
				}					
		    },{ //--------------------------------------------------------------------------
		        html: 'New Operation',
		        margin: '0 32',
				listeners: {
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showNewOperationPage(); me.hide();
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		        html: 'Manage Operation',
		        margin: '0 32',
				listeners: {
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showManageOperationPage(); me.hide();
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		        html: 'Manage Field Shapes',
		        margin: '0 32',
				listeners: { 
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showManageFieldsPage(); me.hide();
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		        html: 'Create Scenarios',
		        margin: '0 32',
				listeners: { 
					render: function(c) {
						createClickHandlerFor(c, function() {
							DSS.ApplicationFlow.instance.showManageOperationPage(); me.hide();
						});
					}
				}		
		    },{ //--------------------------------------------------------------------------
		        html: 'Assign Landcover',
		        cls: 'menu-title-disabled',
		        margin: '0 64'
		    },{ //--------------------------------------------------------------------------
		        html: 'Manage Grazing',
		        cls: 'menu-title-disabled',
		        margin: '0 64'
		    },{ //--------------------------------------------------------------------------
		        html: 'Manage Herd',
		        cls: 'menu-title-disabled',
		        margin: '0 64'
		    },{ //--------------------------------------------------------------------------
		        html: 'Compare Scenarios',
		        margin: '0 32',
				listeners: {
					render: function(c) {
						c.getEl().on({
							click: function() {
								var filters = Ext.data.StoreManager.lookup('operationsStore').getFilters();
								filters.removeAll();
								let fil = function(item) {
									return item.data.isOperation == false;
								} 
								filters.add(fil);	
								
								DSS.mainViewport.doChartWorkPanel();
								me.hide();
							}
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		    	html: 'Compare Operations',
		        style: 'font-size: 1.2rem; font-weight: bold',
				listeners: {
					render: function(c) {
						c.getEl().on({
							click: function() {
								var filters = Ext.data.StoreManager.lookup('operationsStore').getFilters();
								filters.removeAll();
								let fil = function(item) {
									return item.data.isOperation == true;
								} 
								filters.add(fil);	
								
								
								DSS.mainViewport.doChartWorkPanel();
								me.hide();
							}
						});
					}
				}	
		    },{ //--------------------------------------------------------------------------
		    	html: 'GrazeScape Toolbox',
		        style: 'font-size: 1.2rem; font-weight: bold'
		    },{ //--------------------------------------------------------------------------
		        html: 'Manage Assumptions',
		        margin: '0 32',
				listeners: { render: function(c) {
					c.getEl().on({ click: function() {
						DSS.mainViewport.doManageAssumptionsPage(); me.hide();
					}});
				}}		
		    },{ //--------------------------------------------------------------------------
		        html: 'Fence Calculator',
		        margin: '0 32'
		    },{ //--------------------------------------------------------------------------
		        html: 'Soil / Slope Lookup',
		        cls: 'menu-title-disabled',
		        margin: '0 32'
		    },{ //--------------------------------------------------------------------------
		    	html: 'Help',
		        cls: 'menu-title-disabled',
		        style: 'font-size: 1.2rem; font-weight: bold'
		    }]
		});
		me.callParent(arguments);
	},
	
	showMenu: function() {
		let me = this;
		
		me.showAt(-400, 10).zIndexManager.mask.animate({
			from: {opacity: 0},
			to: {opacity: 1},
			duration: 300
		});
		me.animate({
			to: {x:-20}, 
			duration: 300
		});
	}

});
