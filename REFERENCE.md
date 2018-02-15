# React4s

**React4s is a small but complete** Scala library for frontend UI. 
It uses React internally, but has a simpler lifecycle and interface.

The library comes with no macros, no implicits, and no complicated types.

Components emit messages instead of taking in callbacks. You won't be needing Redux.


# Getting started

Add this to your SBT file to depend on React4s:

```sbt
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "com.github.ahnfelt" %%% "react4s" % "0.9.1-SNAPSHOT"
```

Import everything from React4s:
```scala
import com.github.ahnfelt.react4s._
```

Write your first component:

```scala
case class TodoList(label : P[String]) extends Component[NoEmit] {

    val todos = State(List("Learn React4s"))
    val todoText = State("")

    def addTodo(get : Get) : Unit = {
        todos.modify(get(todoText) :: _)
        todoText.set("")
    }

    override def render(get : Get) = E.div(
        E.div(Text(get(label)),
            E.div(
                E.input(
                    A.value(get(todoText)),
                    A.onChangeText(todoText.set)
                ),
                E.button(
                    Text("Add"),
                    A.onLeftClick { _ => addTodo(get) }
                )
            ),
            E.ul(
                Tags(
                    for(todo <- get(todos))
                        yield E.li(Text(todo))
                )
            )
        )
    )

}
```

Render it to a DOM element of your choosing:

```html
<div id="main"></div>
```

```scala
object Main extends js.JSApp {
    def main() : Unit = {
        val component = Component(TodoList, "What would you like to do?")
        ReactBridge.renderToDomById(component, "main")
    }
}
```

Alternatively, start from the Spotify search example application and modify it to your needs:

https://github.com/Ahnfelt/react4s-example


# Components, props & state


# HTML elements and attributes

| Syntax | HTML equivalent |
----------------------------
| `E("div", ...)` | `<div>...</div>` |
| `E.div(...)` | `<div>...</div>` |
| `E.div(A("data", "hello"))` | `<div data="hello"></div>` |
| `E.input(A.onClick(...))` | `<div onclick="..."></div>` |
| `E.input(A.onClick(...), Text("next"))` | `<div onclick="...">next</div>` |


# CSS styles and classes

Inline styles use the `S.` prefix:

```scala
case class OkCancel(label : P[String]) extends Component[Boolean] {
    override def render(get : Get) = E.div(
        E.div(Text(get(label)), S.color.rgb(0, 0, 255)),
        E.div(
            E.button(
                FancyButtonCss,
                Text("OK"),
                A.onClick(_ => emit(true))
            ),
            E.button(
                FancyButtonCss,
                Text("Cancel"),
                A.onClick(_ => emit(false))
            )
        )
    )
}
```

The above uses one inline style `S.color.rgb(0, 0, 255)` and one css class `FancyButtonCss`. The css class is defined as follows:

```scala
object FancyButtonCss extends CssClass(
    S.cursor.pointer(),
    S.border.px(2).solid().rgb(0, 0, 0),
    S.color.rgb(0, 0, 0),
    S.backgroundColor.rgb(255, 255, 255),
    Css.hover(
        S.color.rgb(255, 255, 255),
        S.backgroundColor.rgb(0, 0, 0)
    )
)
```

It styles a button to be white with a black border, and black with white text when the mouse is hovered over it. 
The resulting `<style>...</style>` will be added to the DOM the first time `FancyButtonCss` is used to render a component.

| Syntax | HTML equivalent |
----------------------------
| `S("color", "black")` | `style="color: black"` |
| `S.color("black")` | `style="color: black"` |
| `S.color.rgba(255, 0, 0, 0.5)` | `style="color: rgba(255, 0, 0, 0.5)"` |
| `FancyButtonCss` | `class="FancyButtonCss-c4f3b4be"` |


# Lifecycle

