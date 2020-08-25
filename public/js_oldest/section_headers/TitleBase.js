
DSS.utils.addStyle('.lightweight-title { color: #888; font-size: 1rem; text-align: center;}')
DSS.utils.addStyle('.primary-title { color: #27b; font-size: 1.2rem; line-height: 1.6rem; text-align: center; font-weight: bold}')
DSS.utils.addStyle('.primary-title i { opacity: 0.3; cursor: pointer }')
DSS.utils.addStyle('.primary-title i:hover { opacity: 1}')

//------------------------------------------------------------------------------
Ext.define('DSS.section_headers.TitleBase', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.title_base',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	padding: 8,
	
	DSS_primaryTitle: 'New Operation',
	DSS_secondaryTitle: 'Operation',
		
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
				html: me.DSS_secondaryTitle
			},{ 
				xtype: 'component',
				cls: 'primary-title',
				html: me.DSS_primaryTitle
			}]
		});
		
		me.callParent(arguments);
	}
	
});
