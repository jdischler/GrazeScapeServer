
//------------------------------------------------------------------------------
Ext.define('DSS.map.LayerMenu', {
//------------------------------------------------------------------------------
	extend: 'Ext.menu.Menu',
	alias: 'widget.map_layer_menu',
	
	header: {
		style: 'background: rgba(200,200,200,0.9)',
		padding: 2
	},
	closable: true,
	plain: true,
	width: 160,
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		let makeOpacityMenu = function(openLayersLayer) {
			return {
                width: 130,
                plain: true,
            	listeners: {
            		show: function(menu) {
            			menu.down('#dss-slider').setValue(openLayersLayer.getOpacity() * 100, false)
            		},
            	},                    
                items: [{
    				xtype: 'menuitem',
    				text: 'Opacity', disabled: true,
    				style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
                },{
                	xtype: 'slider',
                	itemId: 'dss-slider',
                	hideEmptyLabel: true,
                	increment: 10,
                	value: 60,
                	minValue: 20, 
                	maxValue: 100,
                	listeners: {
                		change: function(slider, newValue, thumb, eOpts) {
                			openLayersLayer.setOpacity(newValue / 100.0)
                		}	                		
                	}
                }]
            }			
		};

		Ext.applyIf(me, {
			defaults: {
				xtype: 'menucheckitem',
				padding: 2,
                hideOnClick: false,
			},
			items: [{
				xtype: 'menuitem',
				text: 'Base Layer', disabled: true,
				style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
			},{
				xtype: 'radiogroup',
				columns: 1, 
				vertical: true,
				defaults: {
					padding: '2 0',
					group: 'base-layer'
				},
				items: [{ 
					boxLabel: 'Base Layer', 
	                checked: true,
	                text: 'Bing Aerial',
	                DSS_layer: 'bing-aerial',
	                listeners: {
	                	afterrender: function(self) {
	                		self.setValue(DSS.layer.bingAerial.getVisible());
	                	}
	                },
	                handler: function(self, checked) {
	                	DSS.layer.bingAerial.setVisible(checked);
	                	if (checked) Ext.util.Cookies.set("baselayer:visible", "1");	                	
	                }
				},{
					boxLabel: 'Bing Road',
	                DSS_layer: 'bing-aerial',
	                listeners: {
	                	afterrender: function(self) {
	                		self.setValue(DSS.layer.bingRoad.getVisible());
	                	}
	                },
	                handler: function(self, checked) {
	                	DSS.layer.bingRoad.setVisible(checked);
	                	if (checked) Ext.util.Cookies.set("baselayer:visible", "2");	                	
	                }
				},{
					boxLabel: 'Open-Street',
	                DSS_layer: 'bing-aerial',
	                listeners: {
	                	afterrender: function(self) {
	                		self.setValue(DSS.layer.osm.getVisible());
	                	}
	                },
	                handler: function(self, checked) {
	                	DSS.layer.osm.setVisible(checked);
	                	if (checked) Ext.util.Cookies.set("baselayer:visible", "3");	                	
	                }
				}]
			},{ //-------------------------------------------
				xtype: 'menuitem',
				text: 'Overlays', disabled: true,
				style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
			},{ //-------------------------------------------
				text: 'Watershed',
                checked: true,
                menu: makeOpacityMenu(DSS.layer.watershed),
                listeners: {
                	afterrender: function(self) {
                		self.setChecked(DSS.layer.watershed.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("watershed:visible", self.checked ? "1" : "0");                	
                	DSS.layer.watershed.setVisible(self.checked);                    	
                }
			},{ //-------------------------------------------
				text: 'Contour',
				disabled: true,
				listeners: {
                	afterrender: function(self) {
                		//self.setChecked(DSS.layer.contour.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("contour:visible", self.checked ? "1" : "0");                	
                	DSS.layer.contour.setVisible(self.checked);                    	
                }
			},{ //-------------------------------------------
				text: 'Hillshade',					
                checked: true,
                menu: makeOpacityMenu(DSS.layer.hillshade),
                listeners: {
                	afterrender: function(self) {
                		self.setChecked(DSS.layer.hillshade.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("hillshade:visible", self.checked ? "1" : "0");                	
                	DSS.layer.hillshade.setVisible(self.checked);                    	
                }
			},{ //-------------------------------------------
				xtype: 'menuitem',
				text: 'Inspector <i class="fas fa-search accent-text text-drp-50"></i>',
                menu: makeOpacityMenu(DSS.layer.ModelResult),
			}]
		});
		
		me.callParent(arguments);
	},

});

