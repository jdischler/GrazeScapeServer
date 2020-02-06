
DSS.utils.addStyle('.x-tip-default { background-color: #666666ff; border-color:#444}');
DSS.utils.addStyle('.x-tip-body-default {color: #fff}');

Ext.create('Ext.data.Store', {
   storeId: 'operationsStore',
   fields: ['operation', 'scenario1', 'scenario2', 'scenario3', 'scenario4', 'composite', 'isOperation'],
   data: [{
	   operation: 'Grazing Acres', scenario1: 98, scenario2: 70, scenario3: 80, scenario4: 75, composite: 79.5, isOperation: true
   }, {
	   operation: 'Milky Acres', scenario1: 108, scenario2: 10, scenario3: 40, scenario4: 30, composite: 34, isOperation: true
   }, {
	   operation: 'Hilltop', scenario1: 101, scenario2: 50, scenario3: 60, scenario4: 55, composite: 61, isOperation: true
	   
   }, {
	   operation: 'Baseline', scenario1: 108, scenario2: 10, scenario3: 40, scenario4: 30, composite: 34, isOperation: false
   }, {
	   operation: '50% Grazed', scenario1: 101, scenario2: 50, scenario3: 60, scenario4: 55, composite: 61, isOperation: false
   },{
	   operation: '90% Grazed', scenario1: 98, scenario2: 70, scenario3: 80, scenario4: 75, composite: 79.5, isOperation: false
   }]
});

//------------------------------------------------------------------------------
Ext.define('DSS.pages.CompareOperationsPage', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.compare_operations_page',

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
