DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');

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
						self.setMinWidth(150);
						// ooof, the Ext resizer doesn't seem to realize when its resize target has a min/max width change
						self.resizer.resizeTracker.minWidth = 150;
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
				listeners: {
					resize: function(self, w, h) {
						self.setHeight(w * self.DSS_ratio);
					}
				},
				DSS_ratio: '0.6',
				margin: 4,
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)'
			},
			items: [{
				listeners: undefined,
				cls: 'section-title',
				minHeight: undefined,
				style: 'color: #ddd',
				html: 'Map Stats',
				margin: 0
			},{
				DSS_ratio: '0.6',
				minHeight: 64,
			},{
				DSS_ratio: '0.5',
				minHeight: 64,
			}]
		});
		
		me.callParent(arguments);
	}

});

