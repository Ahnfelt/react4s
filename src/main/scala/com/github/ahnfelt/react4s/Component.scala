package com.github.ahnfelt.react4s

import scala.collection.mutable.ListBuffer

/** Represents a React component that can emit messages of type M. Use Component[NoEmit] for components that never emit messages. */
trait Component[M] {
    /** Internal flag that ensures we only update the state once between renderings. */
    private[react4s] var updateScheduled = false
    /** Internal list of attached listeners. */
    private[react4s] val attachedAttachables = ListBuffer[Attachable]()
    /** Emit a message of type M, which can be handled by a parent component by using .withHandler(...). */
    var emit : M => Unit = { _ => }
    /** Signal that the component state has changed. This always results in a re-rendering of this component. */
    var update : () => Unit = { () => }
    /** Attach an Attachable that can listen for events in this components lifecycle. */
    def attach[T <: Attachable](attachable : T) : T = { attachedAttachables += attachable; attachable }
    /** Called just before render(). You can modify component state here. */
    def componentWillRender() : Unit = {}
    /** Called just before the component is unmounted. This callback is typically used to clean up resources. */
    def componentWillUnmount() : Unit = {}
    /** Called when the component needs to be rendered. Rerendering only happens when this components props are change or it's state is updated. */
    def render() : ElementOrComponent

    /** Internal implementation of a component state variable that automatically calls update() when changed. */
    private class ComponentState[T](var value : T) extends State[T] {
        def apply() : T = value
        def set(value : T) : Unit = {
            this.value = value
            if(!updateScheduled) update()
        }
    }

    /** Used to represent local component state. The component update() method is automatically called when .set or .modify is called. */
    object State {
        def apply[T](value : T) : State[T] = new ComponentState(value)
    }
}

object Component {
    /** Captures a Component constructor with zero props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[M](                                       f : { def apply() : Component[M] })                                                                                                                                                                                                 = ConstructorData(Constructor0(f))
    /** Captures a Component constructor with one prop so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, M](                                   f : { def apply(p1 : P[P1]) : Component[M] }, p1 : P1)                                                                                                                                                                              = ConstructorData(Constructor1(f, p1))
    /** Captures a Component constructor with two props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, M](                               f : { def apply(p1 : P[P1], p2 : P[P2]) : Component[M] }, p1 : P1, p2 : P2)                                                                                                                                                         = ConstructorData(Constructor2(f, p1, p2))
    /** Captures a Component constructor with three props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, M](                           f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3)                                                                                                                                    = ConstructorData(Constructor3(f, p1, p2, p3))
    /** Captures a Component constructor with four props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, M](                       f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4)                                                                                                               = ConstructorData(Constructor4(f, p1, p2, p3, p4))
    /** Captures a Component constructor with five props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, P5, M](                   f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5)                                                                                          = ConstructorData(Constructor5(f, p1, p2, p3, p4, p5))
    /** Captures a Component constructor with six props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, P5, P6, M](               f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6)                                                                     = ConstructorData(Constructor6(f, p1, p2, p3, p4, p5, p6))
    /** Captures a Component constructor with seven props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, P5, P6, P7, M](           f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7)                                                = ConstructorData(Constructor7(f, p1, p2, p3, p4, p5, p6, p7))
    /** Captures a Component constructor with eight props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, P5, P6, P7, P8, M](       f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8)                           = ConstructorData(Constructor8(f, p1, p2, p3, p4, p5, p6, p7, p8))
    /** Captures a Component constructor with nine props so that it can be used as the child of an Element. The constructor will be called the first time an instance is required at this position, and the instance will be reused for subsequent renderings. */
    def apply[P1, P2, P3, P4, P5, P6, P7, P8, P9, M](   f : { def apply(p1 : P[P1], p2 : P[P2], p3 : P[P3], p4 : P[P4], p5 : P[P5], p6 : P[P6], p7 : P[P7], p8 : P[P8], p9 : P[P9]) : Component[M] }, p1 : P1, p2 : P2, p3 : P3, p4 : P4, p5 : P5, p6 : P6, p7 : P7, p8 : P8, p9 : P9)      = ConstructorData(Constructor9(f, p1, p2, p3, p4, p5, p6, p7, p8, p9))
}

/** Represents local component state. */
abstract class State[T] extends (() => T) {
    /** Get the current value. */
    def apply() : T
    /** Set the value. In components, State(...) objects automatically call component.update() when this method is called. */
    def set(value : T) : Unit
    /** Modify the value. In components, State(...) objects automatically call component.update() when this method is called. */
    def modify(update : T => T) : Unit = set(update(apply()))
}

