# React4s
React4s is a Scala library for frontend UI. It wraps Facebook's React library. 
It exposes an API that makes it easy to write plain and simple Scala code for your components. 
You get the indispensable `shouldComponentUpdate()` for free, no callback memoization required.
It uses no macros, no implicits and no complicated types.

[Example Application](https://github.com/Ahnfelt/react4s-example) - [Online Demo](http://ting.ahnfelt.dk/react4s/spotify/)

```sbt
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "com.github.ahnfelt" %%% "react4s" % "0.7.3-SNAPSHOT"
```

# Writing a component

```scala
case class OkCancel(label : P[String]) extends Component[Boolean] {
    override def render() = E.div(
        E.div(Text(label())),
        E.div(
            E.button(
                Text("OK"),
                A.onClick(_ => emit(true))
            ),
            E.button(
                Text("Cancel"),
                A.onClick(_ => emit(false))
            )
        )
    )
}
```

This defines a component `OkCancel` that takes one String "prop" named `label`. 
The `Boolean` in `Component[Boolean]` says that this component emits `Boolean` messages, which is done with the `emit(...)` method.
The `render()` method is what renders your component, 
and the component is rerendered automatically when the props change or the state is updated.
The `E`, `A` and `S` objects provide methods for building the Virtual DOM.

Emitting messages instead of taking in callbacks via props is a departure from the usual React API, 
and is how you get `shouldComponentUpdate()` for free.
It also clearly separates input (props) from output (callbacks).

You can use a component like this: `Component(OkCancel, "Would you like some icecream?")`. 
The first argument is the components companion object. 
The remaining arguments are the props for the component.


# Keeping state

```scala
case class Counter() extends Component[NoEmit] {
    
    val okClicks = State(0)
    val cancelClicks = State(0)
    
    def onClick(ok : Boolean) = {
        if(ok) {
            okClicks.modify(_ + 1)
        } else {
            cancelClicks.modify(_ + 1)
        }
    }
    
    override def render() = E.div(
        Component(OkCancel, "Would you like some icecream?").withHandler(onClick),
        E.hr(),
        E.div(Text("You've clicked OK " + okClicks() + " times.")),
        E.div(Text("You've clicked Cancel " + cancelClicks() + " times."))
    )
    
}
```

The `State` type allows the library to detect when you update the state, so it can rerender your component. You can read it with eg. `okClicks()` and update it with eg. `okClicks.set(42)` or `okClicks.modify(_ + 1)`.


# Styles and CSS

```scala
case class OkCancel(label : P[String]) extends Component[Boolean] {
    override def render() = E.div(
        E.div(Text(label()), S.color.rgb(0, 0, 255)),
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

It styles a button to be white with a black border, and black with white text when the mouse is hovered over it. The resulting `<style>...</style>` will be added to the DOM the first time `FancyButtonCss` is used to render a component.


# Binding it to the DOM

```scala
object Main extends js.JSApp {
    def main() : Unit = {
        val component = Component(Counter)
        ReactBridge.renderToDomById(component, "main")
    }
}
```

Just create the component and call `renderToDomById`. The `"main"` argument is the ID refering to an existing HTML element, eg. `<div id="main"></div>`.


# Performance

In React, you implement `shouldComponentUpdate()` to avoid rerendering unrelated components when your model is updated. In React4s, this method is already implemented for you. It uses Scala's `!=` operator to check if any props changed, and only updates the component if either the props changed or the state has been updated. That means that for everything that hasn't been reallocated, it just compares the references, and thus doesn't traverse deep into the props.

Beware that what you pass via props must be immutable and have structural equality. You can't pass mutable objects or functions as props, or you will get a stale view or a slow view respectively. However, it's completely safe to pass immutable collections and immutable case classes.


# Lifecycle

![image](https://cloud.githubusercontent.com/assets/78472/22898855/198ae112-f229-11e6-8784-b854dd679f50.png)

This is the complete component lifecycle for React4s. It's simpler than plain React because the React4s model makes the assumption that your props are immutable and have structural equality.

1. When your component is added to the Virtual DOM, the constructor is invoked.
2. Before each render, the componentWillRender() method is called. Here you can update any state that depends on props that have changed.
3. Then in render(), you'll return the Virual DOM that displays your component. State updates are not allowed during this call.
4. When your component is removed from the Virtual DOM, componentWillUnmount() is called.

The component will only be rerendered when your props have changed, as defined by Scala's structural inequality `!=`, or your state has been updated. The state is considered updated when you've called `update()` explicitly or called `.set(...)` or `.modify(...)` on State objects. React4s never looks inside your state to see if it changed.

You can attach Attachables that listen on these lifecycle events, and React4s comes with three of those: `Timeout`, `Debounce` and `Loader`. See how they're used in https://github.com/Ahnfelt/react4s-example.
