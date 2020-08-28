

DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');
DSS.utils.addStyle('.box-label-cls {color: #eee; text-shadow: 0 1px rgba(0,0,0,0.2),1px 0 rgba(0,0,0,0.2); font-size: 0.9rem}');
DSS.utils.addStyle('.small {  font-size: 1rem}');
DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');
DSS.utils.addStyle('.drop {overflow: visible!important}');
DSS.utils.addStyle('.drop:after {overflow: visible!important; display: block; position: absolute; bottom: -8px; left: calc(50% - 8px); content: ""; background-color: transparent; border-top: 8px solid #666; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent;}');

DSS.utils.addStyle('.accent-text { color: #48b;}')
DSS.utils.addStyle('.light-text { color: #ddd;}')
DSS.utils.addStyle('.med-text { color: #999;}')
DSS.utils.addStyle('.text-drp-20 { text-shadow: 0 1px rgba(0,0,0,0.2)}');
DSS.utils.addStyle('.text-drp-50 { text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2)}');
DSS.utils.addStyle('.font-10 { font-size: 1rem }');
DSS.utils.addStyle('.font-9 { font-size: 0.9rem }');
DSS.utils.addStyle('.bold { font-weight: bold}');

DSS.utils.addStyle('.section-title { padding: 0.5rem; font-size: 1.2rem; text-align: center; font-weight: bold}');

DSS.utils.addStyle('.x-mask { background-color: rgba(102,102,102,0.6);}')
DSS.utils.addStyle('.footer-text {border-top: 1px solid rgba(0,0,0,0.15); background: rgba(0,0,0,0.5);padding: 0.72rem; color: #fff; font-size: 0.8rem; text-align: center}')

DSS.utils.addStyle('.button-margin { margin: 0.5rem 1.75rem 0.75rem;}')
DSS.utils.addStyle('.button-text-pad { padding: 0.33rem;}')

DSS.utils.addStyle('.information { padding: 0.5rem 0 0.25rem 0; font-size: 0.9rem; text-align: center}')
DSS.utils.addStyle('.section-title { padding: 0.5rem; font-size: 1.2rem; text-align: center; font-weight: bold}');
DSS.utils.addStyle('.section { margin: 0.5rem; margin-bottom: 1rem; padding: 0.75rem; background-color: #fff; border: 1px solid #bbb; border-radius: 0.3rem; box-shadow: 0px 4px 8px rgba(0,0,0,0.25) }')

DSS.utils.addStyle('.back-button { padding: 0.23rem; color: rgba(34,114,204,0.5); font-size: 1.25rem; cursor: pointer;}');
DSS.utils.addStyle('.back-button:hover { color: rgba(40,120,220,1); text-shadow: 0 1px 0 rgba(0,0,0,0.8)');

// Section that roughly corresponds to the left portion of the application. This area will contain logos, titles, controls, etc
//	and generally be the starting point/container for controlling the entire application flow...whereas the remainder of the
//	application space will contain (primarily) the map display but will be switched out as needed for charts/reports/other.

