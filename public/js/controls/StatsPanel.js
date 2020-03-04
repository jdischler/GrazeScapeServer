DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');
DSS.utils.addStyle('.box-label-cls {color: #eee; text-shadow: 0 1px rgba(0,0,0,0.2),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.small {color: #38b; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');

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
					xtype: 'radiogroup',
					id: 'DSS_cheat',
					columns: 1,
					vertical: true,
					items: [{ 
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Corn', name: 'model', inputValue: 'corn', checked: true 
					},{
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Slope', name: 'model', inputValue: 'slope'
					},{
						boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
						boxLabel: 'Bird Habitat', name: 'model', inputValue: 'bird'
					}]
				},{
					xtype: 'component', flex: 1
				}]
			},{
				xtype: 'checkbox',
				id: 'DSS_cheatRestrictFarm',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Restrict to active farm', checked: true,
				handler: function(self, checked) {
					me.down('#DSS_aggregateHider').animate({
						dynamic: true,
						to: {
							height: checked ? 24 : 0
						}
					})
				}
			},{
				xtype: 'container',
				style: 'overflow: hidden!important',
				itemId: 'DSS_aggregateHider',
//				height: 0,
				items: [{
					xtype: 'checkbox',
					id: 'DSS_cheatFieldAggregate',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Aggregate to field', checked: true 
				}]
			},{
				xtype: 'checkbox',
				id: 'DSS_maskByCDL',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Restrict to landcover', checked: false,
				handler: function(self, checked) {
					me.down('#DSS_cdlHider').animate({
						dynamic: true,
						to: {
							height: checked ? 48 : 0
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
					boxLabel: 'Match rowcrops', checked: true
				},{
					xtype: 'checkbox',
					id: 'DSS_cheatGrassMask',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Match grasses', checked: true 
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});

