
DSS.utils.addStyle('.drop-box {background-color: #ddd; border: 2px dashed #fff; border-radius: 6px;}')
DSS.utils.addStyle('.drag-over {color: #137; background-color: #ccc!important; border: 2px dashed #fff; border-radius: 6px;}')

//------------------------------------------------------------------------------
Ext.define('DSS.app.Shapes_Upload', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.shapes_upload',
	
//	layout: 'fit',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				padding: '16 32',
				//style: 'background-color: #ddd; border: 2px dashed #fff; border-radius: 6px',
				cls: 'drop-box',
				html: 'Drop Files Here',
				listeners: {
					drop: {
						element: 'el', fn: me.drop
					},
					dragstart: {
						element: 'el', fn: me.addDropZone
					},
					dragenter: {
						element: 'el', fn: me.addDropZone
					},
					dragover: {
						element: 'el', fn: me.addDropZone
					},
					dragleave: {
						element: 'el', fn: me.removeDropZone
					},
					dragexit: {
						element: 'el', fn: me.removeDropZone
					}
				}
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	addDropZone: function(e) {
		let me = this;
		
		if (!e.browserEvent.dataTransfer || Ext.Array.from(e.browserEvent.dataTransfer.types).indexOf('Files') === -1) {
			return;
		}
		
		e.stopEvent();
		
		me.addCls('drag-over');
	},

    //--------------------------------------------------------------------------
    removeDropZone: function(e) {
    	let me = this;
		let el = e.getTarget(),
			meEl = me.el;//();

		e.stopEvent();

		if (el === meEl.dom) {
			me.removeCls('drag-over');
			return;
		}

        while (el !== meEl.dom && el && el.parentNode) {
            el = el.parentNode;
        }

        if (el !== meEl.dom) {
            me.removeCls('drag-over');
        }
    },

	//--------------------------------------------------------------------------
	drop: function(e) {
		let me = this;
		
		e.stopEvent();
		Ext.Array.forEach(Ext.Array.from(e.browserEvent.dataTransfer.files), function(file, idx) {
			console.log(file);
//			me.postDocument("insert your upload url here", file, idx);
		});
		me.removeCls('drag-over');
		
	},

	//--------------------------------------------------------------------------
	postDocument: function(url, file, i) {
		let xhr = new XMLHttpRequest();
		let fd = new FormData();
		
		fd.append("serverTimeDiff", 0);
		xhr.open("POST", url, true);
		fd.append('index', i);
		fd.append('file', file);
		
		//xhr.setRequestHeader("Content-Type","multipart/form-data");
		xhr.setRequestHeader("serverTimeDiff", 0);
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4 && xhr.status == 200) {
				//handle the answer, in order to detect any server side error
				if (Ext.decode(xhr.responseText).success) {
					// Uploaded
				} else {
					// Error
				}
			} else if (xhr.readyState == 4 && xhr.status == 404) {
				// Error
			}
		};
		// Initiate a multipart/form-data upload
		xhr.send(fd);
	}

});
