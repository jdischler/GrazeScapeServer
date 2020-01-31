package controllers;

import javax.inject.Inject;

import data_types.Farm;
import play.mvc.*;
import utils.ServerStartup;

//• On connect:
//	• Create new user id if needed (stored in cookies)

//• Assign any short-term scenario resources:
//	• Main one might be a clone of the farm data set which the user can then modify
//	• Secondary one might be the current scenario itself so the user has the option of refreshing the browser but still
//		keeping their scenario setup
//	• Another might be a copy of the assumptions, again so they could be cached for a browser refresh?

//	• Client changes:
//	• Alert user about cookie policy (cookies required). Specifically, cookies are used to link your scenarios to the
//		computing resources that will process the scenario. Cookies may be used for research purposes 
//		(linking multiple scenario runs...ie, tracking what types of things users are doing)

//	• Render assigned farm data set points
//	• When farm data set changes, need to send new to client and redraw
//	• Scenario setups could be sent more regularly and stored in the session cache?

// 	• dynamic masking may be doable with: https://github.com/come/csg2d.js/

//	• general design changes: store things like job keys, scenario keys, and similar in cookies?

/*SmartScape 2.0 Design Considerations

Performance / Resource Management:
	•	Unbounded access to CPU and MEM can't be allowed in the future
	•	Dealing with disk asset cleanup
	•	Overall reduction of disk footprint for temporary-ish results
	•	Efficiently managing models that can communicate with other models and/or rely on another model for results before doing its own work
	•	Area of Interest (AOI) complicates this. Unload Areas when they are not referenced for some time?

User / Website Related:
	•	ADA accessibility considerations (visually impaired) and color blind
	•	Cookie transparency, cookie user agreements, general cookie policy
	•	Google analytics tracking concerns
	•	Registered user features complications. Requires tracking email addresses, storing passwords, risk of data breach
	•	Mobile vs. desktop layout considerations. Mobile design likely needs to be hugely different and overall simplified
	•	Lack of level-of-detail improvements to selection/heatmap displays during map zoom
	•	Satellite map layer requirements. Most likely paid unless we host Open Street Maps data here. Which then opens up the requirement to periodically update that to maintain accuracy. Plus additional server resources (map server) and setup complications
	•	Reliance on ExtJs libraries and potential for licensing requirements

Area of Interest / subsetting / Selection / Queries:
	•	requirement for full datasets for each area of interest (AOI)
	•	ease of defining new areas of interests, potentially outside of Wisconsin
	•	Inter-screen communication between Portal and Application
	•	Where do selection transforms go? Such as procedural fraction, buffering, critical max?
	•	Selection transforms are order dependent. Subset most likely should be after Critical Max but other than that...Is there a fixed and generally most-useful ordering we should do? 
	•	Layers are currently looked up by name, not by a composite AOI and name...or some other suitable scheme

Models:
	•	Effort to develop new models.
	•	General complexity of model modules communicating and sharing results.
	•	Do we need to support the potential for one or more models to not be relevant or available based on area and/or lack of input data
	•	Provide feedback on overall model run progress
	•	Potentially have a model run queue and feedback to the end-user where they are in the run queue
	
Farms:
	•	Most likely need a master set of farms and then a way to filter it down to a (large?) radius buffered selection from the watershed / county AOI for the user.
	 	For a large buffering radius, this won't be super fast. However, the results could be stored as just the set of ID's with the idea that
	 	a working set of user modifiable Farms can be created as needed without the need to repeat the selection.
	 	
*/

//------------------------------------------------------------------
public class HomeController extends Controller {
	
	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
	}
	
	//------------------------------------------------------------------
	public Result app() {

		return ok(views.html.app.render());
	}
	
	public Result getFarms() {
		return ok(Farm.getAllAsGeoJson());
	}
}
