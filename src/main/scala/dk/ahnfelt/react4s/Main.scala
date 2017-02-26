package dk.ahnfelt.react4s



//
// The code in this file is just example code, and not really part of the library.
// The example code will be removed from this repository eventually.
//



import scala.scalajs.js


object Main extends js.JSApp {
    def main() : Unit = {
        val component = Component(CounterListComponent)
        ReactBridge.renderToDomById(component, "main")
    }
}



// List of removable counters example


case class Counter(value : Int)


sealed abstract class CounterMessage
case object Increment extends CounterMessage
case object Decrement extends CounterMessage
case object Remove extends CounterMessage


case class CounterListComponent() extends Component[NoEmit] {

    val counters = State(List[Counter]())

    def onAddCounter() = {
        counters.modify(_ :+ Counter(0))
    }

    def onCounterMessage(index : Int)(message : CounterMessage) = message match {
        case Increment =>
            counters.modify(l => l.updated(index, l(index).copy(value = l(index).value + 1)))
        case Decrement =>
            counters.modify(l => l.updated(index, l(index).copy(value = l(index).value - 1)))
        case Remove =>
            counters.modify(l => l.take(index) ++ l.drop(index + 1))
    }

    override def render() = {
        E.div(
            E.button(Text("Add"), A.onClick(_ => onAddCounter()), FancyButtonCss),
            Tags(
                counters().zipWithIndex.map { case (counter, index) =>
                    Component(CounterComponent, counter).withHandler(onCounterMessage(index))
                }
            )
        )
    }
}


case class CounterComponent(counter : P[Counter]) extends Component[CounterMessage] {

    def spacer(width : Int) = E.span(S.display.inlineBlock(), S.width.px(width))

    override def render() = {
        E.div(
            E.button(Text("-"), A.onClick(_ => emit(Decrement)), FancyButtonCss),
            spacer(20),
            Text(counter().value.toString),
            spacer(20),
            E.button(Text("+"), A.onClick(_ => emit(Increment)), FancyButtonCss),
            spacer(50),
            E.button(Text("X"), A.onClick(_ => emit(Remove)), FancyButtonCss)
        )
    }

}


object FancyButtonCss extends CssClass(
    S.cursor.pointer(),
    S.border.px(2).solid().rgb(0, 0, 0),
    S.margin.px(2),
    S.color.rgb(0, 0, 0),
    S.backgroundColor.rgb(255, 255, 255),
    Css.hover(
        S.color.rgb(255, 255, 255),
        S.backgroundColor.rgb(0, 0, 0)
    )
)
