
DSS.utils.addStyle('.hover {cursor: pointer}');

//------------------------------------------------------------------------------
Ext.define('DSS.field_shapes.apply.Landcover', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shapes_apply_landcover',
	
	cls: 'restriction-widget',
	margin: '2 0 4 0',
	padding: 2,
	
	layout: DSS.utils.layout('vbox', 'start', 'center'),
	
	DSS_sectionHeight: 150,
	
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
					getToggle(me, 'crop.is_active') // Helper defined in DrawAndApply.js
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
					name: rbName,
					listeners: {
						afterrender: function(self) {
							if ( self.boxLabelEl) {
								self.boxLabelEl.setStyle('cursor', 'pointer')
							}
						}
					}
				//	boxLabelCls: 'hover'
				},
				items: [{ 
					boxLabel: 'Dry Lot', 			inputValue: 'dl',
				},{
					boxLabel: 'Pasture', 			inputValue: 'ps',
			/*	},{
					xtype: 'component',
					style: 'border-bottom: 1px solid rgba(0,0,0,0.1); margin: 2px -32px'*/
				},{
					boxLabel: 'Continuous Corn',	inputValue: 'cc',
				},{
					boxLabel: 'Cash Grain',			inputValue: 'cg',
					boxLabelAttrTpl: 'data-qtip="Two-year rotation: Corn Grain & Soybeans"',
				},{
					boxLabel: 'Dairy Rotation 1',	inputValue: 'd1',
					boxLabelAttrTpl: 'data-qtip="Five-year rotation: Corn Grain, Corn Silage, Three years of Alfalfa"',
				},{
					boxLabel: 'Dairy Rotation 2', 	inputValue: 'd2',
					boxLabelAttrTpl: 'data-qtip="Three-year rotation: Corn Silage, Soybeans, Oats"',
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
