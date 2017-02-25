package dk.ahnfelt.react4s

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

/**
For loading things based on props and state asynchronously without introducing race conditions. Assuming itemId : P[Long], here's an example:
{{{
val itemName = attach(Loader(itemId) { id =>
    Ajax.get("/item/" + id + "/name")
})

def render() = E.div(
    E.div(Text("Loading...")).when(itemName.loading()),
    itemName().map(name => E.div(Text(name))).getOrElse(TagList.empty),
    itemName.error().map(throwable => E.div(Text(throwable.getMessage))).getOrElse(TagList.empty)
)
}}}
*/
trait Loader[T] {
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
    def apply[I, O](dependency : () => I)(future : I => Future[O]) : AttachableLoader[O] = new AttachableLoader[O] {
        var lastDependency : Option[I] = None
        var lastValue : Option[O] = None
        var lastError : Option[Throwable] = None
        var isLoading : Boolean = false
        var lastVersion : Long = 0
        var unmounted = false

        override def componentWillRender(update : () => Unit) : Unit = {
            val newDependency = dependency()
            if(!lastDependency.contains(newDependency)) {
                lastDependency = Some(newDependency)
                isLoading = true
                lastVersion += 1
                val version = lastVersion
                import scala.concurrent.ExecutionContext.Implicits.global
                future(newDependency).onComplete {
                    case Success(newValue) => if(version == lastVersion && !unmounted) {
                        lastValue = Some(newValue)
                        lastError = None
                        isLoading = false
                        update()
                    }
                    case Failure(newError) => if(version == lastVersion && !unmounted) {
                        lastError = Some(newError)
                        isLoading = false
                        update()
                    }
                }
            }
        }


        override def componentWillUnmount() = unmounted = true

        override def apply() : Option[O] = lastValue
        override def error() : Option[Throwable] = lastError
        override def loading() : Boolean = isLoading
    }
}

/** Used to run a function after a specified timeout, or on an interval. */
object Timeout {

    /** Sets a timeout of the specified duration. When it triggers, onTimeout is called. If interval is set, onTimeout is called on every interval instead of just once. */
    def simple(milliseconds : Long, interval : Boolean = false)(onTimeout : () => Unit) : Attachable = new Attachable {
        var timeout : SetTimeoutHandle = _
        def setTimeout() : Unit = {
            timeout = js.timers.setTimeout(milliseconds) {
                onTimeout()
                if(interval) setTimeout()
            }
        }
        setTimeout()
        override def componentWillUnmount() : Unit = {
            js.timers.clearTimeout(timeout)
        }
    }

    /** Sets a timeout of the specified duration. When it starts, onStart is called. When it triggers, onTimeout is called. Every time the dependency changes, the old timeout is cancelled and a new one starts. If interval is set, onTimeout is called on every interval instead of just once. */
    def apply[T](dependency : () => T)(milliseconds : T => Long, onStart : T => Unit = {_ : T => }, interval : Boolean = false)(onTimeout : T => Unit) = new Attachable {
        var timeout : Option[SetTimeoutHandle] = None
        var oldValue : Option[T] = None

        private def setTimeout(duration : Long, value : T) : Unit = {
            timeout = Some(js.timers.setTimeout(duration) {
                onTimeout(value)
                if(interval) setTimeout(duration, value)
            })
        }

        override def componentWillRender(update : () => Unit) : Unit = {
            val newValue = dependency()
            if(!oldValue.contains(newValue)) {
                for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
                oldValue = Some(newValue)
                val duration = milliseconds(newValue)
                onStart(newValue)
                setTimeout(duration, newValue)
            }
        }

        override def componentWillUnmount() : Unit = {
            for(oldTimeout <- timeout) js.timers.clearTimeout(oldTimeout)
        }
    }
}
