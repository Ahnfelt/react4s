package dk.ahnfelt.react4s

abstract class Component[M] {
    private[react4s] var updateScheduled = false
    var emit : M => Unit = { _ => }
    var update : () => Unit = { () => }
    def componentWillRender() : Unit = {}
    def componentWillUnmount() : Unit = {}
    def render() : Element

    class ComponentState[T](var value : T) extends State[T] {
        def apply() : T = value
        def set(value : T) : Unit = {
            this.value = value
            if(!updateScheduled) update()
        }
    }

    object State {
        def apply[T](value : T) = new ComponentState(value)
    }
}

object Component {
    def apply[M](                                       f : { def apply() : Component[M] })                                                                                                                                                                                                 = ConstructorData(Constructor0(f))
    def apply[P1, M](                                   f : { def apply(p1 : P[P1]) : Component[M] }, p1 : P1)                                                                                                                                                                              = ConstructorData(Constructor1(f, p1))
    def apply[P1, P2, M](                               f : { def apply(p1 : P[P1], p2 : P[P2]) : Component[M] }, p1 : P1, p2 : P2)                                                                                                                                                         = ConstructorData(Constructor2(f, p1, p2))
    def apply[P1, P2, P3, M](                           f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3)                                                                                                                                    = ConstructorData(Constructor3(f, p1, p2, p3))
    def apply[P1, P2, P3, P4, M](                       f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4)                                                                                                               = ConstructorData(Constructor4(f, p1, p2, p3, p4))
    def apply[P1, P2, P3, P4, P5, M](                   f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5)                                                                                          = ConstructorData(Constructor5(f, p1, p2, p3, p4, p5))
    def apply[P1, P2, P3, P4, P5, P6, M](               f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6)                                                                     = ConstructorData(Constructor6(f, p1, p2, p3, p4, p5, p6))
    def apply[P1, P2, P3, P4, P5, P6, P7, M](           f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7)                                                = ConstructorData(Constructor7(f, p1, p2, p3, p4, p5, p6, p7))
    def apply[P1, P2, P3, P4, P5, P6, P7, P8, M](       f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8)                           = ConstructorData(Constructor8(f, p1, p2, p3, p4, p5, p6, p7, p8))
    def apply[P1, P2, P3, P4, P5, P6, P7, P8, P9, M](   f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8], p9 : P[P9]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8, p9 : P9)      = ConstructorData(Constructor9(f, p1, p2, p3, p4, p5, p6, p7, p8, p9))
}

abstract class State[T] {
    def apply() : T
    def set(value : T) : Unit
    def modify(update : T => T) : Unit = set(update(apply()))
}

abstract class P[T] extends (() => T)

sealed abstract class Tag {
    def when(condition : Boolean) : Tag = if(condition) this else Empty
}

sealed trait ElementOrComponent extends Tag {
    def withKey(key : String) : ElementOrComponent
    def withRef(onAddToDom : Any => Unit) : ElementOrComponent
}

final case class Element(tagName : String, children : Seq[Tag], key : Option[String] = None, ref : Option[Any => Unit] = None) extends ElementOrComponent {
    def apply(moreChildren : Tag*) = copy(children = children ++ moreChildren)
    def withKey(key : String) = copy(key = Some(key))
    def withRef(onAddToDom : Any => Unit) = copy(ref = Some(onAddToDom))
}

case class TagList(elements : List[Tag]) extends Tag {}

case object Empty extends Tag {}

case class Attributes(name : String, value : String, next : Option[Attributes] = None) extends Tag {}

case class Text(value : String) extends Tag {}

case class EventHandler(name : String, handler : SyntheticEvent => Unit) extends Tag {}

abstract class CssClass(val children : CssChild*) extends Tag with CssChild {
    override def toString : String = name
    def toCss : String = CssChild.cssToString(this)
    Css.nextClassId += 1
    val name = getClass.getSimpleName + "-" + Css.nextClassId
    var emitted = false
}

