package com.github.ahnfelt.react4s

import com.github.ahnfelt.react4s.ReactBridge.{React, ReactElement}

import scala.language.reflectiveCalls
import scala.scalajs.js

/** Used to bridge between the React4s API and the plain React API. This instances uses React etc. form the global namespace. */
object ReactBridge extends ReactBridge(js.Dynamic.global.React, js.Dynamic.global.ReactDOM, js.Dynamic.global.ReactDOMServer, null) {

    /** Represents a plain React element. */
    @js.native
    trait ReactElement extends js.Object {}

    /** Represents the React object. */
    @js.native
    trait React extends js.Object {
        def createElement(tagNameOrClass : js.Any, props : js.Dictionary[js.Any], children : js.Any*) : ReactElement = js.native
    }

}

/**
  Used to bridge between the React4s API and the plain React API.
  Normally you should use the ReactBridge object directly, but
  if you use a JavaScript module system, use the following instead:
<pre>
object ModularReactBridge extends ReactBridge(React, ReactDOM)

&#64;js.native &#64;JSImport("react", JSImport.Namespace)
private object React extends js.Object

&#64;js.native &#64;JSImport("react-dom", JSImport.Namespace)
private object ReactDOM extends js.Object
</pre>
  <p>And then you can do, for example:</p>
<pre>
ModularReactBridge.renderToDomById(component, "main")
</pre>
*/
class ReactBridge(react : => Any, reactDom : => Any = js.undefined, reactDomServer : => Any = js.undefined, addStyle : (String, String) => Unit = null) {

    private lazy val React = react.asInstanceOf[React]
    private lazy val ReactDOM = reactDom.asInstanceOf[js.Dynamic]
    private lazy val ReactDOMServer = reactDomServer.asInstanceOf[js.Dynamic]
    private var isRenderingToString = false

    private def doAddStyle(name : String, css : String) : Unit = if(addStyle != null) addStyle(name, css) else {
        if(js.isUndefined(js.Dynamic.global.document) || js.isUndefined(js.Dynamic.global.document.createElement)) return
        val domStyle = js.Dynamic.global.document.createElement("style")
        domStyle.textContent = "\n" + css
        js.Dynamic.global.document.head.appendChild(domStyle)
    }

    /** Insert the specified element or component inside the DOM element with the given ID. The DOM element must already exist in the DOM. Set the hydrate flag if hydrating server side rendered DOM. */
    def renderToDomById(elementOrComponent : ElementOrComponent, id : String, hydrate : Boolean = false) : Unit = {
        val domElement = js.Dynamic.global.document.getElementById(id)
        renderToDom(elementOrComponent, domElement, hydrate)
    }

    /** Insert the specified element or component inside the given DOM element. The DOM element must already exist in the DOM. Set the hydrate flag if hydrating server side rendered DOM. */
    def renderToDom(elementOrComponent : ElementOrComponent, domElement : js.Any, hydrate : Boolean = false) : Unit = {
        val e = elementOrComponentToReact(elementOrComponent)
        if(hydrate) ReactDOM.hydrate(e, domElement)
        else ReactDOM.render(e, domElement)
    }

    /** Generates static HTML with additional attributes that preserves the HTML if renderToDom is later called on the same element. For server-side use. */
    def renderToString(elementOrComponent : ElementOrComponent) : String = {
        isRenderingToString = true
        try {
            val e = elementOrComponentToReact(elementOrComponent)
            ReactDOMServer.renderToString(e).asInstanceOf[String]
        } finally {
            isRenderingToString = false
        }
    }

    /** Generates plain static HTML without the additional attributes of renderToString. For server-side use. */
    def renderToStaticMarkup(elementOrComponent : ElementOrComponent) : String = {
        val e = elementOrComponentToReact(elementOrComponent)
        ReactDOMServer.renderToStaticMarkup(e).asInstanceOf[String]
    }

    private def insert(tag : JsTag, props : js.Dictionary[js.Any], children : js.Array[js.Any], style : js.Dictionary[js.Any]) : Unit = tag match {

        case element : Element =>
            children.push(elementToReact(element))

        case constructor : ConstructorData[_] =>
            children.push(componentToReact(constructor))

        case dynamic : JsComponentConstructor =>
            children.push(jsComponentToReact(dynamic))

        case Tags(tags) =>
            for(t <- tags) insert(t, props, children, style)

        case Attribute(name, value) =>
            props.update(name, value.asInstanceOf[js.Any])

        case JsProp(name, value) =>
            props.update(name, value)

        case JsPropChildren(name, elements) =>
            val cs = elements.map(nodeToReact)
            props.update(name, if(cs.size == 1) cs.head else js.Array(cs : _*))

        case cssClass : CssClass =>
            if(addCss(cssClass.name)) {
                doAddStyle(cssClass.name, CssChild.cssToString(cssClass, Some(addCss)))
            }
            props.update("className", props.get("className").map(_ + " " + cssClass.name : js.Any).getOrElse(cssClass.name))

        case Style(name, value) =>
            style.update(Style.toReactName(name), value)

        case Text(value) =>
            children.push(value)

        case EventHandler(name, handler) =>
            props.update(name, handler)

    }

    def elementToReact(element : Element) : ReactElement = {

        val props = js.Dictionary[js.Any]()
        val children = js.Array[js.Any]()
        val style = js.Dictionary[js.Any]()

        for(tag <- element.children) insert(tag, props, children, style)

        for(k <- element.key) props.update("key", k)
        for(r <- element.ref) props.update("ref", r)
        if(style.nonEmpty) props.update("style", style)

        React.createElement(element.tagName, props, children : _*)

    }

    private val componentClassMap = js.Dictionary[js.Any]()
    private val cssMap = js.Dictionary[Boolean]()
    private val addCss : String => Boolean = { cssName => val result = !cssMap.contains(cssName); cssMap.update(cssName, true); result }

