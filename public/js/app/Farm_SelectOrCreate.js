
//------------------------------------------------------------------------------
Ext.define('DSS.app.Farm_SelectOrCreate', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.farm_select_or_create',

	requires: [
		'DSS.app.Shapes_Upload',	
	],
		
	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	padding: 8,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				xtype: 'container',
				margin: 2,
			},
			items: [{
				style: 'border-radius: 2px; border: 1px solid #ddd; background-color: #eee;',
				layout: DSS.utils.layout('vbox', 'center', 'middle'),
				items: [{
					xtype: 'component',
					style: 'font-weight: bold; color: rgba(0,0,0,0.75)',
					margin: 2,
					html: 'Select a Farm'
				},{
					xtype: 'container',
					margin: '2 2 8 2',
					layout: DSS.utils.layout('hbox', 'center', 'middle'),
					items: [{
						xtype: 'button',
						scale: 'medium',
						margin: 2,
						width: 100,
						text: 'Browse',
					},{
						xtype: 'component',
						margin: 8,
						style: 'color: rgba(0,0,0,0.6); text-align: center',
						html: 'or'
					},{
						xtype: 'button',
						scale: 'medium',
						margin: 2,
						width: 100,
						text: 'Create',
					}]
				}]
			},{ 
				//---------------------------------------
				style: 'border-radius: 2px; border: 1px solid #ddd; background-color: #eee;',
				layout: DSS.utils.layout('vbox', 'center', 'middle'),
				items: [{
					xtype: 'component',
					style: 'font-weight: bold; color: rgba(0,0,0,0.75)',
					margin: 2,
					html: 'Browse Map for Farms'
				},{
					xtype: 'container',
					margin: 2,
					layout: DSS.utils.layout('hbox', 'center', 'middle'),
					items: [{
						xtype: 'textfield',
						emptyText: 'Filter farms by...',
						width: 240
					},{
						xtype: 'tool',
						margin: '0 4',
						type: 'search',
						tooltip: 'Apply Filter'
					}]
				},{
					xtype: 'checkbox',
					margin: 8,
					boxLabel: 'Include publicly shared farms',
				}]
			},{ 
				//---------------------------------------
				style: 'border-radius: 2px; border: 1px solid #ddd; background-color: #eee;',
				layout: DSS.utils.layout('vbox', 'center', 'middle'),
				items: [{
					xtype: 'component',
					style: 'font-weight: bold; color: rgba(0,0,0,0.75)',
					margin: 2,
					html: 'Field Boundaries'
				},{
					xtype: 'container',
					margin: '2 2 8 2',
					layout: DSS.utils.layout('hbox', 'center', 'middle'),
					items: [{
						xtype: 'button',
						scale: 'medium',
						margin: 2,
						width: 120,
						text: 'Draw / Modify',
					},{
						xtype: 'component',
						margin: 8,
						style: 'color: rgba(0,0,0,0.6); text-align: center',
						html: 'or'
					},{
						xtype: 'button',
						scale: 'medium',
						disabled: true,
						margin: 2,
						width: 120,
						text: 'Upload',
					}]
				}]
			},{ 
				//---------------------------------------
				style: 'border-radius: 2px; border: 1px solid #ddd; background-color: #eee;',
				layout: DSS.utils.layout('vbox', 'center', 'middle'),
				items: [{
					xtype: 'component',
					style: 'font-weight: bold; color: rgba(0,0,0,0.75)',
					margin: 2,
					html: 'Upload Shapefiles'
				},{
					xtype: 'shapes_upload',
					margin: '8 2',
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
