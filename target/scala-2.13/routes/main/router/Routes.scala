// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jdischler/Projects/GrazeScape_svr/conf/routes
// @DATE:Fri Jan 31 16:02:09 CST 2020

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:4
  HomeController_1: controllers.HomeController,
  // @LINE:9
  Assets_0: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:4
    HomeController_1: controllers.HomeController,
    // @LINE:9
    Assets_0: controllers.Assets
  ) = this(errorHandler, HomeController_1, Assets_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_1, Assets_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.app()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """app""", """controllers.HomeController.app()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """get_farms""", """controllers.HomeController.getFarms()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """app/""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public/js", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:4
  private[this] lazy val controllers_HomeController_app0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_app0_invoker = createInvoker(
    HomeController_1.app(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "app",
      Nil,
      "GET",
      this.prefix + """""",
      """""",
      Seq()
    )
  )

  // @LINE:5
  private[this] lazy val controllers_HomeController_app1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("app")))
  )
  private[this] lazy val controllers_HomeController_app1_invoker = createInvoker(
    HomeController_1.app(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "app",
      Nil,
      "GET",
      this.prefix + """app""",
      """""",
      Seq()
    )
  )

  // @LINE:6
  private[this] lazy val controllers_HomeController_getFarms2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("get_farms")))
  )
  private[this] lazy val controllers_HomeController_getFarms2_invoker = createInvoker(
    HomeController_1.getFarms(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "getFarms",
      Nil,
      "GET",
      this.prefix + """get_farms""",
      """""",
      Seq()
    )
  )

  // @LINE:9
  private[this] lazy val controllers_Assets_versioned3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned3_invoker = createInvoker(
    Assets_0.versioned(fakeValue[String], fakeValue[Asset]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      this.prefix + """assets/""" + "$" + """file<.+>""",
      """ Map static resources from the /public folder to the /assets URL path""",
      Seq()
    )
  )

  // @LINE:10
  private[this] lazy val controllers_Assets_at4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("app/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at4_invoker = createInvoker(
    Assets_0.at(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      this.prefix + """app/""" + "$" + """file<.+>""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:4
    case controllers_HomeController_app0_route(params@_) =>
      call { 
        controllers_HomeController_app0_invoker.call(HomeController_1.app())
      }
  
    // @LINE:5
    case controllers_HomeController_app1_route(params@_) =>
      call { 
        controllers_HomeController_app1_invoker.call(HomeController_1.app())
      }
  
    // @LINE:6
    case controllers_HomeController_getFarms2_route(params@_) =>
      call { 
        controllers_HomeController_getFarms2_invoker.call(HomeController_1.getFarms())
      }
  
    // @LINE:9
    case controllers_Assets_versioned3_route(params@_) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned3_invoker.call(Assets_0.versioned(path, file))
      }
  
    // @LINE:10
    case controllers_Assets_at4_route(params@_) =>
      call(Param[String]("path", Right("/public/js")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at4_invoker.call(Assets_0.at(path, file))
      }
  }
}
