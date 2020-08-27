
//------------------------------------------------------------------------------
Ext.define('DSS.state.scenario.AnimalDialog', {
//------------------------------------------------------------------------------
	extend: 'Ext.window.Window',
	alias: 'widget.state_animal_dialog',
	
	autoDestroy: false,
	closeAction: 'hide',
	constrain: true,
//	floating: true,
//	shadow: false,
	modal: true,
	margin: 32,
	width: 480,
	minHeight: 320,
	resizable: true,
	
	title: 'Configure animals',
	
//	style: 'border-radius: 2px; box-shadow: 0 4px 8px rgba(0,0,0,0.5);',
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
		
		AppEvents.registerListener("viewport_resize", function(opts) {
			me.center();
		})
	},
	
});
