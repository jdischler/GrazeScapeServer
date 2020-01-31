// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jdischler/Projects/GrazeScape_svr/conf/routes
// @DATE:Fri Jan 31 16:02:09 CST 2020

import play.api.mvc.Call


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:4
package controllers {

  // @LINE:4
  class ReverseHomeController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:4
    def app(): Call = {
    
      () match {
      
        // @LINE:4
        case ()  =>
          
          Call("GET", _prefix)
      
      }
    
    }
  
    // @LINE:6
    def getFarms(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "get_farms")
    }
  
  }

  // @LINE:9
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def at(file:String): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public/js"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "app/" + implicitly[play.api.mvc.PathBindable[String]].unbind("file", file))
    }
  
    // @LINE:9
    def versioned(file:Asset): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[play.api.mvc.PathBindable[Asset]].unbind("file", file))
    }
  
  }


}