    def componentToReact(constructorData : ConstructorData[_]) : ReactElement = {
        val props = js.Dictionary[js.Any]()
        props.update("handler", constructorData.handler)
        for(k <- constructorData.key) props.update("key", k)
        for(r <- constructorData.ref) props.update("ref", r)
        for((p, i) <- constructorData.constructor.props.zipWithIndex) {
            props.update("p" + (i + 1), p.asInstanceOf[js.Any])
        }
        val classKey = constructorData.constructor.f.getClass.getName
        val componentClass = componentClassMap.get(classKey).getOrElse {
            val c = createComponentClass(constructorData)
            componentClassMap.update(classKey, c)
            c
        }
        React.createElement(componentClass, props)
    }

    def jsComponentToReact(dynamic : JsComponentConstructor) : ReactElement = {
        val props = js.Dictionary[js.Any]()
        val children = js.Array[js.Any]()
        val style = js.Dictionary[js.Any]()

        for(tag <- dynamic.children) insert(tag, props, children, style)

        for(k <- dynamic.key) props.update("key", k)
        for(r <- dynamic.ref) props.update("ref", r)
        if(style.nonEmpty) props.update("style", style)

        React.createElement(dynamic.componentClass.asInstanceOf[js.Any], props, children : _*)
    }

    def elementOrComponentToReact(elementOrComponent : ElementOrComponent) : ReactElement = {
        elementOrComponent match {
            case element : Element => elementToReact(element)
            case constructor : ConstructorData[_] => componentToReact(constructor)
            case dynamic : JsComponentConstructor => jsComponentToReact(dynamic)
        }
    }

    def nodeToReact(node : Node) : ReactElement = {
        node match {
            case element : ElementOrComponent => elementOrComponentToReact(element)
            case Text(text : String) => text.asInstanceOf[ReactElement]
        }
    }

    def createComponentClass(constructorData : ConstructorData[_]) = {

        // These lines are inspired by the create-react-component implementation

        val react = React.asInstanceOf[js.Dynamic]
        val classConstructor : js.ThisFunction = { (self : js.Dynamic, props : js.Dynamic, context : js.Dynamic) =>
            self.props = props
            self.context = context
            self.refs = {}
            self.state = js.Dynamic.literal("stateUpdates" -> 0.0)
        }
        val dynamicConstructor = classConstructor.asInstanceOf[js.Dynamic]
        dynamicConstructor.prototype = js.Dynamic.newInstance(react.Component)()
        dynamicConstructor.prototype.constructor = dynamicConstructor

        // The following lines bridges between React4s components and React components

        dynamicConstructor.prototype.componentWillMount = { (self : js.Dynamic) =>
            def newP[T](name : String) : P[T] = new P[T] {
                def apply(get : Get) : T = self.props.selectDynamic(name).asInstanceOf[T]
            }
            val instance = constructorData.constructor match {
                case Constructor0(f) => f()
                case Constructor1(f, _) => f(newP("p1"))
                case Constructor2(f, _, _) => f(newP("p1"), newP("p2"))
                case Constructor3(f, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"))
                case Constructor4(f, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"))
                case Constructor5(f, _, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"), newP("p5"))
                case Constructor6(f, _, _, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"), newP("p5"), newP("p6"))
                case Constructor7(f, _, _, _, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"), newP("p5"), newP("p6"), newP("p7"))
                case Constructor8(f, _, _, _, _, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"), newP("p5"), newP("p6"), newP("p7"), newP("p8"))
                case Constructor9(f, _, _, _, _, _, _, _, _, _) => f(newP("p1"), newP("p2"), newP("p3"), newP("p4"), newP("p5"), newP("p6"), newP("p7"), newP("p8"), newP("p9"))
            }
            instance.update = { () =>
                if(!instance.updateScheduled) {
                    instance.updateScheduled = true
                    val stateUpdates = self.state.stateUpdates.asInstanceOf[Double] + 1.0
                    self.setState(js.Dictionary("stateUpdates" -> stateUpdates))
                }
            }
            instance.emit = { message =>
                self.props.handler(message.asInstanceOf[js.Any])
            }
            self.constructor.displayName = instance.getClass.getSimpleName
            self.instance = instance.asInstanceOf[js.Any]
        } : js.ThisFunction

        dynamicConstructor.prototype.componentWillUnmount = { (self : js.Dynamic) =>
            val instance = self.instance.asInstanceOf[Component[_]]
            instance.componentWillUnmount(Get.Unsafe)
            for(attachable <- instance.attachedAttachables) attachable.componentWillUnmount(Get.Unsafe)
        } : js.ThisFunction

        dynamicConstructor.prototype.shouldComponentUpdate = { (self : js.Dynamic, nextProps : js.Dictionary[js.Any], nextState : js.Dictionary[Double]) =>
            self.state.stateUpdates.asInstanceOf[Double] != nextState("stateUpdates") ||
                (1 to constructorData.constructor.props.length).exists(i =>
                    self.props.selectDynamic("p" + i).asInstanceOf[js.Any] != nextProps("p" + i)
                )
        } : js.ThisFunction

        dynamicConstructor.prototype.render = { (self : js.Dynamic) =>
            val instance = self.instance.asInstanceOf[Component[_]]
            if(!isRenderingToString) {
                instance.updateScheduled = true // Suppresses update() calls inside componentWillRender
                instance.componentWillRender(Get.Unsafe)
                for(attachable <- instance.attachedAttachables) attachable.componentWillRender(Get.Unsafe)
            }
            instance.updateScheduled = false
            elementOrComponentToReact(instance.render(Get.Unsafe))
        } : js.ThisFunction

        dynamicConstructor
    }

}
