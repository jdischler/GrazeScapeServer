
//------------------------------------------------------------------------------
Ext.define('DSS.controls.MapLegend', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.map_legend',

	layout: 'absolute',

	width: 64,
	height: 256,
	floating: true,
	shadow: false,

	margin: 8,
	DSS_colors: [],
	DSS_values: [],
	
	style: 'opacity: 0.8;background-color: rgba(0,0,0,0.5); border: 1px solid rgba(0,0,0,0.1); border-radius: 4px; box-shadow: 0 6px 8px rgba(0,0,0,0.2); pointer-events: none',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		let usableHeight = me.height - 12 * 2;
		let usableWidth = me.width - 6 * 2;
		
		let chipHeight = usableHeight / me.DSS_colors.length;
		let atX = 6, atY = 12;
		
		let elements = [];
		Ext.each(me.DSS_colors, function(it) {
			elements.push({
				xtype: 'component',
				style: 'border: 1px solid rgba(0,0,0,0.25); background-color:' + it,
				x: atX,
				y: atY,
				width: usableWidth,
				height: chipHeight
			});
			atY += chipHeight;
		})

		atY = 3;
		Ext.each(me.DSS_values, function(it) {
			elements.push({
				xtype: 'component',
				style: 'color: white; text-shadow: 1px 0 0 black, -1px 0 0 grey, 0 1px 0 black, 0 -1px 0 grey; text-align: right; font-weight: bold',
				x: atX,
				y: atY,
				width: usableWidth-2,
				height: chipHeight,
				html: Ext.util.Format.number(it, '0.0#')
			});
			atY += chipHeight;
		})
		
		Ext.applyIf(me, {
			items: elements
		});
		
		me.callParent(arguments);
	}

});

