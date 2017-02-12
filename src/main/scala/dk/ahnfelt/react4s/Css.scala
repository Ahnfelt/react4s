package dk.ahnfelt.react4s

import scala.scalajs.js

trait CssChild
case class CssPseudoClass(name : String, children : Seq[CssChild]) extends CssChild
case class CssMediaQuery(query : String, children : Seq[CssChild]) extends CssChild
case class CssSelector(selector : String, children : Seq[CssChild]) extends CssChild

object Css {

    private[react4s] var nextClassId = 0

    def selector(selector : String, children : CssChild*) = CssSelector(selector, children)
    def media(query : String, children : CssChild*) = CssMediaQuery(query, children)
    def pseudo(pseudoClass : String, children : CssChild*) = CssPseudoClass(pseudoClass, children)
    def cssClass(className : String, children : CssChild*) = CssSelector("." + className, children)
    def directChild(tagName : String, children : CssChild*) = CssSelector(">" + tagName, children)
    def mediaSmall(children : CssChild*) = CssMediaQuery("(max-width: 991px)", children)
    def mediaLarge(children : CssChild*) = CssMediaQuery("(min-width: 992px)", children)

    def active(children : CssChild*) = CssPseudoClass("active", children)
    def checked(children : CssChild*) = CssPseudoClass("checked", children)
    def default(children : CssChild*) = CssPseudoClass("default", children)
    def empty(children : CssChild*) = CssPseudoClass("empty", children)
    def enabled(children : CssChild*) = CssPseudoClass("enabled", children)
    def first(children : CssChild*) = CssPseudoClass("first", children)
    def firstChild(children : CssChild*) = CssPseudoClass("first-child", children)
    def firstOfType(children : CssChild*) = CssPseudoClass("first-of-type", children)
    def focus(children : CssChild*) = CssPseudoClass("focus", children)
    def hover(children : CssChild*) = CssPseudoClass("hover", children)
    def indeterminate(children : CssChild*) = CssPseudoClass("indeterminate", children)
    def inRange(children : CssChild*) = CssPseudoClass("in-range", children)
    def invalid(children : CssChild*) = CssPseudoClass("invalid", children)
    def lastChild(children : CssChild*) = CssPseudoClass("lastChild", children)
    def lastOfType(children : CssChild*) = CssPseudoClass("lastOfType", children)
    def optional(children : CssChild*) = CssPseudoClass("optional", children)
    def outOfRange(children : CssChild*) = CssPseudoClass("out-of-range", children)
    def placeholderShown(children : CssChild*) = CssPseudoClass("placeholder-shown", children)
    def readOnly(children : CssChild*) = CssPseudoClass("read-only", children)
    def readWrite(children : CssChild*) = CssPseudoClass("read-write", children)
    def required(children : CssChild*) = CssPseudoClass("required", children)
    def target(children : CssChild*) = CssPseudoClass("target", children)
    def valid(children : CssChild*) = CssPseudoClass("valid", children)
    def visited(children : CssChild*) = CssPseudoClass("visited", children)
    def before(children : CssChild*) = CssPseudoClass(":before", children)
    def after(children : CssChild*) = CssPseudoClass(":after", children)
    def firstLetter(children : CssChild*) = CssPseudoClass(":first-letter", children)
    def firstLine(children : CssChild*) = CssPseudoClass(":first-line", children)
    def selection(children : CssChild*) = CssPseudoClass(":selection", children)
}

object CssChild {

    def cssToString(cssClass : CssClass) : String = {
        val selector = "." + cssClass.name
        val builder = new StringBuilder()
        emitCssChildren(builder, "", selector, cssClass.children)
        builder.toString()
    }

    def emitCssChildren(builder : StringBuilder, media : String, selector : String, children : Seq[CssChild]) : Unit = {

        if(children.exists(_.isInstanceOf[Style])) {
            val mediaSpaces = if(media.nonEmpty) "  " else ""
            if(media.nonEmpty) builder.append("@media" + media + " {\n")
            builder.append(mediaSpaces + selector + " {\n")
            for(c <- children) c match {
                case s : Style => builder.append(mediaSpaces + "  " + s + "\n")
                case _ =>
            }
            builder.append(mediaSpaces + "}\n")
            if(media.nonEmpty) builder.append("}\n")
        }

        for(c <- children) c match {
            case CssPseudoClass(name, cs) => emitCssChildren(builder, media, selector + ":" + name, cs)
            case CssMediaQuery(query, cs) => emitCssChildren(builder, media + " " + query, selector, cs)
            case CssSelector(s, cs) => emitCssChildren(builder, media, selector + s, cs)
            case _ =>
        }

    }

}
