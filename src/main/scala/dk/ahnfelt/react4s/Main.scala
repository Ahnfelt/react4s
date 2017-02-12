package dk.ahnfelt.react4s



//
// The code in this file is just example code, and not really part of the library.
// The example code will be removed from this repository eventually.
//



import scala.scalajs.js


object Main extends js.JSApp {
    def main() : Unit = {
        val component = H(CounterListComponent, H.swallow)
        ReactBridge.renderToDomById(component, "main")
    }
}



// List of removable counters example


case class Counter(value : Int)


sealed abstract class CounterMessage
case object Increment extends CounterMessage
case object Decrement extends CounterMessage
case object Remove extends CounterMessage


case class CounterListComponent() extends Component[Unit] {

    val counters = State(List[Counter]())

    def onAddCounter() = {
        counters.set(counters() :+ Counter(0))
    }

    def onCounterMessage(index : Int)(message : CounterMessage) = message match {
        case Increment =>
            val counter = Counter(counters()(index).value + 1)
            counters.set(counters().updated(index, counter))
        case Decrement =>
            val counter = Counter(counters()(index).value - 1)
            counters.set(counters().updated(index, counter))
        case Remove =>
            counters.set(counters().take(index) ++ counters().drop(index + 1))
    }

    override def render() = {
        E.div(
            E.button(H.text("Add"), A.onClick(_ => onAddCounter()), FancyButtonCss),
            H.list(
                counters().zipWithIndex.map { case (counter, index) =>
                    H(CounterComponent, counter, onCounterMessage(index))
                }
            )
        )
    }
}


case class CounterComponent(counter : P[Counter]) extends Component[CounterMessage] {

    def spacer(width : Int) = E.span(S.display.inlineBlock(), S.width.px(width))

    override def render() = {
        E.div(
            E.button(H.text("-"), A.onClick(_ => emit(Decrement)), FancyButtonCss),
            spacer(20),
            H.text(counter().value.toString),
            spacer(20),
            E.button(H.text("+"), A.onClick(_ => emit(Increment)), FancyButtonCss),
            spacer(50),
            E.button(H.text("X"), A.onClick(_ => emit(Remove)), FancyButtonCss)
        )
    }

}


object FancyButtonCss extends CssClass(
    S.cursor.pointer(),
    S.border.px(2).solid().rgb(0, 0, 0),
    S.color.rgb(0, 0, 0),
    S.backgroundColor.rgb(255, 255, 255),
    Css.hover(
        S.color.rgb(255, 255, 255),
        S.backgroundColor.rgb(0, 0, 0)
    )
)
