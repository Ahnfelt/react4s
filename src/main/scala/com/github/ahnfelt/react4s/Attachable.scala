package com.github.ahnfelt.react4s

import com.github.ahnfelt.react4s.Loader.Loaded

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

trait AddEventListener[T] extends Signal[T]

object AddEventListener {
    trait AttachableAddEventListener[T] extends AddEventListener[T] with Attachable

    /** Listens for an addEventListener event. Fires handler(value, None) initially and when the dependency changes, and handler(value, Some(event)) when the event occurs. */
    def apply[I, O](component : Component[_], target : js.Any, eventName : String, dependency : Signal[I])(handler : (I, Option[js.Dynamic]) => O) : AddEventListener[O] = component.attach(new AttachableAddEventListener[O] {

        var listener : js.Function1[Any, Unit] = _
        var result : O = _
        var lastDependency : Option[I] = None

        override def componentWillRender(get : Get) : Unit = {
            val newDependency = get(dependency)
            if(!lastDependency.contains(newDependency)) {
                lastDependency = Some(newDependency)
                result = handler(newDependency, None)
                component.update()
            }
            if(listener == null) {
                listener = { e : Any =>
                    result = handler(get(dependency), Some(e.asInstanceOf[js.Dynamic]))
                    component.update()
                }
                target.asInstanceOf[js.Dynamic].addEventListener(eventName, listener)
            }
        }

        override def componentWillUnmount(get : Get) : Unit = {
            if(listener != null) {
                target.asInstanceOf[js.Dynamic].removeEventListener(eventName, listener)
                listener = null
            }
        }

        override def sample(get : Get) = result

    })

    /** A version with no signal dependency that returns None until the first event happens. */
    def apply[I, O](component : Component[_], target : js.Any, eventName : String)(handler : js.Dynamic => O) : AddEventListener[Option[O]] =
        apply(component, target, eventName, _ => {}) { case (_, None) => None; case (_, Some(e)) => Some(handler(e)) }

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
trait Loader[T] extends Signal[Loaded[T]] {
    def sample(get : Get) : Loaded[T]
    /** True until the most recent future completes, then false. */
    val loading : Signal[Boolean]
    /** The last error, if any. Cleared when the most recent future succeeds. */
    val error : Signal[Option[Throwable]]
    /** The last loaded value, if any. Not cleared when the most recent future fails. */
    val result : Signal[Option[T]]
    /** Forces the loader to restart the future. */
    def retry() : Unit
}

/** Used to create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. To avoid race conditions, it waits for the old future to complete before starting a new. If initial is Some(initialValue), delays first load until dependency() != initialValue(). */
object Loader {

    /** The status of a loader: Loading if the current future is running. Error if the current future has failed. Result otherwise. */
    sealed abstract class Loaded[+T] {
        def zip[T1](that1 : Loaded[T1]) : Loaded[(T, T1)] = (this, that1) match {
            case (Loading(), _) => Loading()
            case (_, Loading()) => Loading()
            case (Error(throwable), _) => Error(throwable)
            case (_, Error(throwable)) => Error(throwable)
            case (Result(v1), Result(v2)) => Result((v1, v2))
        }
    }
    case class Loading[+T]() extends Loaded[T]
    case class Error[+T](throwable : Throwable) extends Loaded[T]
    case class Result[+T](value : T) extends Loaded[T]

    trait AttachableLoader[T] extends Loader[T] with Attachable

    /** Create a Loader. Whenever the dependency (eg. a prop) changes, a new future is created and the old future (if any) is ignored. To avoid race conditions, it waits for the old future to complete before starting a new. If initial is Some(initialValue), delays first load until dependency() != initialValue(). */
    def apply[I, O](component : Component[_], dependency : Signal[I], initial : Option[Signal[I]] = None)(future : I => Future[O]) : Loader[O] = component.attach(new AttachableLoader[O] {
        var lastDependency : Option[I] = None
        var nextDependency : Option[I] = None
        var lastValue : Option[O] = None
        var lastError : Option[Throwable] = None
        var isLoading : Boolean = false
        var lastVersion : Long = 0
        var lastRetries : Long = 0
        var retries : Long  = 0
        var changedSinceInitial : Boolean = false
        var unmounted : Boolean = false

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
                    if(!unmounted) {
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
        }

        override def componentWillUnmount(get : Get) : Unit = unmounted = true

        override def retry() : Unit = { retries += 1; component.update() }
        val loading : Signal[Boolean] = Signal.of(_ => isLoading)
        val error : Signal[Option[Throwable]] = Signal.of(_ => lastError)
        val result : Signal[Option[O]] = Signal.of(_ => lastValue)
        override def sample(get : Get) : Loaded[O] =
            if(isLoading) Loading[O]()
            else lastError.map(Error[O]).getOrElse(lastValue.map(Result[O]).getOrElse(Loading[O]()))
    })
}

/** Set after a specified timeout, or on an interval. */
trait Timeout extends Signal[Boolean] {
    /** Has the timeout triggered yet? */
    def sample(get : Get) : Boolean
    /** The number of times the timeout has triggered (only ever higher than 1 for intervals). */
    def ticks(get : Get) : Long
    /** The number of milliseconds since the timeout was started. */
    def elapsed(get : Get) : Long
}

/** Used to run a function after a specified timeout, or on an interval. */
object Timeout {

    trait AttachableTimeout extends Timeout with Attachable

    /** Sets a timeout that restarts every time the dependency changes. If interval is set, it triggers every interval instead of just once. */
    def apply[T](component: Component[_], dependency : Signal[T], interval : Boolean = false)(milliseconds : T => Long) : Timeout = component.attach(new AttachableTimeout {
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

        override def sample(get : Get) = triggered > 0
        override def ticks(get : Get) = triggered
        override def elapsed(get : Get) = (System.nanoTime() - startTime) / (1000 * 1000)
    })
}

/** Used to debounce or throttle changes. */
trait Debounce[T] extends Signal[T] {
    /** Get the debounced value. */
    def sample(get : Get) : T
}

object Debounce {

    trait AttachableDebounce[T] extends Debounce[T] with Attachable

    /** When the dependency is changed, don't propagate the value immediately - instead wait until no change has been made for the specified milliseconds. If immediate is set, propagate the first change after a pause immediately. */
    def apply[T](component : Component[_], dependency : Signal[T], milliseconds : Long = 250, immediate : Boolean = false) : Debounce[T] = component.attach(new AttachableDebounce[T] {
        private var timeout : Option[SetTimeoutHandle] = None
        private var oldValue : T = Get.Unsafe(dependency)

        override def sample(get : Get) = oldValue

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
