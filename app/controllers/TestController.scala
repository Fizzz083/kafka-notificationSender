package controllers

import com.google.inject.Inject
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class TestController @Inject()(controllerComponents: ControllerComponents
                              )
  extends AbstractController(controllerComponents) {
    def index() = Action { implicit request: Request[AnyContent] =>

      val a = 5
      val b = 9
      val c = a+b
      println(s"sum is = $c")
      println("Hello from HomeIndex")
      Ok(views.html.index())
    }
}
