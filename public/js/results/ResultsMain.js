
//------------------------------------------------------------------------------
Ext.define('DSS.results.ResultsMain', {
//------------------------------------------------------------------------------
	extend: 'Ext.window.Window',
	alias: 'widget.results_main',
	
    scrollable: 'y',

    title: 'Results',
    layout: DSS.utils.layout('vbox', 'start', 'stretch'),
 //   style: 'background-color: #fff',
	titleAlign: 'center',
	requires: [
	],
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				height: 300,
				margin: 4,
				layout: DSS.utils.layout('hbox', 'start', 'stretch'),
				items: [{
					xtype: 'polar',
					flex: 1,
					insetPadding: 0,
					border: false,
					store: {
						fields: ['name', 'data1'],
						data: [{
							name: 'Silage',
							data1: 25592
						},{
							name: 'Alfalfa',
							data1: 12809
						},{
							name: 'Grass',
							data1: 8701
						},{
							name: 'Oats',
							data1: 3204.3
						},{
							name: 'Soybeans',
							data1: 6216
						},{
							name: 'Corn Grain',
							data1: 23972
						}]
					},
					legend: {
						type: 'sprite',
						docked: 'bottom'
					},
					series: {
						type: 'pie',
						angleField: 'data1',
						label: {
							field: 'name',
							display: 'inside'
						},
						colors: [
							'#a84',
							'#a6d',
							'#6c4',
							'#c74',
							'#49d',
							'#ee5',
						]
					}				
				},{ //--------------------------
					xtype: 'cartesian',
					flex: 1,
					insetPadding: {
						left: 0, right: 10, top: 10, bottom: 0
					},
					innerPadding: {
						left: 6, right: 6,
					},
					border: false,
					legend: {
						type: 'sprite',
						docked: 'bottom'
					},				   
					store: {
						fields: ['name', 'datax_cg', 'datay_cg', 'datax_il', 'datay_il'],
						data: [{
							'datax_cg':1.36535, 'datay_cg':0
						},{
							'datax_cg':1.86535, 'datay_cg':21.83
						},{
							'datax_cg':2.3143568, 'datay_cg':0
						},{
							'datax_cg':2.7633638, 'datay_cg':0
						},{
							'datax_cg':3.2123709, 'datay_cg':0
						},{
							'datax_cg':3.6613774, 'datay_cg':1.43
						},{
							'datax_cg':4.1103845, 'datay_cg':5.3199997
						},{
							'datax_cg':4.5593915, 'datay_cg':6.41
						},{
							'datax_cg':5.008398, 'datay_cg':0.03
						},{
							'datax_cg':5.457405, 'datay_cg':0
						},{
							'datax_cg':5.906412, 'datay_cg':0
						},{
							'datax_cg':6.355419, 'datay_cg':0
						},{
							'datax_cg':6.804426, 'datay_cg':0
						},{
							'datax_cg':7.2534328, 'datay_cg':0
						},{
							'datax_cg':7.70244, 'datay_cg':0
						},{
							'datax_cg':8.151446, 'datay_cg':0.53
						},{
							'datax_cg':8.600453, 'datay_cg':2.81
						},{
							'datax_cg':9.04946, 'datay_cg':3.8999999
						},{
							'datax_cg':9.4984665, 'datay_cg':4.89
						},{
							'datax_cg':9.947474, 'datay_cg':1.8299999
						},{
							'datax_cg':10.396481, 'datay_cg':0.01
						},
							
						{'datax_il':3.8, 'datay_il':0.0},
						{'datax_il':3.96, 'datay_il':2.61},{'datax_il':4.18, 'datay_il':5.88},
						{'datax_il':4.39, 'datay_il':9.15},{'datax_il':4.59, 'datay_il':4.19},
						{'datax_il':4.81, 'datay_il':0.0},{'datax_il':5.02, 'datay_il':0.0},
						{'datax_il':5.23, 'datay_il':0.0},{'datax_il':5.43, 'datay_il':0.0},
						{'datax_il':5.64, 'datay_il':0.0},{'datax_il':5.85, 'datay_il':0.0},
						{'datax_il':6.06, 'datay_il':0.01},{'datax_il':6.27, 'datay_il':0.04},
						{'datax_il':6.48, 'datay_il':0.13},{'datax_il':6.69, 'datay_il':1.34},
						{'datax_il':6.90, 'datay_il':1.89},{'datax_il':7.11, 'datay_il':3.30},
						{'datax_il':7.32, 'datay_il':6.06},{'datax_il':7.53, 'datay_il':5.09},
						{'datax_il':7.74, 'datay_il':3.01},{'datax_il':7.95, 'datay_il':0.02},
						
						{'datax_gg':4.2, 'datay_gg':0},{'datax_gg':4.267647, 'datay_gg':0.14},{'datax_gg':4.3257365, 'datay_gg':0.32999998},{'datax_gg':4.3838263, 'datay_gg':0.52},{'datax_gg':4.4419165, 'datay_gg':0.72999996},{'datax_gg':4.5000057, 'datay_gg':0.96},{'datax_gg':4.558096, 'datay_gg':1.7299999},{'datax_gg':4.616186, 'datay_gg':1.8499999},{'datax_gg':4.6742754, 'datay_gg':1.87},{'datax_gg':4.7323656, 'datay_gg':1.6899999},{'datax_gg':4.790456, 'datay_gg':1.53},{'datax_gg':4.848545, 'datay_gg':1.4},{'datax_gg':4.9066353, 'datay_gg':1.49},{'datax_gg':4.9647255, 'datay_gg':1.31},{'datax_gg':5.0228148, 'datay_gg':0.92999995},{'datax_gg':5.080905, 'datay_gg':0.64},{'datax_gg':5.138995, 'datay_gg':0.57},{'datax_gg':5.1970844, 'datay_gg':0.22999999},{'datax_gg':5.2551746, 'datay_gg':0.099999994},{'datax_gg':5.3132644, 'datay_gg':0.07},{'datax_gg':5.371354, 'datay_gg':0.01},
						
						{'datax_sb':1.3248898, 'datay_sb':0},
						{'datax_sb':1.3848898, 'datay_sb':3.04},
						{'datax_sb':1.4432318, 'datay_sb':5.2599998},{'datax_sb':1.5015737, 'datay_sb':10.139999},{'datax_sb':1.5599157, 'datay_sb':2.45},{'datax_sb':1.6182575, 'datay_sb':0},{'datax_sb':1.6765995, 'datay_sb':0},{'datax_sb':1.7349415, 'datay_sb':0},{'datax_sb':1.7932835, 'datay_sb':0},{'datax_sb':1.8516254, 'datay_sb':0},{'datax_sb':1.9099673, 'datay_sb':0},{'datax_sb':1.9683092, 'datay_sb':0.099999994},{'datax_sb':2.0266511, 'datay_sb':1.23},{'datax_sb':2.0849931, 'datay_sb':1.63},{'datax_sb':2.143335, 'datay_sb':0.85999995},{'datax_sb':2.201677, 'datay_sb':1.62},{'datax_sb':2.2600188, 'datay_sb':3.27},{'datax_sb':2.3183608, 'datay_sb':3.6299999},{'datax_sb':2.3767028, 'datay_sb':0.69},{'datax_sb':2.4350448, 'datay_sb':0.14999999},{'datax_sb':2.4933867, 'datay_sb':0.01},
						
						{'datax_al':4.86, 'datay_al':0},{'datax_al':4.931372, 'datay_al':0.19},{'datax_al':5.006387, 'datay_al':0.19},{'datax_al':5.0814023, 'datay_al':0.22999999},{'datax_al':5.156418, 'datay_al':0.25},{'datax_al':5.231433, 'datay_al':0.42999998},{'datax_al':5.306448, 'datay_al':0.51},{'datax_al':5.381463, 'datay_al':0.57},{'datax_al':5.456478, 'datay_al':0.9},{'datax_al':5.531493, 'datay_al':1.38},{'datax_al':5.6065083, 'datay_al':1.36},{'datax_al':5.681524, 'datay_al':1.64},{'datax_al':5.756539, 'datay_al':2.1},{'datax_al':5.831554, 'datay_al':1.52},{'datax_al':5.906569, 'datay_al':2.09},{'datax_al':5.981584, 'datay_al':3.3799999},{'datax_al':6.056599, 'datay_al':2.35},{'datax_al':6.1316147, 'datay_al':2.1599998},{'datax_al':6.2066298, 'datay_al':0.55},{'datax_al':6.281645, 'datay_al':0.02},{'datax_al':6.35666, 'datay_al':0.01},
						]
					},
					axes: [{
						type: 'numeric',
						position: 'left',
						fields: ['datay_cg', 'datay_gg', 'datay_sb', 'datay_il', 'datay_al'],
						title: {
							text: 'ha',
							fontSize: 15
						},
						grid: true,
					},{
						type: 'numeric',
						position: 'bottom',
						fields: ['datax_cg','datax_gg', 'datax_sb', 'datax_il', 'datax_al'],
						title: {
							text: 'DM (kg)',
							fontSize: 15
						}
					}],
					series: [{
						type: 'line', fill: true,//, smooth: true,
						xField: 'datax_il', yField: 'datay_il',
						title: 'Silage',
						style: {
							fill: '#962',
							fillOpacity: 0.2,
							stroke: '#740',
							strokeOpacity: 0.8,
						}
					},{
						type: 'line', fill: true,// smooth: true,
						xField: 'datax_al', yField: 'datay_al',
						title: 'Alfalfa',
						style: {
							fill: '#94b',
							fillOpacity: 0.2,
							stroke: '#729',
							strokeOpacity: 0.8,
						}
					},{
						type: 'line', fill: true,// smooth: true,
						xField: 'datax_gg', yField: 'datay_gg',
						title: 'Grass',
						style: {
							fill: '#4a2',
							fillOpacity: 0.2,
							stroke: '#280',
							strokeOpacity: 0.8,
						}
					},{
						type: 'line', fill: true,// smooth: true,
						xField: 'datax_ot', yField: 'datay_ot',
						title: 'Oats',
						style: {
							fill: '#c74',
							fillOpacity: 0.2,
							stroke: '#840',
							strokeOpacity: 0.8,
						}
					},{
						type: 'line', fill: true,// smooth: true,
						xField: 'datax_sb', yField: 'datay_sb',
						title: 'Soybeans',
						style: {
							fill: '#26a',
							fillOpacity: 0.2,
							stroke: '#048',
							strokeOpacity: 0.8,
						}
					},{
						type: 'line', fill: true,//, smooth: true,
						xField: 'datax_cg', yField: 'datay_cg',
						title: 'Corn Grain',
						style: {
							fill: '#ee2',
							fillOpacity: 0.2,
							stroke: '#cc0',
							strokeOpacity: 0.8,
						}
					}]
				}]
			}]
		});
				
		
		
	/*			xtype: 'container',
				style: 'background-color: #fff; border-radius: 6px; border: 1px solid #444',
				width: 250,
				margin: '8 4 4 4',
			//	height: 350,
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'section-title accent-text',
					html: '80,496'
				},{
					xtype: 'container',
					layout: DSS.utils.layout('hbox', 'start', 'stretch'),
					items:[{
						xtype: 'component', flex: 1
					},{
						xtype: 'component',
						cls: 'information med-text',
						style: 'border-top: 1px solid #aaa',
						padding: '4 8',
						html: 'Total Dry Matter (kg)'
					},{
						xtype: 'component', flex: 1
					}]
				},{ //-------------------------------------------------------------------
				},{ //---------------------------------------------------------------------------
			}]
		});
		*/
		me.callParent(arguments);
	},
	
});
