package dk.ahnfelt.react4s

import scala.concurrent.Future
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

        override def apply() : Option[O] = lastValue
        override def error() : Option[Throwable] = lastError
        override def loading() : Boolean = isLoading
    }
}

