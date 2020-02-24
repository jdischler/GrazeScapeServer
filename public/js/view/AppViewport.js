var DSS_viewport = false;

DSS.utils.addStyle('.x-btn-focus.x-btn-over.x-btn-default-toolbar-small {z-index:2000;overflow: visible;box-shadow: #4297d4 0 1px 0px 0 inset, #4297d4 0 -1px 0px 0 inset, #4297d4 -1px 0 0px 0 inset, #4297d4 1px 0 0px 0 inset, -2px 4px 4px rgba(0,0,0,0.5);}')
DSS.utils.addStyle('.x-btn-default-toolbar-small {box-shadow: -1px 2px 2px rgba(0,0,0,0.25);}')
DSS.utils.addStyle('.x-btn-pressed {z-index:2000; box-shadow: -2px 4px 4px rgba(0,0,0,0.4)!important;}')
DSS.utils.addStyle('.x-btn-inner-default-small {font-size: 1rem}');

//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
		'DSS.app.MainMap',
		'DSS.app.MapLayers',
		'DSS.pages.CompareOperationsPage',
		'DSS.pages.ManageAssumptionsPage',
		'DSS.controls.ApplicationFlow',
	],

	minWidth: 480,
	minHeight: 240,
	style: 'background-color: #fff',
	
	scrollable: false,
    renderTo: Ext.getBody(),
	layout: 'border',
	
	listeners: {
		afterrender: function(self) {
			// FIXME: TODO: responsive layouts weren't working until a page resize. why?
			// This also doesn't seem to do anything anyway?
		//	Ext.mixin.Responsive.notify();
			
			DSS.mainViewport = self;
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				region: 'center',
				layout: 'fit',
				items: [{
					xtype: 'container',
					autoDestroy: false,
					layout: 'border',
					items: [{
						xtype: 'main_map',
						region: 'center',
					},{					
						xtype: 'component',
						width: 0,
						style: ' background: white; background-image: url("assets/images/fake_dash.png"); background-size: contain; background-repeat: no-repeat;',
						region: 'east',
						listeners: {
							afterrender: function(self) { me.DSS_realtimeDashboard = self; }
						}
					},{					
						xtype: 'map_layers',
						region: 'south'
					}],
					listeners: {
						afterrender: function(self) { me.DSS_MapWidget = self; }
					}
				}],
				listeners: {
					afterrender: function(self) { me.DSS_WorkContainer = self; }
				}
			},{
				xtype: 'container',
				region: 'west',
				style: 'background: #ede9d9',
				width: 388,
				layout: 'fit',
				minWidth: 388,
				items: [{
					xtype: 'application_flow',
					listeners: {
						afterrender: function(self) { me.DSS_App = {Flow: {Content: self}} }
					}
				}],
				listeners: {
					afterrender: function(self) { me.DSS_App = {Flow: {Container: self}} }
				}
			}]
		});

		me.callParent(arguments);
		DSS_viewport = me;	
	},
	
	doChartWorkPanel: function() {
		let me = this;
		
		if (!me.DSS_ChartWidget) {
			me.DSS_ChartWidget = Ext.create({xtype: 'compare_operations_page'});
		} else if (me.DSS_ChartWidget.isResident) {
			return;
		}
		
		me.DSS_WorkContainer.remove(me.DSS_MapWidget,false);
		me.DSS_WorkContainer.add(me.DSS_ChartWidget)
		
		me.DSS_ChartWidget.isResident = true;
		me.DSS_MapWidget.isResident = false;
	},

	doMapWorkPanel: function() {
		let me = this;
		
		if (me.DSS_MapWidget.isResident) {
			return;
		}
		
		me.DSS_WorkContainer.remove(me.DSS_ChartWidget, false)
		me.DSS_WorkContainer.add(me.DSS_MapWidget);
	},
	
});
