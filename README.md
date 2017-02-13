# React4s
React4s is a Scala library for frontend UI. It wraps Facebook's React library. 
It exposes an API that makes it easy to write plain and simple Scala code for your components. 
You get the indispensable `shouldComponentUpdate()` for free, no callback memoization required.
It uses no macros, no implicits and no complicated types.


# Writing a component

```scala
case class OkCancel(label : P[String]) extends Component[Boolean] {
    override def render() = {
        E.div(H.text(label())),
        E.div(
            E.button(
                H.text("OK"),
                A.onClick(_ => emit(true))
            ),
            E.button(
                H.text("Cancel"),
                A.onClick(_ => emit(false))
            )
        )
    }
}
```

This defines a component `OkCancel` that takes one String "prop" named `label`. 
The `Boolean` in `Component[Boolean]` says that this component emits `Boolean` messages, which is done with the `emit(...)` method.
The `render()` method is what renders your component, 
and the component is rerendered automatically when the props change or the state is updated.
The `E`, `H`, `S` and `A` objects provide methods for building the Virtual DOM.

Emitting messages instead of taking in callbacks via props is a departure from the usual React API, 
and is how you get `shouldComponentUpdate()` for free.
It also clearly separates input (props) from output (callbacks).

You can use a component like this: `H(OkCancel, "Would you like some icecream?", onClick)`. 
The first argument is the components companion object. 
The second argument is the prop argument to the component (you can have multiple props if you like).
The third argument is the function to call when the component emits a message. See the next section for an example.


# Keeping state

```scala
case class Counter() extends Component[Unit] {
    
    val okClicks = State(0)
    val cancelClicks = State(0)
    
    def onClick(ok : Boolean) = {
        if(ok) {
            okClicks.set(okClicks() + 1)
        } else {
            cancelClicks.set(cancelClicks() + 1)
        }
    }
    
    override def render() = {
        H(OkCancel, "Would you like some icecream?", onClick),
        E.hr(),
        E.div(H.text("You've clicked OK " + okClicks() + " times.")),
        E.div(H.text("You've clicked Cancel " + cancelClicks() + " times."))
    }
    
}
```

The `State` type allows the library to detect when you update the state, so it can rerender your component. You can read it with eg. `okClicks()` and update it with eg. `okClicks.set(42)`.


# Styles and CSS

```scala
case class OkCancel(label : P[String]) extends Component[Boolean] {
    override def render() = {
        E.div(H.text(label()), S.color.rgb(0, 0, 255)),
        E.div(
            E.button(
                FancyButtonCss,
                H.text("OK"),
                A.onClick(_ => emit(true))
            ),
            E.button(
                FancyButtonCss,
                H.text("Cancel"),
                A.onClick(_ => emit(false))
            )
        )
    }
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
        val component = H(Counter, H.swallow)
        ReactBridge.renderToDomById(component, "main")
    }
}
```

Just create the component and call `renderToDomById`. In the example, the `Counter` component does not emit any messages, so we just ignore it with `H.swallow`. The `"main"` argument is the ID refering to an existing HTML element, eg. `<div id="main"></div>`.


# Performance

In React, you implement `shouldComponentUpdate()` to avoid rerendering unrelated components when your model is updated. In React4s, this method is already implemented for you. It uses Scala's `!=` operator to check if any props changed, and only updates the component if either the props changed or the state has been updated. That means that for everything that hasn't been reallocated, it just compares the references, and thus doesn't traverse deep into the props.

Beware that what you pass via props must be immutable and have structural equality. You can't pass mutable objects or functions as props, or you will get a stale view or a slow view respectively. However, it's completely safe to pass immutable collections and immutable case classes.


# Lifecycle

![image](https://cloud.githubusercontent.com/assets/78472/22898855/198ae112-f229-11e6-8784-b854dd679f50.png)

This is the complete component lifecycle for React4s. It's simpler than plain React because the React4s model makes the assumption that your props are immutable and have structural equality.

1. When your component is added to the Virtual DOM, the constructor is invoked.
2. Before each render, the componentWillRender() method is called.
3. Then in render(), you'll return the Virual DOM that displays your component.
4. When your component is removed from the Virtual DOM, componentWillUnmount() is called.

Step 1 is a good place to initialize the component. Step 2 is a good place to update state that depends on props. Step 3 should be a pure function of your state and props. Step 4 is a good place to clean up any resources you've allocated.

The component will only be rerendered when your props have changed, as defined by Scala's structural inequality `!=`, or your state has been updated. The state is considered updated when you've called `update()` explicitly or called `.set(...)` or `.modify(...)` on State objects. React4s never looks inside your state to see if it changed.
