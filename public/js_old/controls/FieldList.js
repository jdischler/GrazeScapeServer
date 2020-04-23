
DSS.utils.addStyle('.trash:hover {color: #b21; text-shadow: 0 -1px 1px white, 0 0 5px rgba(255,0,0,0.5); }')

//------------------------------------------------------------------------------
Ext.create('Ext.data.Store', {
//------------------------------------------------------------------------------
	
	storeId: 'field_data',
	fields: [{
		name: 'id', type: 'number'
	},{
		name: 'name', type: 'string'
	},{
		name: 'acres', type: 'number'
	},{
		name: 'p-loss-ac', type: 'number'
	},{
		name: 'p-loss-total', type: 'number'
	}],
	data: [{
		id: 1, name: 'field 1', acres: 30,
	},{
		id: 2, name: 'field 2', acres: 20, 
	},{
		id: 3, name: 'field 3', acres: 10,
	},{
		id: 4, name: 'field 4', acres: 20, 
	},{
		id: 5, name: 'field 5', acres: 10, 
	}]
});

//------------------------------------------------------------------------------
Ext.define('DSS.controls.FieldList', {
//------------------------------------------------------------------------------
	extend: 'Ext.grid.Panel',
	alias: 'widget.field_list',

	store: 'field_data',
	listeners: {
		select: function(grid, record, idx) {
			let collection = DSS.selectionTool.getFeatures();
			DSS.layer.fields.getSource().forEachFeature(function(f) {
				if (f.get('f_id') == record.get('id')) {
					collection.clear();
					collection.push(f);
					return true;
				}
			});
		}
	},
	columns: [{
		text: 'Name',
		hideable: false, menuDisabled: true,
		dataIndex: 'name',
		flex: 1
	},{
		xtype: 'numbercolumn',
		hideable: false, menuDisabled: true,
		text: 'Acres',
		dataIndex: 'acres',
		format: '0,0.00',
		minWidth: 110,
	},{
		xtype: 'numbercolumn',
		hideable: false, menuDisabled: true,
		text: 'P-Loss / Ac',
		dataIndex: 'p-loss-ac',
		minWidth: 110,
		format: '0,0.000'
	},{
		xtype: 'numbercolumn',
		hideable: false, menuDisabled: true,
		text: 'P-Loss Total',
		dataIndex: 'p-loss-total',
		minWidth: 110,
		format: '0,0.00'
	},{
		xtype: 'actioncolumn',
		sortable: false, draggable: false, hideable: false, menuDisabled: true,
		width: 56,
		items: [{
			iconCls: "far fa-trash-alt trash",
			tooltip: 'Delete this field',
			handler: function(view, row, col, self, evt, rec) {
				Ext.Msg.confirm('Delete this field?', 'This process cannot be undone. Continue?', function(btn) {
					if (btn === 'yes') {
						//TODO: delete the field 
						console.log("Todo: delete the field");
					}
				});
			}
		}]
	}],
	
	layout: 'fit',
	resizable: {
		dynamic: true,
	},
	resizeHandles: 'n',
	height: 0,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		DSS.fieldList = me;
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
	},
	
	//----------------------------------------------------------
	open: function() {
		let me = this;
		
		me.animate({
			dynamic: true,
			to: {
				height: 140
			},
			callback: function() {
				me.setMinHeight(90);
				// ooof, the Ext resizer doesn't seem to realize when its resize target has a min/max width change
				me.resizer.resizeTracker.minHeight = 90;
			}
		});
	},
	
	//----------------------------------------------------------
	addStats: function(mode, fields) {

		let me = this;
		
		if (mode != 'p-loss') return;

		if (me.getHeight() <= 0) {
			me.open();
		}
		
		let store = me.getStore();
		
		let records = [];
		let fid = 1;
		Ext.Object.each(fields, function(key, value) {
			let ac = value.area / 4046.8564224; //ol.sphere.getArea(f.getGeometry())
			records.push({
				id: key,
				name: 'field' + key,
				acres: ac, //ol.sphere.getArea(f.getGeometry())
				'p-loss-ac': value.mean,
				'p-loss-total': value.mean * ac
			});
		})
		store.loadRawData(records);
//		store.commitChanges();
	}

});
