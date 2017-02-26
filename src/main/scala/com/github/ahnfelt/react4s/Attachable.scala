package com.github.ahnfelt.react4s

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.util._

/** Can be attached to a Component to listen for lifecycle events. */
trait Attachable {
    /** Called after componentWillRender() returns on the component to which this is attached. Use update() to signal to the component that state has changed. */
    def componentWillRender(update : () => Unit) : Unit = {}
    /** Called after componentWillUnmount() returns on the component to which this is attached. */
    def componentWillUnmount() : Unit = {}
}

// TODO: It's too easy to forget to attach() Attachables. Fix it.

/**
For loading things based on props and state asynchronously without introducing race conditions. Assuming itemId : P[Long], here's an example:
{{{
val itemName = Loader(this, itemId) { id =>
    Ajax.get("/item/" + id + "/name").map(_.responseText)
}

def render() = E.div(
    E.div(Text("Loading...")).when(itemName.loading()),
    itemName().map(name => E.div(Text(name))).getOrElse(TagList.empty),
    itemName.error().map(throwable => E.div(Text(throwable.getMessage))).getOrElse(TagList.empty)
)
}}}
*/
trait Loader[T] extends (() => Option[T]) {
    /** The last loaded value, if any. Not cleared when the most recent future fails. */
    def apply() : Option[T]
    /** The last error, if any. Cleared when the most recent future succeeds. */
    def error() : Option[Throwable]
    /** True until the most recent future completes, then false. */
    def loading() : Boolean
}

/** Used to create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. */
object Loader {
    trait AttachableLoader[T] extends Loader[T] with Attachable

    /** Create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. */
    def apply[I, O](component : Component[_], dependency : () => I)(future : I => Future[O]) : Loader[O] = component.attach(new AttachableLoader[O] {
        var lastDependency : Option[I] = None
        var lastValue : Option[O] = None
        var lastError : Option[Throwable] = None
        var isLoading : Boolean = false
        var lastVersion : Long = 0

        override def componentWillRender(update : () => Unit) : Unit = {
            val newDependency = dependency()
            if(!lastDependency.contains(newDependency)) {
                lastDependency = Some(newDependency)
                isLoading = true
                lastVersion += 1
                val version = lastVersion
                import scala.concurrent.ExecutionContext.Implicits.global
                future(newDependency).onComplete {
                    case Success(newValue) => if(version == lastVersion) {
                        lastValue = Some(newValue)
                        lastError = None
                        isLoading = false
                        update()
                    }
                    case Failure(newError) => if(version == lastVersion) {
                        lastError = Some(newError)
                        isLoading = false
                        update()
                    }
                }
            }
        }

        // Ensure we don't update the state after the component has been unmounted
        override def componentWillUnmount() = lastVersion += 1

        override def apply() : Option[O] = lastValue
        override def error() : Option[Throwable] = lastError
        override def loading() : Boolean = isLoading
    })
}

/** Set after a specified timeout, or on an interval. */
trait Timeout {
    /** Has the timeout triggered yet? */
    def apply() : Boolean
    /** The number of times the timeout has triggered (only ever higher than 1 for intervals). */
    def ticks() : Long
    /** The number of milliseconds since the timeout was started. */
    def elapsed() : Long
}

/** Used to run a function after a specified timeout, or on an interval. */
object Timeout {

    trait AttachableTimeout extends Timeout with Attachable

    /** Sets a timeout that restarts every time the dependency changes. If interval is set, it triggers every interval instead of just once. */
    def apply[T](component: Component[_], dependency : () => T, interval : Boolean = false)(milliseconds : T => Long) : Timeout = component.attach(new AttachableTimeout {
        var timeout : Option[SetTimeoutHandle] = None
        var oldValue : Option[T] = None
        var startTime = System.nanoTime()
        var triggered = 0

        private def setTimeout(update : () => Unit, duration : Long) : Unit = {
            timeout = Some(js.timers.setTimeout(duration) {
                triggered += 1
                update()
                if(interval) setTimeout(update, duration)
            })
        }

        override def componentWillRender(update : () => Unit) : Unit = {
            val newValue = dependency()
            if(!oldValue.contains(newValue)) {
                for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
                oldValue = Some(newValue)
                startTime = System.nanoTime()
                triggered = 0
                val duration = milliseconds(newValue)
                setTimeout(update, duration)
            }
        }

        override def componentWillUnmount() : Unit = {
            for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
        }

        override def apply() = triggered > 0
        override def ticks() = triggered
        override def elapsed() = (System.nanoTime() - startTime) / (1000 * 1000)
    })
}

/** Used to debounce or throttle changes. */
trait Debounce[T] extends (() => T) {
    /** Get the debounced value. */
    def apply() : T
}

object Debounce {

    trait AttachableDebounce[T] extends Debounce[T] with Attachable

    /** When the dependency is changed, don't propagate the value immediately - instead wait until no change has been made for the specified milliseconds. If immediate is set, propagate the first change after a pause immediately. */
    def apply[T](component : Component[_], dependency : () => T, milliseconds : Long = 250, immediate : Boolean = false) : Debounce[T] = component.attach(new AttachableDebounce[T] {
        private var timeout : Option[SetTimeoutHandle] = None
        private var oldValue : T = dependency()

        override def apply() = oldValue

        override def componentWillRender(update : () => Unit) : Unit = {
            val newValue = dependency()
            if(oldValue != newValue) {
                if(immediate && timeout.isEmpty) {
                    oldValue = newValue
                }
                for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
                timeout = Some(js.timers.setTimeout(milliseconds) {
                    oldValue = newValue
                    timeout = None
                    update()
                })
            }
        }

        override def componentWillUnmount() : Unit = {
            for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
        }
    })
}
