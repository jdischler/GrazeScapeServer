
// TODO: rename? Is this only local state? 
//------------------------------------------------------------------------------
Ext.define('DSS.data.ApplicationState', {
	extend: 'Ext.Base',
    alternateClassName: 'ApplicationState',
    singleton: true,	
	    
    //----------------------------------------
    activateOperation: function(operation_id) {
    
    	// TODO: communicate to server? Also set in local cookies?
    	
    	return getActiveOperation();
    },
    
    //----------------------------------------
    getActiveOperation: function() {
    	
    	// TODO: populate
    	return {
    		id: 1,
    		name: 'todo'
    	}
    	
    },
    
    //----------------------------------------
    getOperationsList: function(parms_todo_if_needed) {
    	
    	// TODO: get operations
    	return [{
    		id: 1, name: 'test'
    	},{
    		id: 4, name: 'yes'
    	}]
    }
   
});