//------------------------------------------------------------------------------
Ext.define('DSS.state.ApplicationFlow', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.application_flow',

	requires: [
		'DSS.state.MapStateTools',
		'DSS.state.BrowseOrCreate',
		'DSS.state.CreateNew',
		'DSS.state.Manage',
		'DSS.state.Scenario'
	],
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),

	DSS_minTitleHeight: 64,
	
	listeners: {
		afterrender: function(self) {
			self.showLandingPage()
		}
	},
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		DSS['ApplicationFlow'] = {instance: me};
		me.DSS_App = {};
		
		if (!DSS['viewModel']) DSS['viewModel'] = {}
		
		DSS.viewModel.master = new Ext.app.ViewModel({
			formulas: {
				browse_or_create: {
					bind: '{farm_count}',
					get: function(ct) {
						if (ct < 1) {
							return 'Start by creating a new operation'
						}
						return 'Select <i class="accent-text fas fa-hand-pointer"></i>' + 
						' an operation on the map... or create a new one'
					}
				}
			},
			data: {
				farm_count: 0
			}
		});
		
		me.setViewModel(DSS.viewModel.master);

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'start', 'stretch'),
				
				// Top Section (Logo, Titles, MenuWidgets, and general application flow controls)
				//----------------------------------------------------------------------------------
				items: [{
					xtype: 'component',
					cls: 'accent-text section-title',
					padding: '4 8',
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
					xtype: 'component',
					flex: 1,
					height: 114, margin: '8 8 0 -12',
					style: 'background-image: url("assets/images/graze_logo.png"); background-size: contain; background-repeat: no-repeat',
						
/*				},{
					xtype: 'component',
					padding: '4 8',

					cls: 'accent-text section-title',
					html: '<i class="fas fa-user"></i>'*/
				}]
			},{
				// Container for controls necessary at each step in the application flow
				//----------------------------------------------------------------------------------
				xtype: 'container',
				scrollable: true,
				flex: 1,
				listeners: {
					afterrender: function(self) { me.DSS_App['FlowContainer'] = self }
				}			
			},{
				// Footer, possibly should hide when not on the "landing" portion of the grazescape application 
				//----------------------------------------------------------------------------------
				xtype: 'component',
				cls: 'footer-text',
				html: 'GrazeScape<br>Footer / Copyright ©2020'
			}]
		});
		
		me.callParent(arguments);

		//----------------------------------------------------------------------------------
		window.onhashchange = function() {
			event.preventDefault();
			event.stopPropagation();
		}		
	},
	
	//----------------------------------------------------------------------------------
	setControlBlock: function(extDef) {
		let me = this;
		me.DSS_App.FlowContainer.removeAll();
		me.DSS_App.FlowContainer.add(extDef);
	},
	
	// SHOW Handling -------------------------------------------------------------------
	//
	//----------------------------------------------------------------------------------
	showLandingPage: function() {
		let me = this;

		
		Ext.suspendLayouts();
			me.setControlBlock({xtype:'operation_browse_create'});
		Ext.resumeLayouts(true);
		
		DSS.mouseMoveFunction = DSS.MapState.mouseoverFarmHandler();
		DSS.mapClickFunction = DSS.MapState.clickActivateFarmHandler();
		DSS.MapState.zoomToExtent();
		
		DSS.MapState.disableFieldDraw();
		
		DSS.layer.farms.setVisible(true);
		DSS.layer.farms.setOpacity(1);
		DSS.layer.markers.setVisible(false);
		
	
		return;
		if (!DSS.dialogs) DSS.dialogs = {};
		if (!DSS.dialogs.AnimalDialog) {
			DSS.dialogs.AnimalDialog = Ext.create('DSS.state.scenario.AnimalDialog'); 
		}
		DSS.dialogs.AnimalDialog.show().center();
	},
	
	//----------------------------------------------------------------------------------
	showNewOperationPage: function() {
		let me = this;
		
		Ext.suspendLayouts();
			me.setControlBlock({xtype:'operation_create'});
			DSS.mouseMoveFunction = DSS.MapState.mouseoverFarmHandler();
			DSS.layer.farms.setOpacity(0.5);
		Ext.resumeLayouts(true);
	},
	
	//----------------------------------------------------------------------------------
	showManageOperationPage: function(operationName) {
		let me = this;
		
//		operationName = operationName || "Grazing Acres";
		
		Ext.suspendLayouts();
			me.setControlBlock([	
				DSS.OperationManage.get()
			]);
		Ext.resumeLayouts(true);
		
		DSS.mouseMoveFunction = undefined;
		DSS.layer.farms.setVisible(false);
		
		DSS.MapState.showFieldsForFarm(DSS.activeFarm);
		
		DSS.popupOverlay.setPosition(false);
	},

	//----------------------------------------------------------------------------------
	showScenarioPage: function(operationName) {
		let me = this;
		
		Ext.suspendLayouts();
			me.setControlBlock([	
				DSS.StateScenario.get()
			]);
		Ext.resumeLayouts(true);
	},
	
	//----------------------------------------------------------------------------------
	showManageFieldsPage: function() {
		let me = this;
		
		Ext.suspendLayouts();
			me.setControlBlock([
				{xtype: 'field_shape_mgr'}
			]);
		Ext.resumeLayouts(true);
	}
	
	
	/* If and when needed....
	 
	me.DSS_realtimeDashboard.animate({
		dynamic: true, duration: 300,
		to: {
			width: 0
		}
	})
	
	DSS.map.getView().fit([-10126000, 5360000, -10110000, 5390000], {duration: 1000});
},*/

	
});
