Ext.tip.QuickTipManager.init(true, {shadow: false}); // Instantiate the QuickTipManager

Ext.Loader.setConfig({
	enabled: true,
	paths: {
//		'GeoExt': '/assets/javascripts/vendor/geo-ext',
	}
});

//--------------------------------------------
Ext.application({
	name: 'DSS',
	views: [
		'AppViewport'
	],
	mainView: 'DSS.view.AppViewport',
	init: function() {
		Ext.state.Manager.setProvider(
			Ext.create('Ext.state.CookieProvider', {
				path: '/'
			})
		);
	},
	

	// Routes handling....
    routes: {
    	'browse_or_create': 'browse_or_create'
    },
    listen: {
        controller: {
            '#': {
                unmatchedroute: 'onUnmatchedRoute'
            }
        }
    },

    browse_or_create: function() {
    	alert('hello:' + location.origin);
    	
    },
    onUnmatchedRoute: function(hash) {
    	alert('badness');
        console.log('Unmatched', hash);
     
    },
});
