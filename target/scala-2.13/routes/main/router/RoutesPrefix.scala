// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jdischler/Projects/GrazeScape_svr/conf/routes
// @DATE:Fri Jan 31 16:02:09 CST 2020


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
