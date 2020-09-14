
DSS.utils.addStyle('.drop {overflow: visible!important}');
DSS.utils.addStyle('.drop:after {overflow: visible!important; display: block; position: absolute; bottom: -8px; left: calc(50% - 8px); content: ""; background-color: transparent; border-top: 8px solid #666; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent;}');

//------------------------------------------------------------------------------
Ext.define('DSS.inspector.DataSource', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.inspector_data_source',

	requires: [
		'DSS.inspector.sourceParameters.Slope',
		'DSS.inspector.sourceParameters.Yield',		
		'DSS.inspector.sourceParameters.PLoss'		
	],
	style: 'background-color: #666; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
	cls: 'drop',
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	margin: '8 4',
	padding: '2 8',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		//-------------------------------------------------------------
		function makeInspectionMode(buttonText, modeName, disabled, control, options) {
			return {
				padding: '0 0 0 16',
				text: buttonText,
				disabled: disabled,
				handler: function(self) {
					self.parentMenu.DSS_ownerButton.setText(buttonText);
					// FIXME: let parent pull instead of the child pushing...
					me.DSS_Parent.setMode(modeName);
					Ext.suspendLayouts();
					me.down('#mode-options').removeAll(false);
					if (control) {
						if (!self.DSS_dataParameters) {
							let cfg = Ext.merge({
								xtype: control,
								autoDestroy: false,
								listeners: {
									added: function(ctrl) {
										DSS_RefilterDelayed(50);
									}
								}
							}, options);
							
							self.DSS_dataParameters = Ext.create(cfg)
						}
						me.down('#mode-options').add(self.DSS_dataParameters);
					}
					else {
						DSS_RefilterDelayed(50);
					}
					Ext.resumeLayouts(true);
				}
			}
		};
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				cls: 'information light-text text-drp-20',
				html: 'Inspection Mode'
			},{
				xtype: 'button',
				cls: 'button-text-pad',
				text: 'Slope (DEM)',//Crop Yield',
				listeners: {
					afterrender: function(self) {
						self.setMenu({
							DSS_ownerButton: self,
							listeners: {
								beforeshow: function(menu) {
									menu.setWidth(self.getWidth());
								}
							},
							plain: true,
							//width: 160,
							items: [{
								text: 'GrazeScape / SmartScape Models', disabled: true,
								style: 'border-bottom: 1px solid rgba(0,0,0,0.2);',
							},
								makeInspectionMode('Soil Loss', 	'soil-loss', 	true, 'inspector_ploss'),
								makeInspectionMode('P-Loss', 		'p-loss', 		false, 'inspector_ploss'),
								makeInspectionMode('P-Loss by CDL', 'p-loss-real', 	false),//, 'inspector_p_loss'),
								makeInspectionMode('Crop Yield', 	'crop-yield', 	false, 'inspector_yield'),
								makeInspectionMode('Dry Matter', 	'dry-matter', 	false),
								makeInspectionMode('Bird Habitat', 	'bird-habitat'),
							{
								text: 'Land Properties (SSURGO)', disabled: true,
								style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
							}, 
								makeInspectionMode('WiscLand 2.0', 	'cdl', 		false),
								makeInspectionMode('Slope (DEM)', 	'slope', 	false, 'inspector_slope'),
								makeInspectionMode('Slope (SSURGO)', 'ssurgo-slope', 	false, 'inspector_slope'),//, {DSS_units:'º',DSS_min: 1, DSS_max: 45, DSS_value: 10, DSS_step: 2}),
								makeInspectionMode('Slope Length', 	'slope-length', false),//, 'inspector_limiter', {DSS_units:' ',DSS_min: 60, DSS_max: 250, DSS_value: 60, DSS_step: 10}),
								makeInspectionMode('Soil Depth', 	'soil-depth',false),//, 'inspector_limiter', {DSS_units:'cm',DSS_min: 0, DSS_max: 200, DSS_value: 60, DSS_step: 10}),
								makeInspectionMode('% Sand', 		'perc-sand', false),//, 'inspector_limiter', {DSS_units:'%',DSS_min: 0, DSS_max: 100, DSS_value: 30, DSS_step: 5}),
								makeInspectionMode('% Silt', 		'perc-silt', false),//, 'inspector_limiter', {DSS_units:'%',DSS_min: 0, DSS_max: 100, DSS_value: 30, DSS_step: 5}),
								makeInspectionMode('% Clay', 		'perc-clay', false),//, 'inspector_limiter', {DSS_units:'%',DSS_min: 0, DSS_max: 100, DSS_value: 30, DSS_step: 5}),
								makeInspectionMode('Distance to Water', 'dist-water', false),//, 'inspector_limiter', {DSS_units:'m',DSS_min: 0, DSS_max: 1000, DSS_value: 100, DSS_step: 10}),
								makeInspectionMode('Elevation', 	'dem', 		false),//, 'inspector_limiter',	{DSS_units:'ft',DSS_min: 650, DSS_max: 1400, DSS_value: 900, DSS_step: 25}),
								makeInspectionMode('pH', 			'ph', 		false),//, 'inspector_limiter',	{DSS_units:' ',DSS_min: 4.5, DSS_max: 8, DSS_value: 7, DSS_step: 0.5}),
								makeInspectionMode('OM', 			'om', 		false),//, 'inspector_limiter', {DSS_units:' ',DSS_min: 0, DSS_max: 100, DSS_value: 6, DSS_step: 3}),
								makeInspectionMode('cec', 			'cec', 		false),//, 'inspector_limiter', {DSS_units:'%',DSS_min: 0, DSS_max: 10, DSS_value: 2, DSS_step: 1}),
								makeInspectionMode('K', 			'k', 		false),//, 'inspector_limiter', {DSS_units:' ',DSS_min: 0, DSS_max: 1, DSS_value: 0.5, DSS_step: 0.1}),
								makeInspectionMode('ksat', 			'ksat', 	false),//, 'inspector_limiter', {DSS_units:' ',DSS_min: 0, DSS_max: 300, DSS_value: 20, DSS_step: 10}),
								makeInspectionMode('LS', 			'ls', 		false),//, 'inspector_limiter', {DSS_units:' ',DSS_min: 0, DSS_max: 20, DSS_value: 5, DSS_step: 1}),
								makeInspectionMode('T', 			't', 		false),//, 'inspector_limiter', {DSS_units:'t/a',DSS_min: 2, DSS_max: 0, DSS_value: 2, DSS_step: 1}),
							]
						});
					}
				}
			},{
				xtype: 'container',
				itemId: 'mode-options',
				layout: 'fit',
				margin: '4 0',
				items: [{
					xtype: 'inspector_slope'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------
	getOptions: function() {
	
		let me = this;
		let ch = me.down('#mode-options').child();
		
		if (ch && ch.getOptions && typeof ch.getOptions === 'function') {
			return ch.getOptions();
		}
	}
	
});
