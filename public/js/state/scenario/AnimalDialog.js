
//------------------------------------------------------------------------------
Ext.define('DSS.state.scenario.AnimalDialog', {
//------------------------------------------------------------------------------
	extend: 'Ext.window.Window',
	alias: 'widget.state_animal_dialog',
	
	autoDestroy: false,
	closeAction: 'hide',
	constrain: true,
	modal: true,
	width: 480,
	minHeight: 80,
	minWidth: 320,
	resizable: true,
	
	title: 'Configure Animals',
	
//	style: 'border-radius: 2px; box-shadow: 0 4px 8px rgba(0,0,0,0.5);',
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				padding: 4,
				cls: 'information med-text',
				html: 'Click to choose the types of animals present at this operation'
					
			},{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center'),
				defaults: {
					xtype: 'button',
					margin: '8 4',
					minWidth: 100,
					enableToggle: true
				},
				items: [{
					text: 'Dairy',
					toggleHandler: function(self, pressed) {
						
						let container = me.down("#bloof-1");
						if (pressed) {
							container.setHeight(0);
							container.setVisible(true)
							container.animate({
								dynamic: true,
								to: {
									height: 230
								}
							});
						} 
						else {
							me.setHeight(null)
							container.animate({
								dynamic: true,
								to: {
									height: 0
								},
								callback: function() {
									container.setVisible(false);
								}
							});
						}
					}
				},{
					text: 'Beef',
					toggleHandler: function(self, pressed) {
						me.setHeight(null)
						me.down("#bloof-2").setVisible(pressed)
					}
				}]
			},{
				xtype: 'container',
				//width: '100%',
				//flex: 1,
				margin: 8,
				scrollable: 'y',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				items: [{
					itemId: 'bloof-1',
					xtype: 'component',
					hidden: true,
					height: 230,
					style: 'background-color: red'
				},{
					itemId: 'bloof-2',
					xtype: 'component',
					hidden: true,
					height: 230,
					style: 'background-color: yellow'
				}]	
			}]
			// Add Dairy
			// 	number of lactating cows
			// 	number of dry cows
			//  number of heifers
			//	number of youngstock
			//	- Lactating Cows
			//		Grazing Time (hours/day)
			//		Grazing Period (months/year)
			//		Confined Period (months/year)
			//	- Non-Lactating Cows
			//		Grazing Time (hours/day)
			//		Grazing Period (months/year)
			//		Confined Period (months/year)
			// Add Beef
			//	number of beef cow
			//	number of stockers
			//	number of finishing
			//	- Beef
			//		Grazing Time (hours/day)
			//		Grazing Period (months/year)
			//		Confined Period (months/year)
		});
		
		me.callParent(arguments);
		
		AppEvents.registerListener("viewport_resize", function(opts) {
			me.center();
		})
	},
	
});
