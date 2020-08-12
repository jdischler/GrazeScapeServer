
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
		
		let makeOpacityMenu = function(key, openLayersLayer, minValue) {
			minValue = minValue || 20;
			return {
                width: 130,
                plain: true,
            	listeners: {
            		show: function(menu) {
            			menu.down('#dss-slider').setValue(openLayersLayer.getOpacity() * 100, false);
            			menu.setY(menu.getY() - 29);
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
                	minValue: minValue, 
                	maxValue: 100,
                	listeners: {
                		change: function(slider, newValue, thumb, eOpts) {
                			const val = newValue / 100.0;
                			openLayersLayer.setOpacity(val)
                        	Ext.util.Cookies.set(key + ":opacity", "" + val);
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
			items: [{ //-------------------------------------------
				xtype: 'menuitem',
				text: 'Overlays', disabled: true,
				style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px; background-color: #ccc'
			},{ //-------------------------------------------
				text: 'Crops <i class="fas fa-seedling accent-text text-drp-50"></i>',
                checked: true,
                menu: makeOpacityMenu("crop", DSS.layer.cropOverlay),
                listeners: {
                	afterrender: function(self) {
                		self.setChecked(DSS.layer.cropOverlay.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("crop:visible", self.checked ? "1" : "0");                	
                	DSS.layer.cropOverlay.setVisible(self.checked);                    	
                }
			},{ //-------------------------------------------
				text: 'Inspector <i class="fas fa-search accent-text text-drp-50"></i>',
                checked: true,
                menu: makeOpacityMenu("inspector", DSS.layer.ModelResult, 50),
                listeners: {
                	afterrender: function(self) {
                		self.setChecked(DSS.layer.ModelResult.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("inpsector:visible", self.checked ? "1" : "0");                	
                	DSS.layer.ModelResult.setVisible(self.checked);                    	
                }
			},{ //-------------------------------------------
				text: 'Tainter Watershed',
                checked: true,
                menu: makeOpacityMenu("watershed", DSS.layer.watershed),
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
                menu: makeOpacityMenu("hillshade", DSS.layer.hillshade, 30),
                listeners: {
                	afterrender: function(self) {
                		self.setChecked(DSS.layer.hillshade.getVisible());
                	}
                },
                handler: function(self) {
                	Ext.util.Cookies.set("hillshade:visible", self.checked ? "1" : "0");                	
                	DSS.layer.hillshade.setVisible(self.checked);                    	
                }
			},{//-----------------------------------------------------------------
				xtype: 'menuitem',
				text: 'Base Layer', disabled: true,
				style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px; background-color: #ccc'
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
			}]
		});
		
		me.callParent(arguments);
	},

});

