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
    /** Called just before render(). You can modify component state here. Note that componentWillRender() won't fire for ReactBridge.renderToString and ReactBridge.renderToStaticMarkup. */
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

abstract class JsTag

/** An interface for anything that can be the child of an Element. */
sealed abstract class Tag extends JsTag {
    /** Conditionally replaces the tag with Empty, which does nothing. */
    def when(condition : Boolean) : Tag = if(condition) this else Tags.empty
}

/** An interface for things that are either elements, components or text. */
sealed trait Node extends Tag

/** An interface for things that are either elements or components. */
sealed trait ElementOrComponent extends Node {
    /** Change the key for this element or component. React uses this to reorder components, thus saving time and keeping the internal component state where it belongs. */
    def withKey(key : String) : ElementOrComponent
    /** Set up a callback that is called when this element or component is first added to to the DOM. The callback receives the actual DOM element. */
    def withRef(onAddToDom : Any => Unit) : ElementOrComponent
}

/**
  For exposing existing components written in JavaScript. Example:
<pre>
object FancyButton extends JsComponent(js.Dynamic.global.FancyButton)
</pre>
<p>Usage:</p>
<pre>
FancyButton(
    J("onClick", {_ => println("Clicked!")}),
    J("labelStyle", S.color.rgb(255, 0, 0), S("text-transform", "uppercase")),
    J("tip", "Click me"),
    Text("Submit")
)
</pre>
*/
abstract class JsComponent(componentClass : Any) {
    def apply(children : JsTag*) = JsComponentConstructor(componentClass, children, None, None)
}

/** Internal capture of the JsComponent constructor and apply arguments. */
case class JsComponentConstructor(componentClass : Any, children : Seq[JsTag], key : Option[String], ref : Option[Any => Unit]) extends ElementOrComponent {
    override def withKey(key : String) = copy(key = Some(key))
    override def withRef(onAddToDom : Any => Unit) = copy(ref = Some(onAddToDom))
}

