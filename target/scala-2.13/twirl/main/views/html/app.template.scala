
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import play.data._
import play.core.j.PlayFormsMagicForJava._

object app extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<!DOCTYPE html>
<html lang="en">
<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="mobile-web-app-capable" content="yes">

        <title>GrazeScape v0.1</title>
        <link rel="stylesheet" type="text/css"" href="/assets/vendor/ol/ol.css">
        <link rel="stylesheet" media="screen" href="/assets/vendor/extjs/ext-theme-crisp-all.css"">
        <link rel="shortcut icon" type="image/png" href=""""),_display_(/*13.59*/routes/*13.65*/.Assets.versioned("images/favicon.png")),format.raw/*13.104*/("""">
        <script src="/assets/vendor/ol/ol.js"></script>
        <script src="/assets/vendor/extjs/ext-all.js"></script>
		<link rel="stylesheet" href="/assets/vendor/extjs/charts-all.css">
        <link rel="stylesheet" media="screen" href=""""),_display_(/*17.54*/routes/*17.60*/.Assets.versioned("css/app_main.css")),format.raw/*17.97*/("""">
		<link rel="stylesheet" media="screen" href=""""),_display_(/*18.48*/routes/*18.54*/.Assets.versioned("css/d3-nav.css")),format.raw/*18.89*/("""">
    </head>
    <body>
    </div>
        <script src="assets/js/ExtUtils.js" type="text/javascript"></script>
        <script src="assets/js/app_main.js" type="text/javascript"></script>
		<script src="assets/vendor/d3/d3.min.js"></script>
		<script src="assets/vendor/d3/sankey.js"></script>
		<script type="text/javascript" src="/assets/vendor/extjs/charts.js"></script>
    </body>
</html>"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2020-01-31T16:02:09.497
                  SOURCE: /Users/jdischler/Projects/GrazeScape_svr/app/views/app.scala.html
                  HASH: 562a8a542f2e17cf30c3e0ebcf746281317727ed
                  MATRIX: 1028->0|1625->570|1640->576|1701->615|1973->860|1988->866|2046->903|2123->953|2138->959|2194->994
                  LINES: 33->1|45->13|45->13|45->13|49->17|49->17|49->17|50->18|50->18|50->18
                  -- GENERATED --
              */
          