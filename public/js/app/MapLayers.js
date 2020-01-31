
//------------------------------------------------------------------------------
Ext.define('DSS.app.MapLayers', {
//------------------------------------------------------------------------------
	extend: 'Ext.panel.Panel',
	alias: 'widget.map_layers',
	mixins: [
		'Ext.mixin.Responsive'
	],	

	title: 'Map Layers',
	titleAlign: 'center',
	collapsible: true,
	layout: {
		type: 'hbox',
		pack: 'center',
		align: 'center'
	},
	
	bodyPadding: 8,
	style: 'background-color: #fff',
		
	// 'landscape', 'width', 'height', 'tall', 'wide', <platform> e.g, 'desktop',
	//	examples: '!(desktop || width > 800)'
	responsiveConfig: {
		'width < 800': {
			vertical: true
		},
		'width >= 800': {
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
				width: 400,
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
                    DSS_layer: 'bing-aerial'
				},{
                    boxLabel: 'Bing Road',
                    DSS_layer: 'bing-aerial'
				},{
                    boxLabel: 'Open-Street',
                    DSS_layer: 'bing-aerial'
				},{
					xtype: 'component' // placeholder
				}]
			},{
				width: 400,
				defaults: {
					xtype: 'checkbox',
					flex: 1,
					margin: '0 4'
				},
				items: [{
                    boxLabel: 'Watershed',
				},{
                    boxLabel: 'Fields',					
				},{
                    boxLabel: 'Contour',
				},{
                    boxLabel: 'Hillshade',					
				}]
			},{
				xtype: 'component',
				flex: 1
			}]
		});
		
		me.callParent(arguments);
	},

});

