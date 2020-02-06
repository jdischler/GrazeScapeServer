
//------------------------------------------------------------------------------
Ext.define('DSS.controls.LogoBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.logo_base',

	layout: 'fit',
	padding: '8 0 0 0',
	height: 140,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				style: 'background-image: url("assets/images/graze_logo.png"); background-size: contain; background-repeat: no-repeat',
			}]
		});
		
		me.callParent(arguments);
	}

});
