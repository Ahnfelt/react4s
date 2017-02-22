package dk.ahnfelt.react4s

import scala.scalajs.js

/** Used to bridge between the React4s API and the plain React API. */
object ReactBridge {

    /** Insert the specified element or component inside the DOM element with the given ID. The DOM element must already exist in the DOM. */
    def renderToDomById(elementOrComponent : ElementOrComponent, id : String) : Unit = {
        val domElement = js.Dynamic.global.document.getElementById(id)
        renderToDom(elementOrComponent, domElement)
    }

    /** Insert the specified element or component inside the given DOM element. The DOM element must already exist in the DOM. */
    def renderToDom(elementOrComponent : ElementOrComponent, domElement : js.Any) : Unit = {
        val e = elementOrComponentToReact(elementOrComponent)
        js.Dynamic.global.ReactDOM.render(e, domElement)
    }

    private var addStyle = { (name : String, css : String) =>
        val domStyle = js.Dynamic.global.document.createElement("style")
        domStyle.textContent = "\n" + css
        js.Dynamic.global.document.head.appendChild(domStyle)
    }

    def elementToReact(element : Element) : ReactElement = {

        val props = js.Dictionary[js.Any]()
        val children = js.Array[js.Any]()
        val style = js.Dictionary[js.Any]()

        def insert(tag : Tag) : Unit = tag match {

            case element : Element =>
                children.push(elementToReact(element))

            case constructor : ConstructorData[_] =>
                children.push(componentToReact(constructor))

            case TagList(tags) =>
                for(t <- tags) insert(t)

            case Attributes(name, value, next) =>
                props.update(name, value)

            case cssClass : CssClass =>
                if(!cssClass.emitted) {
                    cssClass.emitted = true
                    addStyle(cssClass.name, CssChild.cssToString(cssClass))
                }
                props.update("className", props.get("className").map(_ + " " + cssClass.name : js.Any).getOrElse(cssClass.name))

            case Style(name, value) =>
                style.update(name, value)

            case Text(value) =>
                children.push(value)

            case EventHandler(name, handler) =>
                props.update(name, handler)

        }

        for(tag <- element.children) insert(tag)

        for(k <- element.key) props.update("key", k)
        for(r <- element.ref) props.update("ref", r)
        if(style.nonEmpty) props.update("style", style)
        if(children.nonEmpty) props.update("children", children)

        React.createElement(element.tagName, props)

    }

    private val componentClassMap = js.Dictionary[js.Any]()

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

    def elementOrComponentToReact(elementOrComponent : ElementOrComponent) : ReactElement = {
        elementOrComponent match {
            case element : Element => elementToReact(element)
            case constructor : ConstructorData[_] => componentToReact(constructor)
        }
    }

    def createComponentClass(constructorData : ConstructorData[_]) = {
        React.createClass(js.Dictionary(
            "getInitialState" -> { () =>
                js.Dictionary("stateUpdates" -> 0.0)
            },
            "componentWillMount" -> ({ (self : js.Dynamic) =>
                def newP[T](name : String) : P[T] = new P[T] {
                    def apply() : T = self.props.selectDynamic(name).asInstanceOf[T]
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
            } : js.ThisFunction),
            "componentWillUnmount" -> ({ (self : js.Dynamic) =>
                val instance = self.instance.asInstanceOf[Component[_]]
                instance.componentWillUnmount()
                for(attachable <- instance.attachedAttachables) attachable.componentWillUnmount()
            } : js.ThisFunction),
            "shouldComponentUpdate" -> ({ (self : js.Dynamic, nextProps : js.Dictionary[js.Any], nextState : js.Dictionary[Double]) =>
                self.state.stateUpdates.asInstanceOf[Double] != nextState("stateUpdates") ||
                    (1 to constructorData.constructor.props.length).exists(i =>
                        self.props.selectDynamic("p" + i).asInstanceOf[js.Any] != nextProps("p" + i)
                    )
            } : js.ThisFunction),
            "render" -> ({ (self : js.Dynamic) =>
                val instance = self.instance.asInstanceOf[Component[_]]
                instance.updateScheduled = true // Suppresses update() calls inside componentWillUpdate
                instance.componentWillRender()
                for(attachable <- instance.attachedAttachables) attachable.componentWillRender()
                instance.updateScheduled = false
                elementToReact(instance.render())
            } : js.ThisFunction)
        ))
    }

}

/** Represents a plain React element. */
@js.native
trait ReactElement extends js.Object {}

@js.native
private object React extends js.Object {
    def createElement(tagNameOrClass : js.Any, props : js.Dictionary[js.Any]) : ReactElement = js.native
    def createClass(methods : js.Dictionary[js.Any]) : js.Any = js.native
}
