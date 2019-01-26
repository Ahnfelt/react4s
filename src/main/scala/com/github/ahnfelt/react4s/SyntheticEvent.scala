package com.github.ahnfelt.react4s

import scala.scalajs.js

@js.native
trait SyntheticEvent extends js.Object {
  def persist(): Unit

  val bubbles: Boolean
  val cancelable: Boolean
  val currentTarget: js.Dynamic
  val defaultPrevented: Boolean
  val eventPhase: Double
  val isTrusted: Boolean
  val nativeEvent: js.Dynamic

  def preventDefault(): Unit

  def isDefaultPrevented(): Boolean

  def stopPropagation(): Unit

  def isPropagationStopped(): Boolean

  val target: js.Dynamic
  val timeStamp: Double
  val `type`: String
}

@js.native
trait KeyboardEvent extends SyntheticEvent {
  val altKey: Boolean
  val charCode: Int
  val ctrlKey: Boolean

  def getModifierState(key: String): Boolean

  val key: String
  val keyCode: Int
  val locale: String
  val location: Int
  val metaKey: Boolean
  val repeat: Boolean
  val shiftKey: Boolean
  val which: Int
}

@js.native
trait MouseEvent extends SyntheticEvent {
  val altKey: Boolean
  val button: Int
  val buttons: Int
  val clientX: Double
  val clientY: Double
  val ctrlKey: Boolean

  def getModifierState(key: String): Boolean

  val metaKey: Boolean
  val pageX: Double
  val pageY: Double
  val relatedTarget: js.Dynamic
  val screenX: Double
  val screenY: Double
  val shiftKey: Boolean
}

@js.native
trait TouchEvent extends SyntheticEvent {
  val altKey: Boolean
  val changedTouches: js.Dynamic
  val ctrlKey: Boolean

  def getModifierState(key: String): Boolean

  val metaKey: Boolean
  val shiftKey: Boolean
  val targetTouches: js.Dynamic
  val touches: js.Dynamic
}

abstract class CommonEvents {

  type Handler = SyntheticEvent => Unit
  type MouseHandler = MouseEvent => Unit
  type KeyboardHandler = KeyboardEvent => Unit
  type TouchHandler = TouchEvent => Unit

  def onClick(handler: MouseHandler) = EventHandler("onClick", handler.asInstanceOf[Handler])

  def onMouseDown(handler: MouseHandler) = EventHandler("onMouseDown", handler.asInstanceOf[Handler])

  def onMouseUp(handler: MouseHandler) = EventHandler("onMouseUp", handler.asInstanceOf[Handler])

  def onMouseEnter(handler: MouseHandler) = EventHandler("onMouseEnter", handler.asInstanceOf[Handler])

  def onMouseLeave(handler: MouseHandler) = EventHandler("onMouseLeave", handler.asInstanceOf[Handler])

  def onKeyPress(handler: KeyboardHandler) = EventHandler("onKeyPress", handler.asInstanceOf[Handler])

  def onKeyDown(handler: KeyboardHandler) = EventHandler("onKeyDown", handler.asInstanceOf[Handler])

  def onKeyUp(handler: KeyboardHandler) = EventHandler("onKeyUp", handler.asInstanceOf[Handler])

  def onFocus(handler: Handler) = EventHandler("onFocus", handler)

  def onBlur(handler: Handler) = EventHandler("onBlur", handler)

  def onChange(handler: Handler) = EventHandler("onChange", handler)

  def onSubmit(handler: Handler) = EventHandler("onSubmit", handler)

  def onTouchCancel(handler: TouchHandler) = EventHandler("onTouchCancel", handler.asInstanceOf[Handler])

  def onTouchEnd(handler: TouchHandler) = EventHandler("onTouchEnd", handler.asInstanceOf[Handler])

  def onTouchMove(handler: TouchHandler) = EventHandler("onTouchMove", handler.asInstanceOf[Handler])

  def onTouchStart(handler: TouchHandler) = EventHandler("onTouchStart", handler.asInstanceOf[Handler])

  def onDrag(handler: MouseHandler) = EventHandler("onDrag", handler.asInstanceOf[Handler])

  def onDragEnd(handler: MouseHandler) = EventHandler("onDragEnd", handler.asInstanceOf[Handler])

  def onDragEnter(handler: MouseHandler) = EventHandler("onDragEnter", handler.asInstanceOf[Handler])

  def onDragExit(handler: MouseHandler) = EventHandler("onDragExit", handler.asInstanceOf[Handler])

  def onDragLeave(handler: MouseHandler) = EventHandler("onDragLeave", handler.asInstanceOf[Handler])

  def onDragOver(handler: MouseHandler) = EventHandler("onDragOver", handler.asInstanceOf[Handler])

  def onDragStart(handler: MouseHandler) = EventHandler("onDragStart", handler.asInstanceOf[Handler])

  def onDrop(handler: MouseHandler) = EventHandler("onDrop", handler.asInstanceOf[Handler])
}
