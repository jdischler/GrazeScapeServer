


// Compute yield for all field shapes and a single crop
//		Be able to set a yield target as the bar for calculating good vs. bad
//		Show map overlay in terms of how far total yield is above below bar set...

// Compute yield for crop assigned to each field shape
// 	Analysis is trickier:
//		Be able to set a yield target as the bar for calculating good vs. bad
//		Show field Total for each component
//		Show map overlay in terms of how far total yield is above below bar set...
//		Click individual bar element to see yield map for just that crop


// Icon ideas for zoom charts up/down and auto-pan map to field?

/*
<i class="fas fa-crosshairs"></i>
<i class="fas fa-compress-alt"></i>
<i class="fas fa-expand-alt"></i>
<i class="fas fa-search-location"></i>
<i class="fas fa-search-plus"></i>
<i class="fas fa-search-minus"></i>

<i class="fas fa-crosshairs"></i>
*/

//------------------------------------------------------------------------------
Ext.define('DSS.pages.CompareChart', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.compare_Chart',

	autoDestroy: false,
	layout: 'fit',

	padding: 32,

	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			items: [{
			   xtype: 'cartesian',
//			   region: 'center',
		       legend: {
		            type: 'sprite',
		            docked: 'bottom'
		        },			   
			   store: 'operationsStore',
			   axes: [{
			       type: 'numeric',
			       position: 'left',
			       grid: true,
			       title: {
			           text: 'Profit per AU',
			           fontSize: 15,
			           fontWeight: 'bold',
			           color: '#888'
			       },
			       fields: ['scenario1','scenario2','scenario3','scenario4', 'composite'],
			   }, {
			       type: 'category',
			       position: 'bottom',
			       title: {
			           text: 'Operations',
			           fontSize: 15,
			           fontWeight: 'bold',
			           color: '#888',
			       },
			       fields: 'operation'
			   }],
			   series: {
			       type: 'bar',
			       stacked: false,
			       title: [ 'Average Year', 'Bad Climate', 'Expensive Feed', 'Low Milk Price', 'Weighted Average' ],
			       xField: 'operation',
			       yField: ['scenario1','scenario2','scenario3','scenario4', 'composite'],
			       style: {
		                minBarWidth: 20,
		                maxBarWidth: 100,
		                minGapWidth: 5,
		                inGroupGapWidth: 2
			       },
		            tooltip: {
		                trackMouse: true,
		                renderer: function(tooltip, record, item) {
		                	if (!record) return;
		                    let fieldIndex = Ext.Array.indexOf(item.series.getYField(), item.field);
		                    let scenario = item.series.getTitle()[fieldIndex];

		                    tooltip.setHtml(record.get('operation') + '[' + scenario + ']: $' +
		                    		record.get(item.field).toFixed(1) + ' (per AU)');
		                },
		            }			       
			   }
			}]
		});
		
		me.callParent(arguments);
		
		var filters = Ext.data.StoreManager.lookup('operationsStore').getFilters();
		filters.removeAll();
		let fil = function(item) {
			return item.data.isOperation == true;
		} 
		filters.add(fil);	
	}

});
