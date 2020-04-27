
//------------------------------------------------------------------------------
Ext.define('DSS.state.operation.AnimalGrazingMode', {
//------------------------------------------------------------------------------
	extend: 'Ext.button.Segmented', // Ext.container
	alias: 'widget.state_animal_grazing_mode',
	
	singleton: true,
	
//	padding: '0 6 6 6',
	floating: true,
	shadow: false,
	hidden: false,
	
	style: 'border-radius: 2px; box-shadow: 0 4px 8px rgba(0,0,0,0.5);',
	layout: DSS.utils.layout('hbox', 'start'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
 				xtype: 'button',
				toggleGroup: 'field-shape-mode',
				padding: '4 0 0 0',
				height: 30,
				allowDepress: false,
				frame: false
			},
			items: [{
				text: 'Work in progress',
				tooltip: 'TBD',
				width: 140, 
			},{
				html: '<i class="fas fa-search"></i>',
				tooltip: 'Activate Inspector <i class="fas fa-search accent-text"></i> mode',
				width: 48,
				pressed: true
			}]
		});
		
		me.callParent(arguments);
		
		me.showAt(400, -38); me.setHidden(true);
		
		AppEvents.registerListener('show_animal_grazing_mode', function() {
			let om = Ext.getCmp('ol_map');
			let x = om.getX() + (om.getWidth() - /*me.getWidth()*/188) * 0.5;
			me.setHidden(false);
			me.setX(x);
			me.stopAnimation().animate({
				duration: 300,
				to: {
					y: -4
				}
			})
		})
		AppEvents.registerListener('hide_animal_grazing_mode', function() {
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