/** Represents a prop, ie. an argument to a Component. The value it holds can be read with .apply() and may change over time. */
abstract class P[T] extends (() => T)

/** A class with no instances, used as the type parameter for Component when the component doesn't emit messages. */
final abstract class NoEmit

/** An interface for anything that can be the child of an Element. */
sealed abstract class Tag {
    /** Conditionally replaces the tag with Empty, which does nothing. */
    def when(condition : Boolean) : Tag = if(condition) this else Tags.empty
}

/** An interface for things that are either elements or components. */
sealed trait ElementOrComponent extends Tag {
    /** Change the key for this element or component. React uses this to reorder components, thus saving time and keeping the internal component state where it belongs. */
    def withKey(key : String) : ElementOrComponent
    /** Set up a callback that is called when this element or component is first added to to the DOM. The callback receives the actual DOM element. */
    def withRef(onAddToDom : Any => Unit) : ElementOrComponent
}

sealed trait Element extends ElementOrComponent {
    type Self <: Element
    def apply(moreChildren : Tag*): Self
    def withKey(key : String): Self
    def withRef(onAddToDom : Any => Unit): Self
}

/** Wraps a React component written in JavaScript. Example: ```DynamicElement(js.Dynamic.global.MyJsComponent, js.Dictionary("label" -> "Go!"))``` */
final case class DynamicElement(componentClass : Any, props : Any, children : Seq[Tag], key : Option[String] = None, ref : Option[Any => Unit] = None) extends Element {
    override type Self = DynamicElement
    /** Appends the extra children to this element. */
    override def apply( moreChildren: Tag* ) = copy(children = children ++ moreChildren)
    override def withKey(key : String) = copy(key = Some(key))
    override def withRef(onAddToDom : Any => Unit) = copy(ref = Some(onAddToDom))
}

/** Represents an element (eg. div, span, p, h1, b, etc.). */
final case class HtmlElement(tagName : String, children : Seq[Tag], key : Option[String] = None, ref : Option[Any => Unit] = None) extends Element {
    override type Self = HtmlElement
    /** Appends the extra children to this element. */
    def apply(moreChildren : Tag*) = copy(children = children ++ moreChildren)
    def withKey(key : String) = copy(key = Some(key))
    def withRef(onAddToDom : Any => Unit) = copy(ref = Some(onAddToDom))
}

/** A list of tags. The list will be flattened into the parent before the Virtual DOM is reconciled. */
case class Tags(tags : Seq[Tag]) extends Tag
object Tags {
    /** An optional tag. */
    def apply(option : Option[Tag]) = option.getOrElse(empty)
    /** An empty list of tags. Since the list is empty, it will be completely removed before the Virtual DOM is reconciled. */
    val empty : Tags = Tags(Seq())
}

/** A tag that will be removed before the Virtual DOM is reconciled, and thus completely ignored. */
case class Attribute(name : String, value : String) extends Tag

/** A piece of plain text. */
case class Text(value : String) extends Tag

/** An event handler, eg. onClick(...). */
case class EventHandler(name : String, handler : SyntheticEvent => Unit) extends Tag

/** A CSS class that will be inserted into the DOM the first time it's used to render a component. Be careful not to create these dynamically, or you'll end up filling up the DOM with styles. */
abstract class CssClass(val children : CssChild*) extends Tag with CssChild {
    override def toString : String = name
    def toCss : String = CssChild.cssToString(this)
    Css.nextClassId += 1
    val name = getClass.getSimpleName + "-" + Css.nextClassId
    var emitted = false
}

