var DSS_viewport = false;

DSS.utils.addStyle('.x-btn-focus.x-btn-over.x-btn-default-toolbar-small {z-index:2000;overflow: visible;box-shadow: #4297d4 0 1px 0px 0 inset, #4297d4 0 -1px 0px 0 inset, #4297d4 -1px 0 0px 0 inset, #4297d4 1px 0 0px 0 inset, -2px 4px 4px rgba(0,0,0,0.5);}')
DSS.utils.addStyle('.x-btn-default-toolbar-small {box-shadow: -1px 2px 2px rgba(0,0,0,0.25);}')
DSS.utils.addStyle('.x-btn-pressed {z-index:2000; box-shadow: 0 4px 6px rgba(0,0,0,0.4)!important;}')
DSS.utils.addStyle('.x-btn-inner-default-small {font-size: 1rem}');

//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
		'DSS.map.Main',
		'DSS.state.ApplicationFlow',
		
		'DSS.inspector.Main',
		
		'DSS.field_shapes.DrawAndApply',
		'DSS.field_shapes.Split',
		'DSS.field_shapes.Join',
		'DSS.field_shapes.Delete',
		
		'DSS.results.ResultsMain'		
	],

	minWidth: 900,
	minHeight: 480,
	style: 'background-color: #000',

	scrollable: false,
    renderTo: Ext.getBody(),
	layout: 'border',
	
	listeners: {
		afterrender: function(self) {
			// FIXME: TODO: responsive layouts weren't working until a page resize. why?
			// This also doesn't seem to do anything anyway?
		//	Ext.mixin.Responsive.notify();
			
			DSS.mainViewport = self;
		},
		resize: function(self, newWidth, newHeight, oldWidth, oldHeight) {
			AppEvents.triggerEvent('viewport_resize', {w: newWidth, h: newHeight})
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
					autoDestroy: false,
					listeners: {
						afterrender: function(self) { 
							me.DSS_MapWidget = self;
							//self.instantiateMap()
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
				width: 280,
				layout: 'fit',
				minWidth: 280,
				items: [{
					xtype: 'application_flow',
				}]
			},{
				xtype: 'container',
				region: 'east',
				cls: 'info-panel',
				resizable: {
					dynamic: true,
					maxWidth: 420,
				},
				resizeHandles: 'w',
				width: 280,
				itemId: 'DSS-mode-controls',
				maxWidth: 420,
				padding: '8 6',
				scrollable: 'y',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				items: [
					DSS.Inspector // Directly add the singleton instance...
				],
				listeners: {
					afterrender: function(self) {
						let targetSize = 280;
						setTimeout(function() {
							self.animate({
								dynamic: true,
								to: {
									width: targetSize
								},
								callback: function() {
									self.setMinWidth(targetSize);
									self.setWidth(targetSize);
									// ooof, the Ext resizer doesn't seem to realize when its resize target has a min/max width change
									self.resizer.resizeTracker.minWidth = targetSize;
								}
							})
						}, 10);
					}
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
		
		/*setTimeout(function() {
			Ext.create('DSS.results.ResultsMain').show().center().maximize(true);
		}, 3000);*/
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
