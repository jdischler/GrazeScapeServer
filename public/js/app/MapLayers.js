
// wiscland 2.0 codes
/*
1	developed
2	Cash Grain
3	Continuous Corn
4	Dairy Rotation
5	Vegetables
6	Hay
7	Pasture
8	Reed Canary Grass
9	Cool-Season
10	Warm-Season
11	Water/Swamp
12	Other (forest)
13	Other
*/


//------------------------------------------------------------------------------
Ext.define('DSS.app.MapLayers', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Container',
	alias: 'widget.map_layers',
	mixins: [
		'Ext.mixin.Responsive'
	],	

	layout: {
		type: 'hbox',
		pack: 'center',
		align: 'center'
	},
	
	padding: 8,
	style: 'background-color: #e9edd977; background-repeat: no-repeat; background-image: linear-gradient(to right, rgba(0,0,0,0.35), rgba(0,0,0,0.1), rgba(0,0,0,0.03), rgba(0,0,0,0)); background-size: 2rem 100%;',
	
		
	// 'landscape', 'width', 'height', 'tall', 'wide', <platform> e.g, 'desktop',
	//	examples: '!(desktop || width > 800)'
	responsiveConfig: {
		'width < 1050': {
			vertical: true
		},
		'width >= 1050': {
			vertical: false
		}
	},
	setVertical: function(v) {
		let me = this;
		
		me.setLayout({vertical: v});
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				xtype: 'container',
				layout: 'hbox',
			},
			items: [{
				xtype: 'component',
				flex: 1
			},{
				width: 300,
				margin: '0 16',
				defaults: {
					xtype: 'radio',
					name: 'base-layer',
					flex: 1,
					margin: '0 4',
					handler: function(control, checked) {
						if (checked) {
							
						}
					}
				},
				items: [{
                    boxLabel: 'Bing Aerial',
                    DSS_layer: 'bing-aerial',
                    checked: true,
                    handler: function(self, checked) {
                    	DSS.layer.bingAerial.setVisible(checked);
                    }
				},{
                    boxLabel: 'Bing Road',
                    DSS_layer: 'bing-aerial',
                    handler: function(self, checked) {
                    	DSS.layer.bingRoad.setVisible(checked);
                    }
				},{
                    boxLabel: 'Open-Street',
                    DSS_layer: 'bing-aerial',
                    handler: function(self, checked) {
                    	DSS.layer.osm.setVisible(checked);
                    }
				}]
			},{
				width: 300,
				margin: '0 16',
				defaults: {
					xtype: 'checkbox',
					flex: 1,
					margin: '0 4'
				},
				items: [{
                    boxLabel: 'Watershed',
                    checked: true,
                    handler: function(self,checked) {
                    	DSS.layer.watershed.setVisible(checked);                    	
                    }
				},{
                    boxLabel: 'Contour',
                    handler: function(self,checked) {
                    	DSS.layer.contour.setVisible(checked);                    	
                    }
				},{
                    boxLabel: 'Hillshade',					
                    checked: true,
                    handler: function(self,checked) {
                    	DSS.layer.hillshade.setVisible(checked);                    	
                    }
				}]
			},{
				xtype: 'component',
				flex: 1
			}]
		});
		
		me.callParent(arguments);
	},

});