case class Style(name : String, value : String) extends Tag with CssChild {
    override def toString : String = name + ":" + value + ";"
    def apply(value : String) = copy(value = this.value + " " + value)
    def comma() = copy(value = this.value + ",")
    def url(value : String) = apply("url('" + value.replace("'", "\\'").replace("\n", "\\n") + "')")
    def percent(value : Double) = apply(value + "%")
    def em(value : Double) = apply(value + "em")
    def ex(value : Double) = apply(value + "ex")
    def ch(value : Double) = apply(value + "ch")
    def rem(value : Double) = apply(value + "rem")
    def vh(value : Double) = apply(value + "vh")
    def vw(value : Double) = apply(value + "vw")
    def vmin(value : Double) = apply(value + "vmin")
    def vmax(value : Double) = apply(value + "vmax")
    def px(value : Double) = apply(value + "px")
    def mm(value : Double) = apply(value + "mm")
    def q(value : Double) = apply(value + "q")
    def cm(value : Double) = apply(value + "cm")
    def in(value : Double) = apply(value + "in")
    def pt(value : Double) = apply(value + "pt")
    def pc(value : Double) = apply(value + "pc")
    def dpi(value : Double) = apply(value + "dpi")
    def dpcm(value : Double) = apply(value + "dpcm")
    def dppx(value : Double) = apply(value + "dppx")
    def s(value : Double) = apply(value + "s")
    def ms(value : Double) = apply(value + "ms")
    def hz(value : Double) = apply(value + "hz")
    def khz(value : Double) = apply(value + "khz")
    def deg(value : Double) = apply(value + "deg")
    def grad(value : Double) = apply(value + "grad")
    def rad(value : Double) = apply(value + "rad")
    def turn(value : Double) = apply(value + "turn")
    def rgb(r : Double, g : Double, b : Double) = apply("rgb(" + r.round + ", " + g.round + ", " + b.round + ")")
    def rgba(r : Double, g : Double, b : Double, a : Double) = apply("rgba(" + r.round + ", " + g.round + ", " + b.round + ", " + a + ")")
    def hsl(h : Double, s : Double, l : Double) = apply("hsl(" + h.round + ", " + s.round + "%, " + l.round + "%)")
    def hsla(h : Double, s : Double, l : Double, a : Double) = apply("hsla(" + h.round + ", " + s.round + "%, " + l.round + "%, " + a + ")")
    def block() = apply("block")
    def inlineBlock() = apply("inline-block")
    def inline() = apply("inline")
    def none() = apply("none")
    def inherit() = apply("inherit")
    def default() = apply("default")
    def bold() = apply("bold")
    def italic() = apply("italic")
    def pointer() = apply("pointer")
    def solid() = apply("solid")
    def dotted() = apply("dotted")
    def dashed() = apply("dashed")
}

sealed abstract class Constructor[M](val props : Seq[Any]) { val f : Any }
case class Constructor0[M](                                     f : { def apply() : Component[M] })                                                                                                                                                                                             extends Constructor[M](Seq())
case class Constructor1[P1, M](                                 f : { def apply(p1 : P[P1]) : Component[M] }, p1 : P1)                                                                                                                                                                          extends Constructor[M](Seq(p1))
case class Constructor2[P1, P2, M](                             f : { def apply(p1 : P[P1], p2 : P[P2]) : Component[M] }, p1 : P1, p2 : P2)                                                                                                                                                     extends Constructor[M](Seq(p1, p2))
case class Constructor3[P1, P2, P3, M](                         f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3)                                                                                                                                extends Constructor[M](Seq(p1, p2, p3))
case class Constructor4[P1, P2, P3, P4, M](                     f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4)                                                                                                           extends Constructor[M](Seq(p1, p2, p3, p4))
case class Constructor5[P1, P2, P3, P4, P5, M](                 f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5)                                                                                      extends Constructor[M](Seq(p1, p2, p3, p4, p5))
case class Constructor6[P1, P2, P3, P4, P5, P6, M](             f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6)                                                                 extends Constructor[M](Seq(p1, p2, p3, p4, p5, p6))
case class Constructor7[P1, P2, P3, P4, P5, P6, P7, M](         f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7)                                            extends Constructor[M](Seq(p1, p2, p3, p4, p5, p6, p7))
case class Constructor8[P1, P2, P3, P4, P5, P6, P7, P8, M](     f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8)                       extends Constructor[M](Seq(p1, p2, p3, p4, p5, p6, p7, p8))
case class Constructor9[P1, P2, P3, P4, P5, P6, P7, P8, P9, M]( f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8], p9 : P[P9]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8, p9 : P9)  extends Constructor[M](Seq(p1, p2, p3, p4, p5, p6, p7, p8, p9))

case class ConstructorData[M](
    constructor : Constructor[M],
    handler : M => Unit = { _ : M => },
    key : Option[String] = None,
    ref : Option[Any => Unit] = None
) extends ElementOrComponent {
    def withHandler(handler : M => Unit) = copy(handler = handler)
    def withKey(key : String) = copy(key = Some(key))
    def withRef(onAddToDom : Any => Unit) = copy(ref = Some(onAddToDom))
}

