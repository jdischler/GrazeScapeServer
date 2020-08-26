
//------------------------------------------------------------------------------
Ext.define('DSS.state.scenario.CropNutrientMode', {
//------------------------------------------------------------------------------
	extend: 'Ext.button.Segmented', // Ext.container
	alias: 'widget.state_crop_nutrient_mode',
	
	singleton: true,
	
//	padding: '0 6 6 6',
	floating: true,
	shadow: false,
	hidden: true,
	
	style: 'border-radius: 2px; box-shadow: 0 4px 8px rgba(0,0,0,0.5);',
	layout: DSS.utils.layout('hbox', 'start'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
 				xtype: 'button',
				toggleGroup: 'crop-nutrient-mode',
				padding: '4 0 0 0',
				height: 30,
				allowDepress: false,
				frame: false
			},
			items: [{
				text: 'Crops',
				tooltip: 'Assign crops / tillage',
				width: 70, 
			},{
				text: 'Nutrients',
				tooltip: 'Apply nutrients',
				width: 96
			},{
				html: '<i class="fas fa-search"></i>',
				tooltip: 'Activate Inspector <i class="fas fa-search accent-text"></i> mode',
				width: 48,
				pressed: true
			}]
		});
		
		me.callParent(arguments);
		
		me.showAt(400, -38); me.setHidden(true);
		
		AppEvents.registerListener('show_crop_nutrient_mode', function() {
			let om = Ext.getCmp('ol_map');
			let x = om.getX() + (om.getWidth() - /*me.getWidth()*/214) * 0.5;
			me.setHidden(false);
			me.setX(x);
			me.stopAnimation().animate({
				duration: 300,
				to: {
					y: -4
				}
			})
		})
		AppEvents.registerListener('hide_crop_nutrient_mode', function() {
			me.stopAnimation().animate({
				duration: 300,
				to: {
					y: -38
				},
				callback: function() {
					me.setHidden(true);
				}
			})
		})
		AppEvents.registerListener('map_resize', function() {
			if (!me.isHidden()) {
				let om = Ext.getCmp('ol_map');
				me.setX(om.getX() + (om.getWidth() - me.getWidth()) * 0.5);
			}
		})
	},
	
});
