var DSS_viewport = false;

DSS.utils.addStyle('.x-btn-focus.x-btn-over.x-btn-default-toolbar-small {z-index:2000;overflow: visible;box-shadow: #4297d4 0 1px 0px 0 inset, #4297d4 0 -1px 0px 0 inset, #4297d4 -1px 0 0px 0 inset, #4297d4 1px 0 0px 0 inset, -2px 4px 4px rgba(0,0,0,0.5);}')
DSS.utils.addStyle('.x-btn-default-toolbar-small {box-shadow: -1px 2px 2px rgba(0,0,0,0.25);}')
DSS.utils.addStyle('.x-btn-pressed {z-index:2000; box-shadow: 0 4px 6px rgba(0,0,0,0.4)!important;}')
DSS.utils.addStyle('.x-btn-inner-default-small {font-size: 1rem}');

DSS.utils.addStyle('.layer-menu { padding: 0.23rem; color: #27c; font-size: 1.25rem; cursor: pointer; text-shadow: 0 1px 0 rgba(0,0,0,0.5), -1px 0 rgba(0,0,0,0.3), 0 0 6px rgba(0,0,0,0.4)}');
DSS.utils.addStyle('.layer-menu:hover { color: #48f; text-shadow: 0 2px 2px rgba(0,0,0,0.8), 1px 0 rgba(0,0,0,0.5), -1px 0 rgba(0,0,0,0.5), 0 0 6px rgba(0,0,0,0.4)}');

//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
		'DSS.data.ApplicationState',
		'DSS.map.MainMap',
		'DSS.map.LayerMenu',
		'DSS.pages.CompareOperationsPage',
		'DSS.pages.ManageAssumptionsPage',
		'DSS.controls.ApplicationFlow',
		'DSS.controls.ApplicationState',
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
					xtype: 'main_map',
//					xtype: 'container',
					autoDestroy: false,
//					layout: 'border',
//					items: [{
//						xtype: 'main_map',
//						region: 'center',
//					}],
					listeners: {
						afterrender: function(self) { 
							me.DSS_MapWidget = self;
							self.instantiateMap()
						}
					}
				}],
				listeners: {
					afterrender: function(self) { me.DSS_WorkContainer = self; }
				}
			},{
				xtype: 'container',
				region: 'west',
				style: 'background: #ede9d9; border-right: 1px solid rgba(0,0,0,0.25);',
				width: 320,
				layout: 'fit',
				minWidth: 320,
				items: [{
					xtype: 'application_flow',
					listeners: {
				//		afterrender: function(self) { me.DSS_App = {Flow: {Content: self}} }
					}
				}],
				listeners: {
				//	afterrender: function(self) { me.DSS_App = {Flow: {Container: self}} }
				}
			}]
		});

		me.callParent(arguments);
		DSS_viewport = me;
		
		// Terrible image cache
		Ext.create('Ext.container.Container', {
			style: 'background:url("assets/images/graze_logo.png")',
			width: 1, height: 1,
			floating: true, 
			shadow: false,
		}).showAt(-1,-1);
		
		Ext.create('Ext.Component', {
			floating: true,
			shadow: false,
			cls: 'layer-menu',
			tooltip: 'Access map layers',
			html: '<i class="far fa-layer-group"></i>',//caret-square-down"></i>',
			listeners: {
				render: function(c) {
					c.getEl().getFirstChild().el.on({
						click: function(self) {
							let rect = self.target.getBoundingClientRect();
							Ext.create('DSS.map.LayerMenu').showAt(rect.left, rect.top);
						}
					});
				}
			}					
		}).showAt(322,2);
		
		Ext.create('DSS.controls.ApplicationState', {id: 'crap-state'}).showAt(400,-4);
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
		
		me.DSS_ChartWidget.isResident = false;
		me.DSS_MapWidget.isResident = true;
	},
	
});