object E {
    def apply(tagName : String, children : Tag*) = Element(tagName, children)
    def div(children : Tag*) = Element("div", children)
    def span(children : Tag*) = Element("span", children)
    def button(children : Tag*) = Element("button", children)
    def input(children : Tag*) = Element("input", children)
    def textarea(children : Tag*) = Element("textarea", children)
    def select(children : Tag*) = Element("select", children)
    def option(children : Tag*) = Element("option", children)
    def form(children : Tag*) = Element("form", children)
    def label(children : Tag*) = Element("label", children)
    def table(children : Tag*) = Element("table", children)
    def thead(children : Tag*) = Element("thead", children)
    def tbody(children : Tag*) = Element("tbody", children)
    def tr(children : Tag*) = Element("tr", children)
    def td(children : Tag*) = Element("td", children)
    def th(children : Tag*) = Element("th", children)
    def p(children : Tag*) = Element("p", children)
    def hr(children : Tag*) = Element("hr", children)
    def br(children : Tag*) = Element("br", children)
    def a(children : Tag*) = Element("a", children)
    def img(children : Tag*) = Element("img", children)
    def h1(children : Tag*) = Element("h1", children)
    def h2(children : Tag*) = Element("h2", children)
    def h3(children : Tag*) = Element("h3", children)
    def h4(children : Tag*) = Element("h4", children)
    def h5(children : Tag*) = Element("h5", children)
    def h6(children : Tag*) = Element("h6", children)
    def blockquote(children : Tag*) = Element("blockquote", children)
    def ol(children : Tag*) = Element("ol", children)
    def ul(children : Tag*) = Element("ul", children)
    def li(children : Tag*) = Element("li", children)
    def iframe(children : Tag*) = Element("iframe", children)
}

object A extends CommonEvents {
    def apply(attributeName : String, value : String) = Attributes(attributeName, value)
    /** A helper method for setting up A.onChange that just looks at e.target.value.}}} */
    def onValue(onChange : String => Unit) = {
        A.onChange(e => onChange(e.target.value.asInstanceOf[String]))
    }
    /** A helper method for setting up A.value and A.onChange for State[String]. Example: {{{E.input(A.bindValue(name))}}} */
    def bindValue(state : State[String]) : TagList = TagList(List(
        A.value(state()),
        A.onValue(v => state.set(v))
    ))
    def action(value : String) = Attributes("action", value)
    def src(value : String) = Attributes("src", value)
    def href(value : String) = Attributes("href", value)
    def target(value : String) = Attributes("target", value)
    def alt(value : String) = Attributes("alt", value)
    def title(value : String) = Attributes("title", value)
    def id(value : String) = Attributes("id", value)
    def name(value : String) = Attributes("name", value)
    def placeholder(value : String) = Attributes("placeholder", value)
    def value(value : String) = Attributes("value", value)
    def htmlType(value : String) = Attributes("type", value)
    def htmlFor(value : String) = Attributes("htmlFor", value)
    def colSpan(value : Int) = Attributes("colSpan", value.toString)
    def rowSpan(value : Int) = Attributes("rowSpan", value.toString)
    def autoFocus() = Attributes("autoFocus", "true")
    def checked() = Attributes("checked", "true")
    def disabled() = Attributes("disabled", "true")
    def className(value : String*) = Attributes("className", value.mkString(" "))
    /** Set up an event handler. Note that the event name must contain the 'on', eg. 'onClick' instead of 'click'. */
    def on(eventName : String, handler : SyntheticEvent => Unit) = EventHandler(eventName, handler)
}

object S {
    def apply(name : String, value : String = "") = Style(name, value)
    val position = Style("position", "")
    val display = Style("display", "")
    val visibility = Style("visibility", "")
    val float = Style("float", "")
    val color = Style("color", "")
    val cursor = Style("cursor", "")
    val backgroundColor = Style("background-color", "")
    val width = Style("width", "")
    val height = Style("height", "")
    val top = Style("top", "")
    val bottom = Style("bottom", "")
    val left = Style("left", "")
    val right = Style("right", "")
    val margin = Style("margin", "")
    val padding = Style("padding", "")
    val borderRadius = Style("border-radius", "")
    val borderTopLeftRadius = Style("border-top-left-radius", "")
    val borderTopRightRadius = Style("border-top-right-radius", "")
    val borderBottomLeftRadius = Style("border-bottom-left-radius", "")
    val borderBottomRightRadius = Style("border-bottom-right-radius", "")
    val border = Style("border", "")
    val borderWidth = Style("border-width", "")
    val borderColor = Style("border-color", "")
    val borderStyle = Style("border-style", "")
    val outline = Style("outline", "")
    val boxShadow = Style("box-shadow", "")
    val textShadow = Style("text-shadow", "")
    val font = Style("font", "")
    val fontWeight = Style("font-weight", "")
    val fontFamily = Style("font-family", "")
    val fontSize = Style("font-size", "")
    val fontStyle = Style("font-style", "")
    val textDecoration = Style("text-decoration", "")
}
