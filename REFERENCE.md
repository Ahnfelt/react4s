# React4s

**React4s is a small but complete** Scala library for frontend UI. 
It uses React internally, but has a simpler lifecycle and a simpler interface.

This library comes with no macros, no implicits, and no complicated types.

You also won't be needing Redux.


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
case class TodoList(label : P[String]) extends Component[Boolean] {

    val todos = State(List("Learn React4s"))
    val todoText = State("")
    
    def addTodo() = {
        todos.modify(todoText :: _)
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
                A.onLeftClick { _ => addTodo() }
            )
        ),
        E.ul(
            Tags(
                for(todo <- get(todos)) 
                    yield E.li(Text(todo))
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
        val component = Component(TodoList, "What would you like to do today?")
        ReactBridge.renderToDomById(component, "main")
    }
}
```

Alternatively, start from the Spotify search example application and modify it to your needs:

https://github.com/Ahnfelt/react4s-example
