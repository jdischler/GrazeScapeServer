DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');
DSS.utils.addStyle('.box-label-cls {color: #eee; text-shadow: 0 1px rgba(0,0,0,0.2),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.small {color: #38b; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');


var models = Ext.create('Ext.data.Store', {
	fields: ['key', 'name'],
	data: [{
		"key":"yield", "name":"Landcover Yield"
	},{
		"key":"slope", "name":"Slope"
	},{
		"key":"bird", "name": "Bird Habitat"
	}]
});

//------------------------------------------------------------------------------
Ext.define('DSS.controls.StatsPanel', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.stats_panel',
    alternateClassName: 'DSS.StatsPanel',
    singleton: true,	
	
	width: 0,
	region: 'east',
	cls: 'info-panel',
	resizable: true,
	resizeHandles: 'w',
	maxWidth: 275,
	padding: '8 6',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	listeners: {
		afterrender: function(self) {
			setTimeout(function() {
				self.animate({
					dynamic: true,
					to: {
						width: 200
					},
					callback: function() {
						self.setMinWidth(200);
						// ooof, the Ext resizer doesn't seem to realize when its resize target has a min/max width change
						self.resizer.resizeTracker.minWidth = 200;
					}
				})
			}, 2000);
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
				xtype: 'component',
			},
			items: [{
				cls: 'section-title',
				style: 'color: #ddd; text-shadow: 0 1px rgba(0,0,0,0.2)',
				html: 'Map Stats <i class="fas fa-chart-area small"></i>',
			},{
				DSS_ratio: 0.6,
				margin: 4,
				minHeight: 64,
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				listeners: {
					resize: function(self, w, h) {
						if (self.DSS_ratio) {
							self.setHeight(w * self.DSS_ratio);
						}
					}
				},
			},{
				DSS_ratio: 0.5,
				margin: 4,
				minHeight: 64,
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				listeners: {
					resize: function(self, w, h) {
						if (self.DSS_ratio) {
							self.setHeight(w * self.DSS_ratio);
						}
					}
				}
			},{
				flex: 1
			},{
				cls: 'section-title',
				style: 'color: #ddd; text-shadow: 0 1px rgba(0,0,0,0.2)',
				html: 'Inspector <i class="fas fa-search small"></i>',
			},{
				xtype: 'container',
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				height: 128,
				margin: 4,
				padding: 8,
				items: [{
					xtype: 'component',
					style: 'text-align: center; color: #ccc',
					html: 'Mode'
				},{
					xtype: 'button',
					text: 'Crop Yield',
					listeners: {
						afterrender: function(self) {
							self.setMenu({
								DSS_ownerButton: self,
								plain: true,
								width: 160,
								items: [{
									text: 'Models',
									disabled: true,
									style: 'border-bottom: 1px solid rgba(0,0,0,0.2);'
								},{
									text: 'Soil Loss',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'P-Loss',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'Crop Yield',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'Bird Habitat',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'Land Properties',
									disabled: true,
									style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
								},{
									text: 'Slope',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'Soil Depth',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: '% Sand',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								},{
									text: 'Distance to Water',
									handler: function(self) {
										self.parentMenu.DSS_ownerButton.setText(self.text)
									}
								}]
							})
						}
					}
				},{
					xtype: 'radiogroup',
					id: 'DSS_cheat',
					columns: 1,
					vertical: true,
					items: [{ 
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Corn', name: 'model', inputValue: 'corn', checked: true,
						handler: function() {
							me.computeResults();
						}
					},{
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Slope', name: 'model', inputValue: 'slope',
						handler: function() {
							me.computeResults();
						}
					},{
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Bird Habitat', name: 'model', inputValue: 'bird',
						handler: function() {
							me.computeResults();
						}
					}]
				},{
					xtype: 'component', flex: 1
				}]
			},{
				xtype: 'checkbox',
				id: 'DSS_cheatRestrictFarm',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Restrict to active operation', checked: true,
				handler: function(self, checked) {
					me.computeResults();
					me.down('#DSS_aggregateHider').animate({
						dynamic: true,
						to: {
							height: checked ? 28 : 0
						}
					})
				}
			},{
				xtype: 'container',
				style: 'overflow: hidden!important',
				itemId: 'DSS_aggregateHider',
				height: 28,
				items: [{
					xtype: 'checkbox',
					id: 'DSS_cheatFieldAggregate',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Aggregate to field', checked: true,
					handler: function() {
						me.computeResults();
					}
				}]
			},{
				xtype: 'checkbox',
				id: 'DSS_maskByCDL',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Restrict to landcover', checked: false,
				handler: function(self, checked) {
					me.computeResults();
					me.down('#DSS_cdlHider').animate({
						dynamic: true,
						to: {
							height: checked ? 56 : 0
						}
					})
				}
			},{
				xtype: 'container',
				itemId: 'DSS_cdlHider',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				height: 0,
				items: [{
					xtype: 'checkbox',
					id: 'DSS_cheatRowCropMask',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Match rowcrops', checked: true,
					handler: function() {
						me.computeResults();
					}
				},{
					xtype: 'checkbox',
					id: 'DSS_cheatGrassMask',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Match grasses', checked: true,
					handler: function() {
						me.computeResults();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	computeResults: function(extents) {
		let me = this;
		
		// TODO: busy feedback
		if (me.DSS_isWorking) {
			return;
		}
		
		if (!extents) {
			extents = me.DSS_extents;
		}
		
		if (!extents) {
			console.log("nothing to do right now?");
			return;
		}
		me['DSS_extents'] = extents;
	
		me.DSS_isWorking = true;
		
		let data = {
			"extent" : extents,
			"model": Ext.getCmp('DSS_cheat').getValue()['model']
		};
		if (Ext.getCmp('DSS_cheatRestrictFarm').getValue() && DSS.activeFarm) {
			data["farm_id"] = DSS.activeFarm;
			data["mode"] = Ext.getCmp('DSS_cheatFieldAggregate').getValue() ? 2 : 1;
		}
		if (Ext.getCmp('DSS_maskByCDL').getValue()) {
			data['row_crops'] = Ext.getCmp('DSS_cheatRowCropMask').getValue() ? true : false;
			data['grasses'] = Ext.getCmp('DSS_cheatGrassMask').getValue() ? true : false;
		}
		
		var obj = Ext.Ajax.request({
			url: location.href + 'fetch_image',
			jsonData: data,
			timeout: 10000,
			success: function(response, opts) {
				var obj = JSON.parse(response.responseText);
				me.DSS_isWorking = false;

				DSS.layer.Image.setSource(new ol.source.ImageStatic({
					url: obj.url,
					imageExtent: obj.extent,
					projection: 'EPSG:3071'
				}))
				DSS.layer.Image.setOpacity(0.7);
				DSS.layer.Image.setVisible(true);	
				
				DSS.MapState.showLegend(obj.palette, obj.values);
			},
			
			failure: function(response, opts) {
				me.DSS_isWorking = false;
			}
		});

	}

});

