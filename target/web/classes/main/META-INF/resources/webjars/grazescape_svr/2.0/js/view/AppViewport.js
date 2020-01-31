var DSS_viewport = false;

DSS.utils.addStyle('.x-btn-focus.x-btn-over.x-btn-default-toolbar-small {z-index:2000;overflow: visible;box-shadow: #4297d4 0 1px 0px 0 inset, #4297d4 0 -1px 0px 0 inset, #4297d4 -1px 0 0px 0 inset, #4297d4 1px 0 0px 0 inset, -2px 4px 4px rgba(0,0,0,0.5);}')
DSS.utils.addStyle('.x-btn-default-toolbar-small {box-shadow: -1px 2px 2px rgba(0,0,0,0.25);}')
DSS.utils.addStyle('.x-btn-pressed {z-index:2000; box-shadow: -2px 4px 4px rgba(0,0,0,0.4)!important;}')

//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
		'DSS.app.MainMap',
		'DSS.app.MapLayers',
		'DSS.app.Farm_SelectOrCreate'
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
			Ext.mixin.Responsive.notify();
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'main_map',
				region: 'center'
			},{
				xtype: 'map_layers',
				region: 'south'
			},{
				xtype: 'panel',
				region: 'west',
				title: 'Farm Configuration',
					titleAlign: 'center',
				titleCollapse: false,
				header: {
					titleCollapse: false,
					padding: '64 8 8 32',
					title: 'Farm Configuration',
				},
				collapsible: true,
				bodyPadding: '0 0 0 48',
				width: 388,
				minWidth: 388,
				listeners:  {
					beforecollapse: function() {
						me.DSS_logoImage.animate({
							duration: 300,
							to: { x: 40, width: 150, height: 30, y:2, opacity: 0.75 }
						})
						me.DSS_toolbar.animate({
							duration: 300,
							to: { x:-80 }
						})
					},

				},
				items: [{
					xtype: 'farm_select_or_create'
				}]
			}]
		});

		me.createLogo();
		me.createFloatingTools();
		
		me.callParent(arguments);
		DSS_viewport = me;
	},
	
	//--------------------------------------------------------------------------
	createLogo: function() {
		let me = this;
		
		me.DSS_logoImage = Ext.create('Ext.Img', {
			xtype: 'image',
			src: '/assets/images/graze-logo.png',
			style: 'pointer-events: none',
			width: 250,
			height: 50,
			floating: true,
			x: 68, y: 6,
			shadow: false
		}).show();
		//me.DSS_logoImage.show();		
	},
	
	//--------------------------------------------------------------------------
	createFloatingTools: function() {
		let me = this;
		
		me.DSS_toolbar = Ext.create('Ext.toolbar.Toolbar', {
	        style: 'background-color: transparent; overflow: visible',
	        border: false,
			width: 74,
			floating: true,
			shadow: false,
			x: -16,
			y: 94,
		    vertical: true,
		    defaults: {
		        text: 'B&nbsp;&nbsp;',
		        margin: 0,
		        toggleGroup: 'true',
		        allowDepress: false,
		        margin: '0 12 0 -12',
		        padding: '0 4 0 16',
		        width: 68, height: 64,
		        style: 'overflow: visible;border-top-right-radius: 6px!important; border-bottom-right-radius: 6px!important;',
		    },
		    items: [{
		    	text: 'Farm',
		    	pressed:true,margin: 0,
			        listeners: {
			            toggle: function(self, pressed) {
			                self.animate({
			                    duration: 125,
			                    to: {
			                        left: pressed ? 0 : -12
			                    }
			                })
			            }
			        }
		        },{
			    	text: 'Crop',
			        listeners: {
			            toggle: function(self, pressed) {
			                self.animate({
			                    duration: 125,
			                    to: {
			                        left: pressed ? 0 : -12
			                    }
			                })
			            }
			        }
		        },{
			    	text: 'Cows',
			        listeners: {
			            toggle: function(self, pressed) {
			                self.animate({
			                    duration: 125,
			                    to: {
			                        left: pressed ? 0 : -12
			                    }
			                })
			            }
			        }
		        },{
			    	text: 'Clim',
			        listeners: {
			            toggle: function(self, pressed) {
			                self.animate({
			                    duration: 125,
			                    to: {
			                        left: pressed ? 0 : -12
			                    }
			                })
			            }
			        }
		        },{
			    	text: 'Assm',
			        listeners: {
			            toggle: function(self, pressed) {
			                self.animate({
			                    duration: 125,
			                    to: {
			                        left: pressed ? 0 : -12
			                    }
			                })
			            }
			        }
		        },
		    ]
		});
		
		me.DSS_toolbar.show();
	}
		
});
