# macro-actors
Replacement of TypedActors.

The current state of this lib is a proof of concept. 
 
Now it can generate an intermediate code by a trait and its implementation in order to make middleman-actors.

**Why?**

The main reason why this library was created is the same as it was at TypedActors - to make communication between actor systems and non-actor code simpler.
Another reason is to unleash the power of akka for using in non-actor systems :)

**How it works**

Let's say that you have such an service interface:

```scala
trait SimpleService {
  def hello(name: String): Future[String]
  def goodBye(name: String): Future[String]
}
```
and you want to work with a distributed implementation of this interface transparently.
For this case you can use Akka Actors. But if you want to make it transparent for other code you must create several intermediate classes using actors.

```scala
//Define proxy sender - it sends messages to the `actorRef`
class SimpleServiceActorImpl(private val actorRef: ActorRef) extends SimpleService {
   private implicit val timeout = Timeout(5 seconds)
   override def hello(name: String): scala.concurrent.Future[String] = {
     actorRef.?(helloMsg(name)).mapTo[String]
   }
   override def goodBye(name: String): scala.concurrent.Future[String] = {
     actorRef.?(goodByeMsg(name)).mapTo[String]
   }
}

//Define ProxyActor - it receives messages and execute corresponding methods of implementation
case class helloMsg(name: String)
case class goodByeMsg(name: String)

class SimpleServiceProxyActor(private val internalImpl: SimpleService) extends Actor {
  import context.dispatcher
  def receive = {
     case helloMsg(name) => tryIt(internalImpl.hello(name)).pipeTo(sender())
     case goodByeMsg(name) => tryIt(internalImpl.goodBye(name)).pipeTo(sender())
  }
}
```
And you have to do it for each service interface that you have.

But hopefully this lib can do it for you :) It creates the needed actor and the needed sender by a trait of service interface.

```scala
val system = ActorSystem("akka-system")
val impl = new SimpleServiceImpl
val service = ActorGenerator.gen(system, impl) // generate an intermediate code
val result = Await.result(service.hello("scala"), 10 seconds)
println(s"result: $result")
```

**How exactly it works**

Right now the implementation is very simple.

1. At runtime `ActorGenerator.gen[TraitType](...)` gets the list of methods from `TraitType` using scala reflection
1. This list is used to generate a string with the scala code.
1. Then this string is compiled by the scala compiler and outcome classes are loaded to JVM.
1. Then objects of these classes are instantiated and connected together
1. And the proxy sender object returns as a result of `ActorGenerator.gen[TraitType](...)`
Though this implementation can be replaced in the future(for macros for example), the main idea will remain the same.