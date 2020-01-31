Ext.tip.QuickTipManager.init(); // Instantiate the QuickTipManager

Ext.Loader.setConfig({
	enabled: true,
	paths: {
//		'GeoExt': '/assets/javascripts/vendor/geo-ext',
	}
});

Ext.application({
	name: 'DSS',
	views: [
		'AppViewport'
	],
	mainView: 'DSS.view.AppViewport',
	init: function() {
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	},	
});
