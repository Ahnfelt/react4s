package dk.ahnfelt.react4s

import scala.scalajs.js

@js.native
trait SyntheticEvent extends js.Object {
    val bubbles : Boolean
    val cancelable : Boolean
    val currentTarget : js.Dynamic
    val defaultPrevented : Boolean
    val eventPhase : Double
    val isTrusted : Boolean
    val nativeEvent : js.Dynamic
    def preventDefault() : Unit
    def isDefaultPrevented() : Boolean
    def stopPropagation() : Unit
    def isPropagationStopped() : Boolean
    val target : js.Dynamic
    val timeStamp : Double
    val `type` : String
}

@js.native
trait KeyboardEvent extends SyntheticEvent {
    val altKey : Boolean
    val charCode : Int
    val ctrlKey : Boolean
    def getModifierState(key : String) : Boolean
    val key : String
    val keyCode : Int
    val locale : String
    val location : Int
    val metaKey : Boolean
    val repeat : Boolean
    val shiftKey : Boolean
    val which : Int
}

@js.native
trait MouseEvent extends SyntheticEvent {
    val altKey : Boolean
    val button : Int
    val buttons : Int
    val clientX : Double
    val clientY : Double
    val ctrlKey : Boolean
    def getModifierState(key : String) : Boolean
    val metaKey : Boolean
    val pageX : Double
    val pageY : Double
    val relatedTarget : js.Dynamic
    val screenX : Double
    val screenY : Double
    val shiftKey : Boolean
}

@js.native
trait TouchEvent extends SyntheticEvent {
    val altKey : Boolean
    val changedTouches : js.Dynamic
    val ctrlKey : Boolean
    def getModifierState(key : String) : Boolean
    val metaKey : Boolean
    val shiftKey : Boolean
    val targetTouches : js.Dynamic
    val touches : js.Dynamic
}

abstract class CommonEvents {
    def onClick(handler : MouseEvent => Unit) = EventHandler("onClick", handler.asInstanceOf[SyntheticEvent => Unit])
    def onMouseDown(handler : MouseEvent => Unit) = EventHandler("onMouseDown", handler.asInstanceOf[SyntheticEvent => Unit])
    def onMouseUp(handler : MouseEvent => Unit) = EventHandler("onMouseUp", handler.asInstanceOf[SyntheticEvent => Unit])
    def onMouseEnter(handler : MouseEvent => Unit) = EventHandler("onMouseEnter", handler.asInstanceOf[SyntheticEvent => Unit])
    def onMouseLeave(handler : MouseEvent => Unit) = EventHandler("onMouseLeave", handler.asInstanceOf[SyntheticEvent => Unit])
    def onKeyPress(handler : KeyboardEvent => Unit) = EventHandler("onKeyPress", handler.asInstanceOf[SyntheticEvent => Unit])
    def onKeyDown(handler : KeyboardEvent => Unit) = EventHandler("onKeyDown", handler.asInstanceOf[SyntheticEvent => Unit])
    def onKeyUp(handler : KeyboardEvent => Unit) = EventHandler("onKeyUp", handler.asInstanceOf[SyntheticEvent => Unit])
    def onFocus(handler : SyntheticEvent => Unit) = EventHandler("onFocus", handler)
    def onBlur(handler : SyntheticEvent => Unit) = EventHandler("onBlur", handler)
    def onChange(handler : SyntheticEvent => Unit) = EventHandler("onChange", handler)
}
