
//------------------------------------------------------------------------------
Ext.define('DSS.controls.ComputationTests', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.computation_tests',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'section-title',
				html: 'Computation Tests'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Compute grassland bird habitat index (400m moving window)'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Grassland Bird Model',
					handler: function() {
						me.computeModel('compute_bird_model');
					}
				},{
					xtype: 'component',
					margin: '16 0 0 0',
					cls: 'information',
					html: 'Compute predicted crop yields'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Corn',
					handler: function() {
						me.computeModel('compute_corn_model');
					}
				},{
					xtype: 'component',
					margin: '16 0 0 0',
					cls: 'information',
					html: 'Visualize Slope'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Slope',
					handler: function() {
						me.computeModel('compute_slope');
					}
				},{
					xtype: 'radiogroup',
					itemId: 'mode_radio',
					style: 'border-top: 1px solid #ddd',
			        columns: 4,
			        vertical: false,
			        padding: '16 0 0 0',
			        items: [{ 
			        	boxLabel: 'Full', name: 'mode', inputValue: 0, checked: true
					},{ 
						boxLabel: 'Clipped', name: 'mode', inputValue: 1
					},{ 
						boxLabel: 'Mean', name: 'mode', inputValue: 2
					},{ 
						boxLabel: 'Masked', name: 'mode', inputValue: 3
					}]
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//----------------------------------------------------------------------
	computeModel: function(url) {
		let me = this;
		
		DSS.activeFarm = DSS.activeFarm || 33;
		
		let obj = Ext.Ajax.request({
			url: location.href + url,
			jsonData: {
				farm_id: DSS.activeFarm,
				mode: me.down('#mode_radio').getValue()['mode']
			},
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				var obj = JSON.parse(response.responseText);
				working = false;
				DSS.layer.Image.setSource(new ol.source.ImageStatic({
					projection: 'EPSG:3071',
					url: obj.url,
					imageExtent: obj.extent
				}))
				DSS.layer.Image.setOpacity(0.7);
				DSS.layer.Image.setVisible(true);	
				
				DSS.MapState.showLegend(obj.palette, obj.values);
			},
			
			failure: function(respose, opts) {
				console.log('computationTests.computeModel');
			}
		});
	},

});