/** Represents an element (eg. div, span, p, h1, b, etc.). */
final case class Element(tagName : String, children : Seq[Tag], key : Option[String] = None, ref : Option[Any => Unit] = None) extends ElementOrComponent {
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

/** An attribute, such as "name" (or prop, for dynamic components). */
case class Attribute(name : String, value : String) extends Tag

/** A piece of plain text. */
case class Text(value : String) extends Node

/** An event handler, eg. onClick(...). */
case class EventHandler(name : String, handler : SyntheticEvent => Unit) extends Tag

/** A CSS class that will be inserted into the DOM the first time it's used to render a component. Be careful not to create these dynamically, or you'll end up filling up the DOM with styles. */
abstract class CssClass(val children : CssChild*) extends Tag with CssChild {
    override def toString : String = name
    def toCss : String = CssChild.cssToString(this)
    val name = getClass.getSimpleName + "-" + getClass.getName.hashCode.toHexString
    var emitted = false
}

/** CSS keyframes that will be inserted into the DOM the first time it's used to render a component. When keyframes are used in a CssClass, the appropriate animation-name rule is automatically inserted. Be careful not to create these dynamically, or you'll end up filling up the DOM with keyframes. */
abstract class CssKeyframes(val keyframes : (String, Seq[Style])*) extends CssChild {
    override def toString : String = name
    def toCss : String =
        "@keyframes " + name + " {\n" +
        (for((at, styles) <- keyframes) yield {
            "  " + at + " {\n" + styles.map("    " + _).mkString("\n") + "\n  }\n"
        }).mkString +
        "}\n"
    val name = getClass.getSimpleName + "-" + getClass.getName.hashCode.toHexString
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
    def table() = apply("table")
    def tableCell() = apply("table-cell")
    def none() = apply("none")
    def inherit() = apply("inherit")
    def default() = apply("default")
    def bold() = apply("bold")
    def italic() = apply("italic")
    def pointer() = apply("pointer")
    def solid() = apply("solid")
    def dotted() = apply("dotted")
    def dashed() = apply("dashed")
    def left() = apply("left")
    def center() = apply("center")
    def right() = apply("right")
    def top() = apply("top")
    def middle() = apply("middle")
    def bottom() = apply("bottom")
    def borderBox() = apply("border-box")
    def contentBox() = apply("content-box")
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
    def apply(tagName : String, children : Tag*) = Element(tagName, children)

    def a(children : Tag*) = Element("a", children)
    def abbr(children : Tag*) = Element("abbr", children)
    def acronym(children : Tag*) = Element("acronym", children)
    def address(children : Tag*) = Element("address", children)
    def applet(children : Tag*) = Element("applet", children)
    def area(children : Tag*) = Element("area", children)
    def article(children : Tag*) = Element("article", children)
    def aside(children : Tag*) = Element("aside", children)
    def audio(children : Tag*) = Element("audio", children)
    def b(children : Tag*) = Element("b", children)
    def base(children : Tag*) = Element("base", children)
    def basefont(children : Tag*) = Element("basefont", children)
    def bdi(children : Tag*) = Element("bdi", children)
    def bdo(children : Tag*) = Element("bdo", children)
    def big(children : Tag*) = Element("big", children)
    def blockquote(children : Tag*) = Element("blockquote", children)
    def body(children : Tag*) = Element("body", children)
    def br(children : Tag*) = Element("br", children)
    def button(children : Tag*) = Element("button", children)
    def canvas(children : Tag*) = Element("canvas", children)
    def caption(children : Tag*) = Element("caption", children)
    def center(children : Tag*) = Element("center", children)
    def cite(children : Tag*) = Element("cite", children)
    def code(children : Tag*) = Element("code", children)
    def col(children : Tag*) = Element("col", children)
    def colgroup(children : Tag*) = Element("colgroup", children)
    def data(children : Tag*) = Element("data", children)
    def datalist(children : Tag*) = Element("datalist", children)
    def dd(children : Tag*) = Element("dd", children)
    def del(children : Tag*) = Element("del", children)
    def details(children : Tag*) = Element("details", children)
    def dfn(children : Tag*) = Element("dfn", children)
    def dialog(children : Tag*) = Element("dialog", children)
    def dir(children : Tag*) = Element("dir", children)
    def div(children : Tag*) = Element("div", children)
    def dl(children : Tag*) = Element("dl", children)
    def dt(children : Tag*) = Element("dt", children)
    def em(children : Tag*) = Element("em", children)
    def embed(children : Tag*) = Element("embed", children)
    def fieldset(children : Tag*) = Element("fieldset", children)
    def figcaption(children : Tag*) = Element("figcaption", children)
    def figure(children : Tag*) = Element("figure", children)
    def font(children : Tag*) = Element("font", children)
    def footer(children : Tag*) = Element("footer", children)
    def form(children : Tag*) = Element("form", children)
    def frame(children : Tag*) = Element("frame", children)
    def frameset(children : Tag*) = Element("frameset", children)
    def h1(children : Tag*) = Element("h1", children)
    def h2(children : Tag*) = Element("h2", children)
    def h3(children : Tag*) = Element("h3", children)
    def h4(children : Tag*) = Element("h4", children)
    def h5(children : Tag*) = Element("h5", children)
    def h6(children : Tag*) = Element("h6", children)
    def head(children : Tag*) = Element("head", children)
    def header(children : Tag*) = Element("header", children)
    def hr(children : Tag*) = Element("hr", children)
    def html(children : Tag*) = Element("html", children)
    def i(children : Tag*) = Element("i", children)
    def iframe(children : Tag*) = Element("iframe", children)
    def img(children : Tag*) = Element("img", children)
    def input(children : Tag*) = Element("input", children)
    def ins(children : Tag*) = Element("ins", children)
    def kbd(children : Tag*) = Element("kbd", children)
    def keygen(children : Tag*) = Element("keygen", children)
    def label(children : Tag*) = Element("label", children)
    def legend(children : Tag*) = Element("legend", children)
    def li(children : Tag*) = Element("li", children)
    def link(children : Tag*) = Element("link", children)
    def main(children : Tag*) = Element("main", children)
    def map(children : Tag*) = Element("map", children)
    def mark(children : Tag*) = Element("mark", children)
    def menu(children : Tag*) = Element("menu", children)
    def menuitem(children : Tag*) = Element("menuitem", children)
    def meta(children : Tag*) = Element("meta", children)
    def meter(children : Tag*) = Element("meter", children)
    def nav(children : Tag*) = Element("nav", children)
    def noframes(children : Tag*) = Element("noframes", children)
    def noscript(children : Tag*) = Element("noscript", children)
    def `object`(children : Tag*) = Element("object", children)
    def ol(children : Tag*) = Element("ol", children)
    def optgroup(children : Tag*) = Element("optgroup", children)
    def option(children : Tag*) = Element("option", children)
    def output(children : Tag*) = Element("output", children)
    def p(children : Tag*) = Element("p", children)
    def param(children : Tag*) = Element("param", children)
    def picture(children : Tag*) = Element("picture", children)
    def pre(children : Tag*) = Element("pre", children)
    def progress(children : Tag*) = Element("progress", children)
    def q(children : Tag*) = Element("q", children)
    def rp(children : Tag*) = Element("rp", children)
    def rt(children : Tag*) = Element("rt", children)
    def ruby(children : Tag*) = Element("ruby", children)
    def s(children : Tag*) = Element("s", children)
    def samp(children : Tag*) = Element("samp", children)
    def script(children : Tag*) = Element("script", children)
    def section(children : Tag*) = Element("section", children)
    def select(children : Tag*) = Element("select", children)
    def small(children : Tag*) = Element("small", children)
    def source(children : Tag*) = Element("source", children)
    def span(children : Tag*) = Element("span", children)
    def strike(children : Tag*) = Element("strike", children)
    def strong(children : Tag*) = Element("strong", children)
    def style(children : Tag*) = Element("style", children)
    def sub(children : Tag*) = Element("sub", children)
    def summary(children : Tag*) = Element("summary", children)
    def sup(children : Tag*) = Element("sup", children)
    def table(children : Tag*) = Element("table", children)
    def tbody(children : Tag*) = Element("tbody", children)
    def td(children : Tag*) = Element("td", children)
    def textarea(children : Tag*) = Element("textarea", children)
    def tfoot(children : Tag*) = Element("tfoot", children)
    def th(children : Tag*) = Element("th", children)
    def thead(children : Tag*) = Element("thead", children)
    def time(children : Tag*) = Element("time", children)
    def title(children : Tag*) = Element("title", children)
    def tr(children : Tag*) = Element("tr", children)
    def track(children : Tag*) = Element("track", children)
    def tt(children : Tag*) = Element("tt", children)
    def u(children : Tag*) = Element("u", children)
    def ul(children : Tag*) = Element("ul", children)
    def `var`(children : Tag*) = Element("var", children)
    def video(children : Tag*) = Element("video", children)
    def wbr(children : Tag*) = Element("wbr", children)
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

    def accept(value : String = "true") = Attribute("accept", value)
    def acceptCharset(value : String = "true") = Attribute("acceptCharset", value)
    def accessKey(value : String = "true") = Attribute("accessKey", value)
    def action(value : String = "true") = Attribute("action", value)
    def allowFullScreen(value : String = "true") = Attribute("allowFullScreen", value)
    def allowTransparency(value : String = "true") = Attribute("allowTransparency", value)
    def alt(value : String = "true") = Attribute("alt", value)
    def async(value : String = "true") = Attribute("async", value)
    def autoComplete(value : String = "true") = Attribute("autoComplete", value)
    def autoFocus(value : String = "true") = Attribute("autoFocus", value)
    def autoPlay(value : String = "true") = Attribute("autoPlay", value)
    def capture(value : String = "true") = Attribute("capture", value)
    def cellPadding(value : String = "true") = Attribute("cellPadding", value)
    def cellSpacing(value : String = "true") = Attribute("cellSpacing", value)
    def challenge(value : String = "true") = Attribute("challenge", value)
    def charSet(value : String = "true") = Attribute("charSet", value)
    def checked(value : String = "true") = Attribute("checked", value)
    def cite(value : String = "true") = Attribute("cite", value)
    def colSpan(value : String = "true") = Attribute("colSpan", value)
    def cols(value : String = "true") = Attribute("cols", value)
    def content(value : String = "true") = Attribute("content", value)
    def contentEditable(value : String = "true") = Attribute("contentEditable", value)
    def contextMenu(value : String = "true") = Attribute("contextMenu", value)
    def controls(value : String = "true") = Attribute("controls", value)
    def coords(value : String = "true") = Attribute("coords", value)
    def crossOrigin(value : String = "true") = Attribute("crossOrigin", value)
    def data(value : String = "true") = Attribute("data", value)
    def dateTime(value : String = "true") = Attribute("dateTime", value)
    def default(value : String = "true") = Attribute("default", value)
    def defer(value : String = "true") = Attribute("defer", value)
    def dir(value : String = "true") = Attribute("dir", value)
    def disabled(value : String = "true") = Attribute("disabled", value)
    def download(value : String = "true") = Attribute("download", value)
    def draggable(value : String = "true") = Attribute("draggable", value)
    def encType(value : String = "true") = Attribute("encType", value)
    def `for`(value : String = "true") = Attribute("for", value)
    def form(value : String = "true") = Attribute("form", value)
    def formAction(value : String = "true") = Attribute("formAction", value)
    def formEncType(value : String = "true") = Attribute("formEncType", value)
    def formMethod(value : String = "true") = Attribute("formMethod", value)
    def formNoValidate(value : String = "true") = Attribute("formNoValidate", value)
    def formTarget(value : String = "true") = Attribute("formTarget", value)
    def frameBorder(value : String = "true") = Attribute("frameBorder", value)
    def headers(value : String = "true") = Attribute("headers", value)
    def height(value : String = "true") = Attribute("height", value)
    def hidden(value : String = "true") = Attribute("hidden", value)
    def high(value : String = "true") = Attribute("high", value)
    def href(value : String = "true") = Attribute("href", value)
    def hrefLang(value : String = "true") = Attribute("hrefLang", value)
    /** An alias for the "for" attribute. */
    def htmlFor(value : String = "true") = Attribute("htmlFor", value)
    /** An alias for the "type" attribute. */
    def htmlType(value : String = "true") = Attribute("type", value)
    def httpEquiv(value : String = "true") = Attribute("httpEquiv", value)
    def icon(value : String = "true") = Attribute("icon", value)
    def id(value : String = "true") = Attribute("id", value)
    def inputMode(value : String = "true") = Attribute("inputMode", value)
    def integrity(value : String = "true") = Attribute("integrity", value)
    def is(value : String = "true") = Attribute("is", value)
    def keyParams(value : String = "true") = Attribute("keyParams", value)
    def keyType(value : String = "true") = Attribute("keyType", value)
    def kind(value : String = "true") = Attribute("kind", value)
    def label(value : String = "true") = Attribute("label", value)
    def lang(value : String = "true") = Attribute("lang", value)
    def list(value : String = "true") = Attribute("list", value)
    def loop(value : String = "true") = Attribute("loop", value)
    def low(value : String = "true") = Attribute("low", value)
    def manifest(value : String = "true") = Attribute("manifest", value)
    def marginHeight(value : String = "true") = Attribute("marginHeight", value)
    def marginWidth(value : String = "true") = Attribute("marginWidth", value)
    def max(value : String = "true") = Attribute("max", value)
    def maxLength(value : String = "true") = Attribute("maxLength", value)
    def media(value : String = "true") = Attribute("media", value)
    def mediaGroup(value : String = "true") = Attribute("mediaGroup", value)
    def method(value : String = "true") = Attribute("method", value)
    def min(value : String = "true") = Attribute("min", value)
    def minLength(value : String = "true") = Attribute("minLength", value)
    def multiple(value : String = "true") = Attribute("multiple", value)
    def muted(value : String = "true") = Attribute("muted", value)
    def name(value : String = "true") = Attribute("name", value)
    def noValidate(value : String = "true") = Attribute("noValidate", value)
    def nonce(value : String = "true") = Attribute("nonce", value)
    def open(value : String = "true") = Attribute("open", value)
    def optimum(value : String = "true") = Attribute("optimum", value)
    def pattern(value : String = "true") = Attribute("pattern", value)
    def placeholder(value : String = "true") = Attribute("placeholder", value)
    def poster(value : String = "true") = Attribute("poster", value)
    def preload(value : String = "true") = Attribute("preload", value)
    def profile(value : String = "true") = Attribute("profile", value)
    def radioGroup(value : String = "true") = Attribute("radioGroup", value)
    def readOnly(value : String = "true") = Attribute("readOnly", value)
    def rel(value : String = "true") = Attribute("rel", value)
    def required(value : String = "true") = Attribute("required", value)
    def reversed(value : String = "true") = Attribute("reversed", value)
    def role(value : String = "true") = Attribute("role", value)
    def rowSpan(value : String = "true") = Attribute("rowSpan", value)
    def rows(value : String = "true") = Attribute("rows", value)
    def sandbox(value : String = "true") = Attribute("sandbox", value)
    def scope(value : String = "true") = Attribute("scope", value)
    def scoped(value : String = "true") = Attribute("scoped", value)
    def scrolling(value : String = "true") = Attribute("scrolling", value)
    def seamless(value : String = "true") = Attribute("seamless", value)
    def selected(value : String = "true") = Attribute("selected", value)
    def shape(value : String = "true") = Attribute("shape", value)
    def size(value : String = "true") = Attribute("size", value)
    def sizes(value : String = "true") = Attribute("sizes", value)
    def span(value : String = "true") = Attribute("span", value)
    def spellCheck(value : String = "true") = Attribute("spellCheck", value)
    def src(value : String = "true") = Attribute("src", value)
    def srcDoc(value : String = "true") = Attribute("srcDoc", value)
    def srcLang(value : String = "true") = Attribute("srcLang", value)
    def srcSet(value : String = "true") = Attribute("srcSet", value)
    def start(value : String = "true") = Attribute("start", value)
    def step(value : String = "true") = Attribute("step", value)
    def style(value : String = "true") = Attribute("style", value)
    def summary(value : String = "true") = Attribute("summary", value)
    def tabIndex(value : String = "true") = Attribute("tabIndex", value)
    def target(value : String = "true") = Attribute("target", value)
    def title(value : String = "true") = Attribute("title", value)
    def `type`(value : String = "true") = Attribute("type", value)
    def useMap(value : String = "true") = Attribute("useMap", value)
    def value(value : String = "true") = Attribute("value", value)
    def width(value : String = "true") = Attribute("width", value)
    def wmode(value : String = "true") = Attribute("wmode", value)
    def wrap(value : String = "true") = Attribute("wrap", value)

    def className(value : String*) = Attribute("className", value.mkString(" "))
    /** Set up an event handler. Note that the event name must contain the 'on', eg. 'onClick' instead of 'click'. */
    def on(eventName : String, handler : SyntheticEvent => Unit) = EventHandler(eventName, handler)
}

/** A convenience object for constructing Styles. */
object S {
    /** A synonym for Style(name, value). */
    def apply(name : String, value : String = "") = Style(name, value)

    val alignContent = Style("align-content", "")
    val alignItems = Style("align-items", "")
    val alignSelf = Style("align-self", "")
    val all = Style("all", "")
    /** It's recommended that you use the CssKeyframe class instead of this property. */
    val animation = Style("animation", "")
    val animationDelay = Style("animation-delay", "")
    val animationDirection = Style("animation-direction", "")
    val animationDuration = Style("animation-duration", "")
    val animationFillMode = Style("animation-fill-mode", "")
    val animationIterationCount = Style("animation-iteration-count", "")
    /** It's recommended that you use the CssKeyframe class instead of this property. */
    val animationName = Style("animation-name", "")
    val animationPlayState = Style("animation-play-state", "")
    val animationTimingFunction = Style("animation-timing-function", "")
    val backfaceVisibility = Style("backface-visibility", "")
    val background = Style("background", "")
    val backgroundAttachment = Style("background-attachment", "")
    val backgroundBlendMode = Style("background-blend-mode", "")
    val backgroundClip = Style("background-clip", "")
    val backgroundColor = Style("background-color", "")
    val backgroundImage = Style("background-image", "")
    val backgroundOrigin = Style("background-origin", "")
    val backgroundPosition = Style("background-position", "")
    val backgroundRepeat = Style("background-repeat", "")
    val backgroundSize = Style("background-size", "")
    val border = Style("border", "")
    val borderBottom = Style("border-bottom", "")
    val borderBottomColor = Style("border-bottom-color", "")
    val borderBottomLeftRadius = Style("border-bottom-left-radius", "")
    val borderBottomRightRadius = Style("border-bottom-right-radius", "")
    val borderBottomStyle = Style("border-bottom-style", "")
    val borderBottomWidth = Style("border-bottom-width", "")
    val borderCollapse = Style("border-collapse", "")
    val borderColor = Style("border-color", "")
    val borderImage = Style("border-image", "")
    val borderImageOutset = Style("border-image-outset", "")
    val borderImageRepeat = Style("border-image-repeat", "")
    val borderImageSlice = Style("border-image-slice", "")
    val borderImageSource = Style("border-image-source", "")
    val borderImageWidth = Style("border-image-width", "")
    val borderLeft = Style("border-left", "")
    val borderLeftColor = Style("border-left-color", "")
    val borderLeftStyle = Style("border-left-style", "")
    val borderLeftWidth = Style("border-left-width", "")
    val borderRadius = Style("border-radius", "")
    val borderRight = Style("border-right", "")
    val borderRightColor = Style("border-right-color", "")
    val borderRightStyle = Style("border-right-style", "")
    val borderRightWidth = Style("border-right-width", "")
    val borderSpacing = Style("border-spacing", "")
    val borderStyle = Style("border-style", "")
    val borderTop = Style("border-top", "")
    val borderTopColor = Style("border-top-color", "")
    val borderTopLeftRadius = Style("border-top-left-radius", "")
    val borderTopRightRadius = Style("border-top-right-radius", "")
    val borderTopStyle = Style("border-top-style", "")
    val borderTopWidth = Style("border-top-width", "")
    val borderWidth = Style("border-width", "")
    val bottom = Style("bottom", "")
    val boxShadow = Style("box-shadow", "")
    val boxSizing = Style("box-sizing", "")
    val captionSide = Style("caption-side", "")
    val clear = Style("clear", "")
    val clip = Style("clip", "")
    val color = Style("color", "")
    val columnCount = Style("column-count", "")
    val columnFill = Style("column-fill", "")
    val columnGap = Style("column-gap", "")
    val columnRule = Style("column-rule", "")
    val columnRuleColor = Style("column-rule-color", "")
    val columnRuleStyle = Style("column-rule-style", "")
    val columnRuleWidth = Style("column-rule-width", "")
    val columnSpan = Style("column-span", "")
    val columnWidth = Style("column-width", "")
    val columns = Style("columns", "")
    val content = Style("content", "")
    val counterIncrement = Style("counter-increment", "")
    val counterReset = Style("counter-reset", "")
    val cursor = Style("cursor", "")
    val direction = Style("direction", "")
    val display = Style("display", "")
    val emptyCells = Style("empty-cells", "")
    val filter = Style("filter", "")
    val flex = Style("flex", "")
    val flexBasis = Style("flex-basis", "")
    val flexDirection = Style("flex-direction", "")
    val flexFlow = Style("flex-flow", "")
    val flexGrow = Style("flex-grow", "")
    val flexShrink = Style("flex-shrink", "")
    val flexWrap = Style("flex-wrap", "")
    val float = Style("float", "")
    val font = Style("font", "")
    val fontFamily = Style("font-family", "")
    val fontSize = Style("font-size", "")
    val fontSizeAdjust = Style("font-size-adjust", "")
    val fontStretch = Style("font-stretch", "")
    val fontStyle = Style("font-style", "")
    val fontVariant = Style("font-variant", "")
    val fontWeight = Style("font-weight", "")
    val hangingPunctuation = Style("hanging-punctuation", "")
    val height = Style("height", "")
    val justifyContent = Style("justify-content", "")
    val left = Style("left", "")
    val letterSpacing = Style("letter-spacing", "")
    val lineHeight = Style("line-height", "")
    val listStyle = Style("list-style", "")
    val listStyleImage = Style("list-style-image", "")
    val listStylePosition = Style("list-style-position", "")
    val listStyleType = Style("list-style-type", "")
    val margin = Style("margin", "")
    val marginBottom = Style("margin-bottom", "")
    val marginLeft = Style("margin-left", "")
    val marginRight = Style("margin-right", "")
    val marginTop = Style("margin-top", "")
    val maxHeight = Style("max-height", "")
    val maxWidth = Style("max-width", "")
    val minHeight = Style("min-height", "")
    val minWidth = Style("min-width", "")
    val navDown = Style("nav-down", "")
    val navIndex = Style("nav-index", "")
    val navLeft = Style("nav-left", "")
    val navRight = Style("nav-right", "")
    val navUp = Style("nav-up", "")
    val opacity = Style("opacity", "")
    val order = Style("order", "")
    val outline = Style("outline", "")
    val outlineColor = Style("outline-color", "")
    val outlineOffset = Style("outline-offset", "")
    val outlineStyle = Style("outline-style", "")
    val outlineWidth = Style("outline-width", "")
    val overflow = Style("overflow", "")
    val overflowX = Style("overflow-x", "")
    val overflowY = Style("overflow-y", "")
    val padding = Style("padding", "")
    val paddingBottom = Style("padding-bottom", "")
    val paddingLeft = Style("padding-left", "")
    val paddingRight = Style("padding-right", "")
    val paddingTop = Style("padding-top", "")
    val pageBreakAfter = Style("page-break-after", "")
    val pageBreakBefore = Style("page-break-before", "")
    val pageBreakInside = Style("page-break-inside", "")
    val perspective = Style("perspective", "")
    val perspectiveOrigin = Style("perspective-origin", "")
    val position = Style("position", "")
    val quotes = Style("quotes", "")
    val resize = Style("resize", "")
    val right = Style("right", "")
    val tabSize = Style("tab-size", "")
    val tableLayout = Style("table-layout", "")
    val textAlign = Style("text-align", "")
    val textAlignLast = Style("text-align-last", "")
    val textDecoration = Style("text-decoration", "")
    val textDecorationColor = Style("text-decoration-color", "")
    val textDecorationLine = Style("text-decoration-line", "")
    val textDecorationStyle = Style("text-decoration-style", "")
    val textIndent = Style("text-indent", "")
    val textJustify = Style("text-justify", "")
    val textOverflow = Style("text-overflow", "")
    val textShadow = Style("text-shadow", "")
    val textTransform = Style("text-transform", "")
    val top = Style("top", "")
    val transform = Style("transform", "")
    val transformOrigin = Style("transform-origin", "")
    val transformStyle = Style("transform-style", "")
    val transition = Style("transition", "")
    val transitionDelay = Style("transition-delay", "")
    val transitionDuration = Style("transition-duration", "")
    val transitionProperty = Style("transition-property", "")
    val transitionTimingFunction = Style("transition-timing-function", "")
    val unicodeBidi = Style("unicode-bidi", "")
    val userSelect = Style("user-select", "")
    val verticalAlign = Style("vertical-align", "")
    val visibility = Style("visibility", "")
    val whiteSpace = Style("white-space", "")
    val width = Style("width", "")
    val wordBreak = Style("word-break", "")
    val wordSpacing = Style("word-spacing", "")
    val wordWrap = Style("word-wrap", "")
    val zIndex = Style("z-index", "")
}
