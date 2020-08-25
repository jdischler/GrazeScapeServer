
//--------------------------------------------------------------------------
var filter_task = new Ext.util.DelayedTask(function(){
	if (!DSS.layer) return;
	DSS.Inspector.computeResults(undefined, DSS.layer.ModelResult);
});

var DSS_Refilter = function() {
	filter_task.delay(0);
}
var DSS_RefilterDelayed = function(msDelay) {
	filter_task.delay(msDelay);
}

//------------------------------------------------------------------------------
Ext.define('DSS.inspector.Main', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector',
    alternateClassName: 'DSS.Inspector',
    singleton: true,	
    autoDestroy: false,
	
    requires: [
    	'DSS.inspector.DataBounds',
    	'DSS.inspector.DataSource',
    	'DSS.inspector.RestrictResults'    	
    ],
    
    scrollable: 'y',

	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
				xtype: 'component',
			},
			items: [{
				cls: 'section-title light-text text-drp-20',
				html: 'Inspector <i class="fas fa-search font-10 accent-text text-drp-50"></i>',
				height: 35				
			},{
				xtype: 'inspector_data_bounds',
			},{
				xtype: 'inspector_data_source',
				itemId: 'dss-data-source',
				DSS_Parent: me
			},{
				xtype: 'inspector_restrict_results',
				itemId: 'dss-resrictor'
			}]
		});
		
		me.callParent(arguments);
//		me.setMode('crop-yield');
		
		AppEvents.registerListener('set_inspector_bounds', function(extents, silent) {
			if (!silent) {
				me.computeResults(extents,DSS.layer.ModelResult);
			}
		})
		
	},
	
	//--------------------------------------------------------------------------
	addModeControl: function() {
		let me = this;
		let c = DSS_viewport.down('#DSS-mode-controls');
		
		if (!c.items.has(me)) {
			Ext.suspendLayouts();
				c.removeAll(false);
				c.add(me);
			Ext.resumeLayouts(true);
		}
	},
	
	//-------------------------------------------------------------------------------------------------
	inspectorOptionsChanged: function(options, silent) {
		let me = this;

		me.DSS_options = options;
		if (!silent) {
			DSS_Refilter();//
//			me.computeResults(undefined, DSS.layer ? DSS.layer.ModelResult : undefined);
		}
	},
	
	//-------------------------------------------------------------------------------------------------
	setMode: function(mode) {
		let me = this;
		
		me.DSS_mode = mode;
	},
	
	//-------------------------------------------------------------------------------------------------
	computeResults: function(extents, modelResultsLayer) {
		let me = this;
		
		// TODO: busy feedback
		if (me.DSS_isWorking) {
			DSS_RefilterDelayed(500);
			return;
		}
		
		if (!extents) {
			extents = me.DSS_extents;
		}
		
		if (!extents) {
			console.log("nothing to do right now?");
			return;
		}
		if (!me.DSS_mode) me.DSS_mode = 'slope';//crop-yield';
		
		me['DSS_extents'] = extents;
	
		me.DSS_isWorking = true;
		
		let options = me.down('#dss-data-source').getOptions();
		let restrictions = me.down('#dss-resrictor').getRestrictions();
		
		let data = {
			"extent": extents,
			"model": me.DSS_mode,
			"options": options,
			"restrictions": restrictions,
		};
	//	console.log(data);
		
		var obj = Ext.Ajax.request({
			url: location.origin + '/fetch_image',
			jsonData: data,
			timeout: 10000,
			success: function(response, opts) {
				var obj = JSON.parse(response.responseText);
				me.DSS_isWorking = false;

				console.log("-----------------");
				console.log(obj);
				modelResultsLayer.setSource(new ol.source.ImageStatic({
					url: obj.url,
					imageExtent: obj.extent,
					projection: 'EPSG:3071',
					imageSmoothing: false

				}))
				modelResultsLayer.setVisible(true);	
				
				if (obj.key) {
					DSS.MapState.showClassifiedLegend(obj.key)
				}
				else {
					DSS.MapState.showContinuousLegend(obj.palette, obj.values);
				}
				
				if (obj.fields) {
			//		DSS.fieldList.addStats(me.DSS_mode, obj.fields)
				}
			},
			
			failure: function(response, opts) {
				me.DSS_isWorking = false;
			}
		});
	},
	
	extractData: function(data, crop, toDat) {
		
		let d = data['model-results'];
		if (!d) return;
		d = d.crops;
		d = d[crop].totals.histogram;
		
		let v = "";
		let dv = d.values, ah = d['area-ha'];
		for (let i = 0; i < dv.length; i++) {
			
			v += "{'datax_" + toDat + "':" + dv[i] + ", 'datay_"+ toDat + "':" + ah[i] + "},";
		}
		console.log(v);
	}

});

