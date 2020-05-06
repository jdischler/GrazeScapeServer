
//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Landcover', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_landcover',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 148,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		let rbName = "crop";
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				width: '100%',
				layout: 'absolute',
				items: [{
					xtype: 'component',
					x: 0, y: -6,
					width: '100%',
					height: 28,
					cls: 'information accent-text bold',
					html: "Set Crop / Landcover",
				},
					getToggle(me, 'crop.is_active')
				]
			},{
				xtype: 'radiogroup',
				itemId: 'contents',
				style: 'padding: 0px; margin: 0px', // fixme: eh...
				hideEmptyLabel: true,
				columns: 1, 
				vertical: true,
				viewModel: {
					formulas: {
						cropValue: {
							bind: '{crop.value}', // inherited from parent
							get: function(val) {
								let obj = {};
								obj[rbName] = val;
								return obj;
							},
							set: function(val) {
								this.set('crop.value', val[rbName]);
							}
						}
					}
				},
				bind: '{cropValue}', // formula from viewModel above
				defaults: {
					name: rbName
				},
				items: [{ 
					boxLabel: 'Dry Lot', 			inputValue: 'dl',
				},{
					boxLabel: 'Pasture', 			inputValue: 'ps',
				},{
					boxLabel: 'Continuous Corn',	inputValue: 'cc',
				},{
					boxLabel: 'Cash Grain',			inputValue: 'cg',
				},{
					boxLabel: 'Dairy Rotation 1',	inputValue: 'd1',
				},{
					boxLabel: 'Dairy Rotation 2', 	inputValue: 'd2'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