/** A style, eg. color: rgb(255, 0, 0). Can be used inline to style an Element or in a CssClass. */
case class Style(name : String, value : String) extends Tag with CssChild {
    /** Converts this style to the CSS syntax followed by a semicolon, eg. Style("background-color", "red").toString == "background-color: red;" */
    override def toString : String = Style.toStandardName(name) + ":" + value + ";"
    /** Appends a space followed by the argument to the right-hand side of the style. */
    def apply(value : String) = copy(value = this.value + " " + value)
    /** Appends a comma to the right-hand side of the style. */
    def comma() = copy(value = this.value + ",")
    /** Appends an escaped URL to the right-hand side of the style. */
    def url(value : String) = apply("url('" + value.replace("'", "\\'").replace("\n", "\\n") + "')")
    /** Appends the value and the % unit to the right-hand side of the style. */
    def percent(value : Double) = apply(value + "%")
    /** Appends the unit-less number to the right-hand side of the style. */
    def number(value : Double) = apply(value.toString)
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
    def absolute() = apply("absolute")
    def relative() = apply("relative")
    def static() = apply("static")
    def fixed() = apply("fixed")
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

object Style {
    private val camelPattern = "([A-Z])".r
    private val snakePattern = "[-]([a-z])".r
    def toStandardName(name : String) : String = camelPattern.replaceAllIn(name, m => "-" + m.group(1).toLowerCase)
    def toReactName(name : String) : String = snakePattern.replaceAllIn(name, m => m.group(1).toUpperCase)
}

/** Represents a component constructor. This class is used to delay creating the instance until necessary. */
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

/** Represents a component constructor plus an associated handler, key and ref. */
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

/** A convenience object for constructing Elements. */
object E {
    /** Synonym for Element(tagName, children) */
    def apply(tagName : String, children : Tag*) = HtmlElement(tagName, children)
    def div(children : Tag*) = HtmlElement("div", children)
    def span(children : Tag*) = HtmlElement("span", children)
    def button(children : Tag*) = HtmlElement("button", children)
    def input(children : Tag*) = HtmlElement("input", children)
    def textarea(children : Tag*) = HtmlElement("textarea", children)
    def select(children : Tag*) = HtmlElement("select", children)
    def option(children : Tag*) = HtmlElement("option", children)
    def form(children : Tag*) = HtmlElement("form", children)
    def label(children : Tag*) = HtmlElement("label", children)
    def table(children : Tag*) = HtmlElement("table", children)
    def thead(children : Tag*) = HtmlElement("thead", children)
    def tbody(children : Tag*) = HtmlElement("tbody", children)
    def tr(children : Tag*) = HtmlElement("tr", children)
    def td(children : Tag*) = HtmlElement("td", children)
    def th(children : Tag*) = HtmlElement("th", children)
    def p(children : Tag*) = HtmlElement("p", children)
    def hr(children : Tag*) = HtmlElement("hr", children)
    def br(children : Tag*) = HtmlElement("br", children)
    def a(children : Tag*) = HtmlElement("a", children)
    def img(children : Tag*) = HtmlElement("img", children)
    def h1(children : Tag*) = HtmlElement("h1", children)
    def h2(children : Tag*) = HtmlElement("h2", children)
    def h3(children : Tag*) = HtmlElement("h3", children)
    def h4(children : Tag*) = HtmlElement("h4", children)
    def h5(children : Tag*) = HtmlElement("h5", children)
    def h6(children : Tag*) = HtmlElement("h6", children)
    def blockquote(children : Tag*) = HtmlElement("blockquote", children)
    def ol(children : Tag*) = HtmlElement("ol", children)
    def ul(children : Tag*) = HtmlElement("ul", children)
    def li(children : Tag*) = HtmlElement("li", children)
    def iframe(children : Tag*) = HtmlElement("iframe", children)
}

/** A convenience object for constructing Attributes. */
object A extends CommonEvents {
    /** A synonym for Attributes(attributeName, value). */
    def apply(attributeName : String, value : String) = Attribute(attributeName, value)
    /** A helper method for setting up A.onChange that just looks at e.target.value.}}} */
    def onChangeText(onChange : String => Unit) = {
        A.onChange(e => onChange(e.target.value.asInstanceOf[String]))
    }
    /** A helper method for setting up A.value and A.onChange for State[String]. Example: {{{E.input(A.bindValue(name))}}} */
    def bindValue(state : State[String]) : Tags = Tags(List(
        A.value(state()),
        A.onChangeText(v => state.set(v))
    ))
    def action(value : String) = Attribute("action", value)
    def src(value : String) = Attribute("src", value)
    def href(value : String) = Attribute("href", value)
    def target(value : String) = Attribute("target", value)
    def alt(value : String) = Attribute("alt", value)
    def title(value : String) = Attribute("title", value)
    def id(value : String) = Attribute("id", value)
    def name(value : String) = Attribute("name", value)
    def placeholder(value : String) = Attribute("placeholder", value)
    def value(value : String) = Attribute("value", value)
    def htmlType(value : String) = Attribute("type", value)
    def htmlFor(value : String) = Attribute("htmlFor", value)
    def colSpan(value : Int) = Attribute("colSpan", value.toString)
    def rowSpan(value : Int) = Attribute("rowSpan", value.toString)
    def autoFocus() = Attribute("autoFocus", "true")
    def checked() = Attribute("checked", "true")
    def disabled() = Attribute("disabled", "true")
    def className(value : String*) = Attribute("className", value.mkString(" "))
    /** Set up an event handler. Note that the event name must contain the 'on', eg. 'onClick' instead of 'click'. */
    def on(eventName : String, handler : SyntheticEvent => Unit) = EventHandler(eventName, handler)
}

/** A convenience object for constructing Styles. */
object S {
    /** A synonym for Style(name, value). */
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
