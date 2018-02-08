package com.github.ahnfelt.react4s

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.util._

/**
  Can be attached to a Component to listen for lifecycle events.
  Please note that componentWillRender() of attachables are run <u>after</u> componentWillRender() of the component,
  so during that, the values of attachables won't be updated yet.
 */
trait Attachable {
    /** Called after componentWillRender() returns on the component to which this is attached. The "get" argument lets you read props etc. Use update() to signal to the component that state has changed. Note that componentWillRender() won't fire for reactBridge.renderToString and .renderToStaticMarkup. */
    def componentWillRender(get : Get) : Unit = {}
    /** Called after componentWillUnmount() returns on the component to which this is attached. The "get" argument lets you read props etc. */
    def componentWillUnmount(get : Get) : Unit = {}
}

/**
For loading things based on props and state asynchronously without introducing race conditions. Assuming itemId : P[Long], here's an example:
{{{
val itemName = Loader(this, itemId) { id =>
    Ajax.get("/item/" + id + "/name").map(_.responseText)
}

def render(get : Get) = E.div(
    E.div(Text("Loading...")).when(get(itemName.loading)),
    get(itemName).map(name => E.div(Text(name))).getOrElse(TagList.empty),
    get(itemName.error).map(throwable => E.div(Text(throwable.getMessage))).getOrElse(TagList.empty)
)
}}}
*/
trait Loader[T] extends (Get => Option[T]) {
    /** The last loaded value, if any. Not cleared when the most recent future fails. */
    def apply(get : Get) : Option[T]
    /** The last error, if any. Cleared when the most recent future succeeds. */
    def error(get : Get) : Option[Throwable]
    /** True until the most recent future completes, then false. */
    def loading(get : Get) : Boolean
    /** Force the loader to reload. */
    def retry() : Unit
}

/** Used to create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. To avoid race conditions, it waits for the old future to complete before starting a new. If initial is Some(initialValue), delays first load until dependency() != initialValue(). */
object Loader {
    trait AttachableLoader[T] extends Loader[T] with Attachable

    /** Create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. To avoid race conditions, it waits for the old future to complete before starting a new. If initial is Some(initialValue), delays first load until dependency() != initialValue(). */
    def apply[I, O](component : Component[_], dependency : Get => I, initial : Option[Get => I] = None)(future : I => Future[O]) : Loader[O] = component.attach(new AttachableLoader[O] {
        var lastDependency : Option[I] = None
        var nextDependency : Option[I] = None
        var lastValue : Option[O] = None
        var lastError : Option[Throwable] = None
        var isLoading : Boolean = false
        var lastVersion : Long = 0
        var lastRetries : Long = 0
        var retries : Long  = 0
        var changedSinceInitial : Boolean = false

        override def componentWillRender(get : Get) : Unit = {
            val newDependency = get(dependency)
            val isInitial = !changedSinceInitial && initial.exists(i => get(i) == newDependency)
            if((!lastDependency.contains(newDependency) || retries != lastRetries) && !isInitial && !isLoading) {
                changedSinceInitial = true
                lastRetries = retries
                lastDependency = Some(newDependency)
                isLoading = true
                lastVersion += 1
                val version = lastVersion
                import scala.concurrent.ExecutionContext.Implicits.global
                future(newDependency).onComplete { result =>
                    result match {
                        case Success(newValue) => if(version == lastVersion) {
                            lastValue = Some(newValue)
                            lastError = None
                        }
                        case Failure(newError) => if(version == lastVersion) {
                            lastError = Some(newError)
                        }
                    }
                    isLoading = false
                    if(!lastDependency.contains(get(dependency)) || retries != lastRetries) {
                        componentWillRender(get)
                    } else {
                        component.update()
                    }
                }
            }
        }

        // Ensure we don't update the state after the component has been unmounted
        override def componentWillUnmount(get : Get) : Unit = lastVersion += 1

        override def retry() : Unit = { retries += 1; component.update() }
        override def apply(get : Get) : Option[O] = lastValue
        override def error(get : Get) : Option[Throwable] = lastError
        override def loading(get : Get) : Boolean = isLoading
    })
}

/** Set after a specified timeout, or on an interval. */
trait Timeout extends (Get => Boolean) {
    /** Has the timeout triggered yet? */
    def apply(get : Get) : Boolean
    /** The number of times the timeout has triggered (only ever higher than 1 for intervals). */
    def ticks(get : Get) : Long
    /** The number of milliseconds since the timeout was started. */
    def elapsed(get : Get) : Long
}

/** Used to run a function after a specified timeout, or on an interval. */
object Timeout {

    trait AttachableTimeout extends Timeout with Attachable

    /** Sets a timeout that restarts every time the dependency changes. If interval is set, it triggers every interval instead of just once. */
    def apply[T](component: Component[_], dependency : Get => T, interval : Boolean = false)(milliseconds : T => Long) : Timeout = component.attach(new AttachableTimeout {
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

        override def componentWillRender(get : Get) : Unit = {
            val newValue = get(dependency)
            if(!oldValue.contains(newValue)) {
                for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
                oldValue = Some(newValue)
                startTime = System.nanoTime()
                triggered = 0
                val duration = milliseconds(newValue)
                setTimeout(component.update, duration)
            }
        }

        override def componentWillUnmount(get : Get) : Unit = {
            for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
        }

        override def apply(get : Get) = triggered > 0
        override def ticks(get : Get) = triggered
        override def elapsed(get : Get) = (System.nanoTime() - startTime) / (1000 * 1000)
    })
}

/** Used to debounce or throttle changes. */
trait Debounce[T] extends (Get => T) {
    /** Get the debounced value. */
    def apply(get : Get) : T
}

object Debounce {

    trait AttachableDebounce[T] extends Debounce[T] with Attachable

    /** When the dependency is changed, don't propagate the value immediately - instead wait until no change has been made for the specified milliseconds. If immediate is set, propagate the first change after a pause immediately. */
    def apply[T](component : Component[_], dependency : Get => T, milliseconds : Long = 250, immediate : Boolean = false) : Debounce[T] = component.attach(new AttachableDebounce[T] {
        private var timeout : Option[SetTimeoutHandle] = None
        private var oldValue : T = Get.Unsafe(dependency)

        override def apply(get : Get) = oldValue

        override def componentWillRender(get : Get) : Unit = {
            val newValue = get(dependency)
            if(oldValue != newValue) {
                if(immediate && timeout.isEmpty) {
                    oldValue = newValue
                }
                for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
                timeout = Some(js.timers.setTimeout(milliseconds) {
                    oldValue = newValue
                    timeout = None
                    component.update()
                })
            }
        }

        override def componentWillUnmount(get : Get) : Unit = {
            for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
        }
    })
}