![image](https://cloud.githubusercontent.com/assets/78472/22898855/198ae112-f229-11e6-8784-b854dd679f50.png)

This is the complete component lifecycle for React4s. It's simpler than plain React because the React4s model makes the assumption that your props are immutable and have structural equality.

1. When your component is added to the Virtual DOM, the constructor is invoked.
2. Before each render, the componentWillRender() method is called. Here you can update any state that depends on props that have changed.
3. Then in render(), you'll return the Virual DOM that displays your component. State updates are not allowed during this call.
4. When your component is removed from the Virtual DOM, componentWillUnmount() is called.

The component will only be rerendered when your props have changed, as defined by Scala's structural inequality `!=`, or your state has been updated. The state is considered updated when you've called `update()` explicitly or called `.set(...)` or `.modify(...)` on State objects. React4s never looks inside your state to see if it changed.

```scala
trait Component[M] {
    def componentWillRender(get : Get) : Unit = {}
    def componentWillUnmount(get : Get) : Unit = {}
    def render(get : Get) : ElementOrComponent
}
```


# Attachables

Whenever you start a long-running task such as an AJAX request, a timeout, or similar, you have to be careful to handle 
errors, display a loading animation, listen for changes, avoid race conditions and clean up any resources when the 
component is unmounted.

Attachables is a way to make this logic reusable across components in a declarative manner. 
It works by attaching itself to the lifecycle events of the component that uses it.

```scala
trait Attachable {
    def componentWillRender(get : Get) : Unit = {}
    def componentWillUnmount(get : Get) : Unit = {}
}
```

Two of the attachables that come with React4s are `Debounce` and `Loader`. 
Here's an example from the Spotify example application that uses both to implement "instant search".

```scala
case class MainComponent() extends Component[NoEmit] {

    val query = State("")
    val debouncedQuery = Debounce(this, query, 500)

    val artists = Loader(this, debouncedQuery) { q =>
        if(q.trim.isEmpty) Future.successful(List()) else {
            val query = js.URIUtils.encodeURIComponent(q)
            Ajax.get("http://show.ahnfelt.net/react4s-spotify/?q=" + query + "&type=artist").
                map { ajax =>
                    js.JSON.parse(ajax.responseText).
                        artists.items.
                        asInstanceOf[js.Array[js.Dynamic]].
                        toList.map(Artist.fromDynamic)
                }
        }
    }

    override def render(get : Get) : Element = {
        E.div(
            E.div(
                Component(SearchInputComponent, get(query)).withHandler(q => query.set(q))
            ),
            E.div(
                Component(LoadingComponent).when(get(artists.loading)),
                Tags(get(artists.error).map(e => Component(ErrorComponent, e.getMessage))),
                Tags(get(artists).map(results => Component(ResultsComponent, results)))
            )
        )
    }

}
```

The `Debounce(this, query, 500)` creates an attachable, which we store in `debouncedQuery`.
Calling `get(decouncedQuery)` will then return the same as `get(query)`, except delayed until you've paused writing for 500 ms. 

trait Debounce[T] {
    def apply(get : Get) : T
}

The `Loader(this, debouncedQuery) { ... }` starts a new Future every time `debouncedQuery` changes.
In this case, the future represents an AJAX request, but it could be any other future.
The loader ensures that only one such future is running at a time per Loader instance, which eliminates race conditions 
if your future has side effects on the server, since it prevents eg. two AJAX requests from crossing each other on the timeline.

The value returned from `Loader(this, debouncedQuery) { ... }` is of type `Loader[T]`. 

```scala
trait Loader[T] {
    def apply(get : Get) : Option[T]
    def error(get : Get) : Option[Throwable]
    def loading(get : Get) : Boolean
    def retry() : Unit
}
```

So `get(artists)` is Some(...) if the future was successful, `get(artists.loading)` is true while the future is running,
and `get(artists.error)` is `Some(...)` if the most recent future failed. That way, it's easy to show the appropriate
success, loading and error states in the render method.

The `retry()` method forces the loader to start a new future.


# Example application

React4s doesn't mandate any specific project structure, but if you'd like to start from something, 
check out the [Example Application](https://github.com/Ahnfelt/react4s-example).
