package com.github.ahnfelt.react4s

import scala.scalajs.js

/** An dynamically typed prop, for JsComponents only. */
case class JsProp(name: String, value: js.Any) extends JsTag {

  /** Conditionally replaces the tag with Empty, which does nothing. */
  def when(condition: Boolean): JsTag = if (condition) this else Tags.empty
}

/** An dynamically typed prop of elements or components, for JsComponents only. */
case class JsPropChildren(name: String, elements: Seq[Node]) extends JsTag {

  /** Conditionally replaces the tag with Empty, which does nothing. */
  def when(condition: Boolean): JsTag = if (condition) this else Tags.empty
}

object J {

  /** Create a dynamically typed prop, for JsComponents only. */
  def apply(name: String, value: js.Any) = JsProp(name, value)

  /** Create a dynamically typed function prop, for JsComponents only. */
  def apply(name: String, value: js.Function) = JsProp(name, value)

  /** Create a dynamically typed styles prop, for JsComponents only. */
  def apply(name: String, styles: Style*) =
    JsProp(name,
           js.Dictionary[String](styles.map(s =>
             Style.toReactName(s.name) -> s.value): _*))

  /** Create a dynamically typed nodes prop, for JsComponents only. */
  def apply(name: String, nodes: Node*) = JsPropChildren(name, nodes)
}
