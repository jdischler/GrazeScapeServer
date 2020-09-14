
//------------------------------------------------------------------------------
Ext.define('DSS.map.Legend', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.map_legend',

	layout: 'absolute',

	width: 54,
	height: 290,
	floating: true,
	shadow: false,

	margin: 8,
	DSS_colors: [], // for continuous;
	DSS_values: [],
	
	DSS_keys: false, // for categorical
	
	style: 'opacity: 0.8;background-color: rgba(0,0,0,0.5); border: 1px solid rgba(0,0,0,0.1); border-radius: 4px; box-shadow: 0 6px 8px rgba(0,0,0,0.2); pointer-events: none',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		let usableHeight = me.height - 12 * 2;
		let usableWidth = me.width - 6 * 2;
		
		let chipHeight = 0;
		let atX = 6, atY = 0;
		
		let elements = [];
		if (me.DSS_colors) {
			chipHeight = usableHeight / me.DSS_colors.length;
			atY = usableHeight - chipHeight + 12;
			Ext.each(me.DSS_colors, function(it) {
				elements.push({
					xtype: 'component',
					style: 'border: 1px solid rgba(0,0,0,0.25); border-bottom-color: rgba(0,0,0,0.5); background-color:' + it,
					x: atX,
					y: atY,
					width: usableWidth,
					height: chipHeight
				});
				atY -= chipHeight;
			})
	
			atY = /*3*/usableHeight + 3;
			Ext.each(me.DSS_values, function(it) {
				let fmt = '0.0#'
				if (it < 1) {fmt += "#"}
				else if (it > 999) fmt = "0";
				else if (it > 99) fmt = "0.#";
				
				elements.push({
					xtype: 'component',
					style: 'color: white; text-shadow: 1px 0 0 black, -1px 0 0 black, 0 1px 0 black, 0 -1px 0 rgba(0,0,0,0.5), 0 2px 4px black; text-align: right; font-weight: bold',
					x: atX,
					y: atY,
					width: usableWidth-2,
					height: chipHeight,
					html: Ext.util.Format.number(it, fmt)
				});
				atY -= chipHeight;
			})
		}
		else {
			chipHeight = usableHeight / me.DSS_keys.length;
			atY = 12;
			Ext.each(me.DSS_keys, function(it) {
				elements.push({
					xtype: 'component',
					style: 'cursor: pointer; border: 1px solid rgba(0,0,0,0.25); border-bottom-color: rgba(0,0,0,0.5); background-color:' + it.color,
					x: atX,
					y: atY,
					width: usableWidth,
					height: chipHeight,
					autoEl: {
						tag: 'div',
						'data-qtip': it.label
					}
				});
				atY += chipHeight;
			})
		}
		
		Ext.applyIf(me, {
			items: elements
		});
		
		me.callParent(arguments);
	}

});

