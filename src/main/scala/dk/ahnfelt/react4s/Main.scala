package dk.ahnfelt.react4s

import scala.scalajs.js
import dk.ahnfelt.react4s.FancyButton._

object Main extends js.JSApp {
    def main() : Unit = {
        val component = H(EchoMyNameWithState, H.swallow)
        ReactBridge.renderToDomById(component, "main")
    }
}

case class EchoMyNameWithState() extends Component[Unit] {
    val name = State("John Doe")
    override def render() : Element = {
        E.div(
            E.input(H.bind(name)),
            E.div(H.text(name()), Colorful),
            E.hr(),
            H(Echo, name(), name.set)
        )
    }
}

case class EchoMyName() extends Component[Unit] {
    var name : String = ""
    override def render() : Element = {
        E.div(
            E.input(H.bindVar(name, {name = _}, update)),
            E.div(H.text(name)),
            E.hr(),
            E.div(
                H(Echo, name, { newName : String => name = newName; update() })
            )
        )
    }
}

case class Echo(name : P[String]) extends Component[String] {
    override def render() : Element = {
        E.div(
            E.input(H.bindProp(name(), emit)),
            E.div(H.text(name()))
        )
    }
}

object Colorful extends CssClass(
    S.color.rgb(200, 0, 0),
    S.cursor.pointer(),
    Css.mediaLarge(
        Css.hover(
            S.textDecoration("underline")
        )
    )
)



case class Baz(list : List[String])

case class Outer() extends Component[Unit] {
    var baz = Baz(List("First", "Second"))

    def onTimeout() = {
        baz = Baz(List("First", "Second"))
        update()
    }

    js.Dynamic.global.setTimeout(() => onTimeout(), 1000)

    override def render() = {
        println("Render outer")
        E.div(
            S.display.block(),
            S.color.rgb(50, 50, 50),
            H(Inner, baz, println)
        )
    }
}

case class Inner(labels : P[Baz]) extends Component[String] {
    override def render() = {
        println("Render inner")
        E.div(
            H(ButtonList, labels().list, emit)
        )
    }
}



case class FancyButton(label : P[String]) extends Component[FancyButton.Message] {
    override def render() = {
        E.div(
            A.onClick(_ => emit(Click)),
            H.text(label())
        )
    }
}

object FancyButton {
    sealed abstract class Message
    case object Click extends Message
}


case class ButtonPanel() extends Component[Unit] {

    def onClick(message : FancyButton.Message) = message match {
        case FancyButton.Click =>
            println("Button clicked!")
    }

    override def render() = {
        E.div(
            H(FancyButton, "Click me!", onClick)
        )
    }
}

case class ButtonList(labels : P[List[String]]) extends Component[String] {

    def onClick(label : String)(message : FancyButton.Message) = emit(label)

    override def render() = {
        E.div(
            H.list(labels().map { label =>
                H(FancyButton, label, onClick(label)).withKey(label)
            })
        )
    }
}
