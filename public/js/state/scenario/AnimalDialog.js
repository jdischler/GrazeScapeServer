
DSS.utils.addStyle('.sub-container {background-color: rgba(180,180,160,0.1); border-radius: 8px; border: 1px solid rgba(0,0,0,0.2); margin: 4px}')

let rotationFreq = Ext.create('Ext.data.Store', {
	fields: ['label', 'enum'],
	autoLoad: true,
	proxy: {
		type: 'ajax',
		url: '/get_options',
		reader: 'json',
		extraParams: {
			type: 'rotationalFrequency'
		}
	}
});

//------------------------------------------------------------------------------
Ext.define('DSS.state.scenario.AnimalDialog', {
//------------------------------------------------------------------------------
	extend: 'Ext.window.Window',
	alias: 'widget.state_animal_dialog',
	
	autoDestroy: false,
	closeAction: 'hide',
	constrain: true,
	modal: true,
	width: 832,
	resizable: false,
	bodyPadding: 8,
	titleAlign: 'center',
	
	title: 'Configure Animals',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		//--------------------------------------------
		// Dairy Container
		//--------------------------------------------
		let dairyContainer = {
			xtype: 'container',
			layout: 'fit',
			items: [{
				xtype: 'container',
				itemId: 'dairy-section',
				cls: 'sub-container',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				hidden: true,
				items: [{
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'Configure the size of the Dairy herd',
					margin: '0 32',
				},{
					xtype: 'container',
					layout: DSS.utils.layout('vbox', 'start', 'middle'),
					margin: 8,
					defaults: {
						xtype: 'numberfield',
						value: 20,
						minValue: 0,
						step: 10,
						labelAlign: 'right',
						labelWidth: 100,
						width: 200,
					},
					items: [{
						fieldLabel: 'Lactating Cows',
						bind: '{dairy.lactating}'
					},{
						fieldLabel: 'Dry Cows',
						bind: '{dairy.dry}'
					},{
						fieldLabel: 'Heifers',
						bind: '{dairy.heifers}'
					},{
						fieldLabel: 'Youngstock',
						bind: '{dairy.youngstock}'
					}]
				},{//----------------------------------------------------------------
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'Specify the Average Daily Milk Yield',
					margin: '0 32',
				},{
					xtype: 'numberfield',
					width: 200,
					margin: '8 82',
					fieldLabel: 'Milk Yield (lb/day/Cow)',
					labelAlign: 'right',
					labelWidth: 148,
					bind: '{dairy.daily-yield}',
					minValue: 1,
					step: 0.5,
				},{ //------------------------------------------------------------
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'How are the Lactating cattle managed?',
					margin: '0 32',
				},{
					xtype: 'container',
					itemId: 'lactating-cattle',
					layout: DSS.utils.layout('vbox', 'start', 'middle'),
					margin: 8,
					items: [{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							itemId: 'grazed-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						},{
							xtype: 'slider',
							width: 200,
							minValue: 0,
							maxValue: 12,
							bind: '{dairy.lactating-confined}',
							step: 1,
							listeners: {
								change: function(slider, newValue) {
									slider.up().down('#grazed-display').update('' + (12 - newValue));
									slider.up().down('#confined-display').update('' + newValue);
									Ext.each(
										Ext.ComponentQuery.query('[dssID=if-grazed]', me.down('#lactating-cattle')),
										function(item) {
											item.setDisabled(newValue == 12);
										}
									);
								}
							},
							tipText: function(thumb)  {
								const v = thumb.slider.getValue();
								return 12 - v + " / " + v;
							}
						},{
							xtype: 'component',
							itemId: 'confined-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						}]
					},{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Grazed'
						},{
							xtype: 'component',
							cls: 'information-compact med-text',
							width: 180,
							html: 'Period (mo / yr)'
						},{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Confined'
						}]
					},{
						xtype: 'numberfield',
						dssID: 'if-grazed',
						bind: '{dairy.lactating-graze-time}',
						minValue: 0,
						maxValue: 12,
						step: 1,
						labelAlign: 'right',
						labelWidth: 140,
						width: 240,
						fieldLabel: 'Grazing Time (h/d)',
						maxValue: 24
					},{
						xtype: 'combo',
						dssID: 'if-grazed',
						fieldLabel: 'Rotational Frequency',
						labelWidth: 140,
						width: 240,
						labelAlign: 'right',
						mode: 'remote',
						triggerAction: 'all',
						store: rotationFreq,
						displayField: 'label',
						valueField: 'enum',
						bind: '{dairy.lactating-rotation-freq}',
					}]
				},{ //------------------------------------------------------------
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'How are the Non-Lactating cattle managed?',
					margin: '0 32',
				},{
					xtype: 'container',
					itemId: 'non-lactating-cattle',
					layout: DSS.utils.layout('vbox', 'start', 'middle'),
					margin: 8,
					items: [{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							itemId: 'grazed-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						},{
							xtype: 'slider',
							width: 200,
							minValue: 0,
							maxValue: 12,
							bind: '{dairy.non-lactating-confined}',
							step: 1,
							listeners: {
								change: function(slider, newValue) {
									slider.up().down('#grazed-display').update('' + (12 - newValue));
									slider.up().down('#confined-display').update('' + newValue);
									Ext.each(
										Ext.ComponentQuery.query('[dssID=if-grazed]', me.down('#non-lactating-cattle')),
										function(item) {
											item.setDisabled(newValue == 12);
										}
									);
								}
							},
							tipText: function(thumb)  {
								const v = thumb.slider.getValue();
								return 12 - v + " / " + v;
							}
						},{
							xtype: 'component',
							itemId: 'confined-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						}]
					},{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Grazed'
						},{
							xtype: 'component',
							cls: 'information-compact med-text',
							width: 180,
							html: 'Period (mo / yr)'
						},{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Confined'
						}]
					},{
						xtype: 'numberfield',
						dssID: 'if-grazed',
						bind: '{dairy.non-lactating-graze-time}',
						minValue: 0,
						maxValue: 12,
						step: 1,
						labelAlign: 'right',
						labelWidth: 140,
						width: 240,
						fieldLabel: 'Grazing Time (h/d)',
						maxValue: 24
					},{
						xtype: 'combo',
						dssID: 'if-grazed',
						fieldLabel: 'Rotational Frequency',
						labelWidth: 140,
						width: 240,
						labelAlign: 'right',
						mode: 'remote',
						triggerAction: 'all',
						store: rotationFreq,
						displayField: 'label',
						valueField: 'enum',
						bind: '{dairy.non-lactating-rotation-freq}',
					}]
				}]
			}]
		};
		
		//------------------------------------------------------------------
		// Beef Container
		//------------------------------------------------------------------
		let beefContainer = {
			xtype: 'container',
			layout: 'fit',
			items: [{
				xtype: 'container',
				itemId: 'beef-section',
				cls: 'sub-container',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				hidden: true,
				items: [{
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'Configure the size of the Beef herd',
					margin: '0 32',
				},{
					xtype: 'container',
					layout: DSS.utils.layout('vbox', 'start', 'middle'),
					margin: 8,
					defaults: {
						xtype: 'numberfield',
						minValue: 0,
						step: 10,
						labelAlign: 'right',
						labelWidth: 100,
						width: 200,
					},
					items: [{
						fieldLabel: 'Beef Cows',
						bind: '{beef.cows}'
					},{
						fieldLabel: 'Stockers',
						bind: '{beef.stockers}'
					},{
						fieldLabel: 'Finishers',
						margin: '0 0 34 0',
						bind: '{beef.finishers}'
					}]
				},{//----------------------------------------------------------------
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'Specify the Average Daily Weight Gain',
					margin: '0 32',
				},{
					xtype: 'numberfield',
					width: 200,
					margin: '8 82',
					fieldLabel: 'Daily Gain (lb/day/AU)',
					labelAlign: 'right',
					labelWidth: 140,
					bind: '{beef.daily-gain}',
					minValue: 0.25,
					step: 0.25,
				},{ //-------------------------------------------------------------
					xtype: 'component',
					cls: 'information accent-text box-underline',
					html: 'How are the Beef cattle managed?',
					margin: '0 32',
				},{
					xtype: 'container',
					itemId: 'beef-cattle',
					layout: DSS.utils.layout('vbox', 'start', 'middle'),
					margin: 8,
					items: [{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							itemId: 'grazed-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						},{
							xtype: 'slider',
							width: 200,
							minValue: 0,
							maxValue: 12,
							bind: '{beef.confined}',
							step: 1,
							listeners: {
								change: function(slider, newValue) {
									slider.up().down('#grazed-display').update('' + (12 - newValue));
									slider.up().down('#confined-display').update('' + newValue);
									Ext.each(
										Ext.ComponentQuery.query('[dssID=if-grazed]', me.down('#beef-cattle')),
										function(item) {
											item.setDisabled(newValue == 12);
										}
									);
								}
							},
							tipText: function(thumb)  {
								const v = thumb.slider.getValue();
								return 12 - v + " / " + v;
							}
						},{
							xtype: 'component',
							itemId: 'confined-display',
							cls: 'information',
							padding: 4,margin: '0 4',
							width: 32,
							style: 'border: 1px solid rgba(0,0,0,0.1); background-color: white; border-radius: 2px',
						}]
					},{
						xtype: 'container',
						width: undefined,
						layout: DSS.utils.layout('hbox', 'center'),
						items: [{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Grazed'
						},{
							xtype: 'component',
							cls: 'information-compact med-text',
							width: 180,
							html: 'Period (mo / yr)'
						},{
							xtype: 'component',
							width: 64,
							cls: 'information accent-text bold',
							html: 'Confined'
						}]
					},{
						xtype: 'numberfield',
						dssID: 'if-grazed',
						bind: '{beef.graze-time}',
						minValue: 0,
						maxValue: 12,
						step: 1,
						labelAlign: 'right',
						labelWidth: 140,
						width: 240,
						fieldLabel: 'Grazing Time (h/d)',
						maxValue: 24
					},{
						xtype: 'combo',
						dssID: 'if-grazed',
						fieldLabel: 'Rotational Frequency',
						labelWidth: 140,
						width: 240,
						labelAlign: 'right',
						mode: 'remote',
						triggerAction: 'all',
						store: rotationFreq,
						displayField: 'label',
						valueField: 'enum',
						bind: '{beef.rotation-freq}',
					}]
				}]
			}]
		};
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				padding: 4,
				cls: 'information med-text',
				html: 'Click to choose the types of animals present at this operation'
					
			},{
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center'),
				defaults: {
					xtype: 'button',
					margin: '8 4',
					minWidth: 100,
					enableToggle: true
				},
				items: [{//--------------------------------------------------------------------------
					text: 'Dairy',
					toggleHandler: function(self, pressed) {
						let container = me.down("#dairy-section");
						if (pressed) {
							container.setHeight(0);
							container.setVisible(true)
							container.animate({
								dynamic: true,
								to: {
									height: 550
								}
							});
						} 
						else {
							me.setHeight(null)
							container.animate({
								dynamic: true,
								to: {
									height: 0
								},
								callback: function() {
									container.setVisible(false);
								}
							});
						}
					}
				},{ //--------------------------------------------------------------------------
					text: 'Beef',
					toggleHandler: function(self, pressed) {
						let container = me.down("#beef-section");
						if (pressed) {
							container.setHeight(0);
							container.setVisible(true)
							container.animate({
								dynamic: true,
								to: {
									height: 390
								}
							});
						} 
						else {
							me.setHeight(null)
							container.animate({
								dynamic: true,
								to: {
									height: 0
								},
								callback: function() {
									container.setVisible(false);
								}
							});
						}
					}
				}]
			},{//------------------------------------------------------------------
				xtype: 'container',
				layout: DSS.utils.layout('hbox', 'center'),
//				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				defaults: {
					width: 400,
				},
				items: [
					dairyContainer,
					beefContainer
				]	
			}]
		});
		
		me.callParent(arguments);
		
		AppEvents.registerListener("viewport_resize", function(opts) {
			me.center();
		})
	},
	
});
