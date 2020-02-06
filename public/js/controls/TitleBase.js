
DSS.utils.addStyle('.lightweight-title { color: #888; font-size: 1rem; text-align: center;}')
DSS.utils.addStyle('.primary-title { color: #27b; font-size: 1.6rem; line-height: 2rem; text-align: center; font-weight: bold}')
DSS.utils.addStyle('.primary-title i { opacity: 0.3; cursor: pointer }')
DSS.utils.addStyle('.primary-title i:hover { opacity: 1}')

//------------------------------------------------------------------------------
Ext.define('DSS.controls.TitleBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.title_base',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	padding: 8,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'lightweight-title',
				html: 'Operation'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center', 'stretch'),
				defaults: {
					xtype: 'component',
					cls: 'primary-title',
				},				
				items: [{
					padding: '0 8rem',
					html: 'Grazing Acres'
				},{
					html: '<i class="fas fa-edit"></i>'
				}]
			}]
		});
		
		me.callParent(arguments);
	}

});
