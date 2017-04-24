package com.github.ahnfelt.react4s

import scala.collection.mutable.ListBuffer

trait CssChild
case class CssPseudoClass(name : String, children : Seq[CssChild]) extends CssChild
case class CssMediaQuery(query : String, children : Seq[CssChild]) extends CssChild
case class CssSelector(selector : String, children : Seq[CssChild]) extends CssChild

object Css {

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
    def disabled(children : CssChild*) = CssPseudoClass("disabled", children)
    def evenChild(children : CssChild*) = CssPseudoClass("nth-child(even)", children)
    def oddChild(children : CssChild*) = CssPseudoClass("nth-child(odd)", children)
    def evenOfType(children : CssChild*) = CssPseudoClass("nth-of-type(even)", children)
    def oddOfType(children : CssChild*) = CssPseudoClass("nth-of-type(odd)", children)
    def first(children : CssChild*) = CssPseudoClass("first", children)
    def firstChild(children : CssChild*) = CssPseudoClass("first-child", children)
    def firstOfType(children : CssChild*) = CssPseudoClass("first-of-type", children)
    def focus(children : CssChild*) = CssPseudoClass("focus", children)
    def hover(children : CssChild*) = CssPseudoClass("hover", children)
    def indeterminate(children : CssChild*) = CssPseudoClass("indeterminate", children)
    def inRange(children : CssChild*) = CssPseudoClass("in-range", children)
    def invalid(children : CssChild*) = CssPseudoClass("invalid", children)
    def lastChild(children : CssChild*) = CssPseudoClass("last-child", children)
    def lastOfType(children : CssChild*) = CssPseudoClass("last-of-type", children)
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

    def not(cssClass : CssClass, children : CssChild*) = CssPseudoClass("not(." + cssClass.name + ")", children)
    def not(pseudoClass : CssPseudoClass, children : CssChild*) = CssPseudoClass("not(:" + pseudoClass.name + ")", children)
}

object CssChild {

    def cssToString(cssClass : CssClass, emitKeyframes : Boolean = false) : String = {
        val selector = "." + cssClass.name
        val builder = new StringBuilder()
        val keyframes = ListBuffer[CssKeyframes]()
        emitCssChildren(builder, keyframes, "", selector, cssClass.children)
        if(emitKeyframes) {
            for(frames <- keyframes) if(!frames.emitted) {
                frames.emitted = true
                builder.append(frames.toCss)
            }
        }
        builder.toString()
    }

    def emitCssChildren(builder : StringBuilder, keyframes : ListBuffer[CssKeyframes], media : String, selector : String, children : Seq[CssChild]) : Unit = {

        def flatten(children : Seq[CssChild]) : Seq[CssChild] = children.flatMap {
            case c : CssClass => flatten(c.children)
            case c => Seq(c)
        }

        val flattened = flatten(children)

        if(flattened.exists(_.isInstanceOf[Style])) {
            val mediaSpaces = if(media.nonEmpty) "  " else ""
            if(media.nonEmpty) builder.append("@media" + media + " {\n")
            builder.append(mediaSpaces + selector + " {\n")
            val animationNames = ListBuffer[String]()
            for(c <- flattened) c match {
                case s : Style => builder.append(mediaSpaces + "  " + Style.toStandardName(s.name) + ":" + s.value + ";\n")
                case f : CssKeyframes => animationNames += f.name
                case _ =>
            }
            if(animationNames.nonEmpty) {
                builder.append(mediaSpaces + "  animation-name: " + animationNames.mkString(", ") + ";\n")
            }
            builder.append(mediaSpaces + "}\n")
            if(media.nonEmpty) builder.append("}\n")
        }

        for(c <- flattened) c match {
            case CssPseudoClass(name, cs) => emitCssChildren(builder, keyframes, media, selector + ":" + name, cs)
            case CssMediaQuery(query, cs) => emitCssChildren(builder, keyframes, media + " " + query, selector, cs)
            case CssSelector(s, cs) => emitCssChildren(builder, keyframes, media, selector + s, cs)
            case frames : CssKeyframes =>
                keyframes += frames
            case _ =>
        }

    }

}

/** For serving .css files. Example:
<pre>
object FancyStylesheet extends CssStylesheet(
    FancyButtonCss,
    FancyInputCss,
    FancyLinkCss
)
</pre>
<p>Server-side:</p>
<pre>
val stylesheetToServeViaHttp = FancyStylesheet.emitAsStylesheet(true)
</pre>
<p>Client-side:</p>
<pre>
FancyStylesheet.emitAsStylesheet(false)
</pre>
<p>The emitAsStylesheet method must be called before components are rendered. Otherwise, the CSS classes may be emitted twice.</p>
*/
class CssStylesheet(cssClasses : CssClass*) {

    /** Emits the stylesheet code (or the empty string, if generateOutput is false) and marks all the cssClasses as emitted. */
    def emitAsStylesheet(generateOutput : Boolean) : String = {
        val builder = new StringBuilder()
        for(cssClass <- cssClasses) {
            cssClass.emitted = true
            if(generateOutput) {
                builder.append(CssChild.cssToString(cssClass, emitKeyframes = true))
                builder.append("\n")
            }
        }
        builder.toString()
    }

}
