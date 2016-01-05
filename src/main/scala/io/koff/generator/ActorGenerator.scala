package io.koff.generator

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Future
import scala.reflect.runtime.{currentMirror => cm, universe => ru}

object ActorGenerator {
  case class MethodParamDesc(paramType: ru.Type, paramName: ru.TermName)
  case class MethodDesc(methodSymbol: ru.MethodSymbol, params : List[List[MethodParamDesc]], returnType: ru.Type)

  private val compiler = new RuntimeCompiler()
  private val futureType = ru.typeOf[Future[_]].typeSymbol

  /**
   * Generate an actor implementation of a trait `Trait`
   * @param actorSystem actor system for internal actor
   */
  def gen[Trait: ru.TypeTag](actorSystem: ActorSystem, impl: Trait): Trait = {
    val traitType = ru.typeOf[Trait]

    val traitTypeSymbol = traitType.typeSymbol
    //get methods from a trait without a constructor
    val methods = traitType.decls.filter(_.isMethod).map(_.asMethod).filter(!_.isConstructor).toSeq
    //translate information about trait methods in internal structure
    val withParamsAndReturn = methods.map{ methodSymb =>
      val params = methodSymb.paramLists.map(_.map(_.asTerm).map(termToNameAndType))
      val returnType = methodSymb.returnType
      MethodDesc(methodSymb, params, returnType)
    }
    println("decls: " + traitTypeSymbol)
    println("with params : " + withParamsAndReturn)

    //generate a code string
    val codeString = generateCode(traitTypeSymbol, withParamsAndReturn)
    println(
      s"""
        |------------------------------------------
        |$codeString
        |------------------------------------------
      """.stripMargin
    )
    val actorClassName    = actorFullName(traitTypeSymbol)
    val implClassName     = implFullName(traitTypeSymbol)
    val resultActorClass  = compiler.compile(actorClassName, codeString)
    val proxyClass        = compiler.compile(implClassName, codeString)
    val actorRef = actorSystem.actorOf(Props(resultActorClass, impl))
    val proxy = proxyClass.getDeclaredConstructors.head.newInstance(actorRef).asInstanceOf[Trait]
    proxy
  }

  private def termToNameAndType(term: ru.TermSymbol): MethodParamDesc = {
    val name = term.name
    val termType = term.typeSignature
    MethodParamDesc(termType, name)
  }

  private def generateMethodParams(params: List[List[MethodParamDesc]]): String = {
    params.map{ paramList =>
      val paramStr = paramList.map(_.paramName).mkString(", ")
      s"($paramStr)"
    }.mkString("")
  }

  private def generateMethodParamsWithType(params: List[List[MethodParamDesc]]): String = {
    params.map{ paramList =>
      val paramStr = paramList.map{
        paramDesc => s"${paramDesc.paramName}: ${paramDesc.paramType}"
      }.mkString(", ")

      s"($paramStr)"
    }.mkString("")
  }

  def generateMessagesReceive(methodDescs: Seq[MethodDesc]): String = {
    methodDescs.map { methodDesc =>
      val methodName = methodDesc.methodSymbol.name
      val msgClassName = messageClassName(methodDesc.methodSymbol)
      val methodParams = generateMethodParams(methodDesc.params)
      s"""|     case $msgClassName$methodParams => tryIt(internalImpl.$methodName$methodParams).pipeTo(sender())""".stripMargin
    }.mkString("\n")
  }

  def generateActorInternals(methodDescs: Seq[MethodDesc]): String = {
    val msgs = generateMessagesReceive(methodDescs)
    s"""
     |  import context.dispatcher
     |  
     |  def receive = {
         |$msgs
     |  }
     """.stripMargin
  }

  private def generateActorImports(traitTypeSymbol: ru.Symbol): String = {
    s"""import akka.actor.Actor
       |import ${traitTypeSymbol.fullName}
       |import akka.pattern.pipe
       |import io.koff.generator.utils.ActorUtils._
     """.stripMargin
  }

  private def messageClassName(methodSymbol: ru.MethodSymbol): String = {
    methodSymbol.name.toString + "Msg"
  }

  private def generateActorMessageClasses(methodDescs: Seq[MethodDesc]) = {
    methodDescs.map { methodDesc =>
      val msgClassName = messageClassName(methodDesc.methodSymbol)
      val methodParams = generateMethodParamsWithType(methodDesc.params)
      s"case class $msgClassName$methodParams"
    }.mkString("\n")
  }

  private def generateActorCode(traitTypeSymbol: ru.Symbol, methodDescs: Seq[MethodDesc]): String = {
    val traitName = traitTypeSymbol.name.toString
    val internals = generateActorInternals(methodDescs)
    val imports = generateActorImports(traitTypeSymbol)
    val pkgName = packageName(traitTypeSymbol)
    val msgClasses = generateActorMessageClasses(methodDescs)
    s"""
    |$imports
    |
    |//Actor's message classes
    |$msgClasses
    |//Generated Actor
    |class ${actorSimpleName(traitName)}(private val internalImpl: $traitName) extends Actor {
    |  $internals
    |}
    """.stripMargin
  }

  private def generateMethods(methodDescs: Seq[MethodDesc]): String = {
    methodDescs.map { methodDesc =>
      val methodName = methodDesc.methodSymbol.name
      val msgClassName = messageClassName(methodDesc.methodSymbol)
      val methodParams = generateMethodParams(methodDesc.params)
      val paramsWithTypes = generateMethodParamsWithType(methodDesc.params)
      val fullReturnType = methodDesc.methodSymbol.returnType
      require(fullReturnType.typeSymbol == futureType, s"only scala.Future[_] is supported: ${fullReturnType.typeSymbol}")
      val ru.TypeRef(_, _, typeParams) = fullReturnType
      val typeParam = typeParams.head

      s"""   override def $methodName$paramsWithTypes: $fullReturnType = {
         |     actorRef.?($msgClassName$methodParams).mapTo[$typeParam]
         |   }""".stripMargin
    }.mkString("\n")
  }

  private def generateImplInternals(withParamsAndReturn: Seq[MethodDesc]): String = {
    val methods = generateMethods(withParamsAndReturn)
    s"""
       |   private implicit val timeout = Timeout(5 seconds)
       |
          |$methods
     """.stripMargin
  }

  private def generateImplImports(traitTypeSymbol: ru.Symbol): String = {
    s"""import akka.actor.ActorRef
       |import akka.util.Timeout
       |import akka.pattern.ask
       |import scala.concurrent.Future
       |import scala.concurrent.duration._
       |import scala.language.postfixOps
       |
       |import ${traitTypeSymbol.fullName}""".stripMargin
  }

  private def actorSimpleName(traitName: String): String = traitName + "ProxyActor"
  private def actorFullName(traitTypeSymbol: ru.Symbol): String = {
    val pkgName = packageName(traitTypeSymbol)
    val simpleName = actorSimpleName(traitTypeSymbol.name.toString)
    s"$pkgName.$simpleName"
  }

  private def implSimpleName(traitName: String): String = traitName + "ActorImpl"
  private def implFullName(traitTypeSymbol: ru.Symbol): String = {
    val pkgName = packageName(traitTypeSymbol)
    val simpleName = implSimpleName(traitTypeSymbol.name.toString)
    s"$pkgName.$simpleName"
  }

  def generateImplCode(traitTypeSymbol: ru.Symbol, withParamsAndReturn: Seq[MethodDesc]): String = {
    val traitName = traitTypeSymbol.name.toString
    val internals = generateImplInternals(withParamsAndReturn)
    val imports = generateImplImports(traitTypeSymbol)
    s"""
       |$imports
       |
       |class ${implSimpleName(traitName)}(private val actorRef: ActorRef) extends $traitName {
          |$internals
       |}
     """.stripMargin
  }

  def generateCode(traitTypeSymbol: ru.Symbol, withParamsAndReturn: Seq[MethodDesc]): String = {
    val actorCode = generateActorCode(traitTypeSymbol, withParamsAndReturn)
    val implCode  = generateImplCode (traitTypeSymbol, withParamsAndReturn)
    val pkgName = packageName(traitTypeSymbol)
    s"""
       |package $pkgName
       |
       |$actorCode
       |
       |$implCode
     """.stripMargin
  }

  private def packageName(sym: ru.Symbol): String = {
    def enclosingPackage(sym: ru.Symbol): ru.Symbol = {
      if (sym == ru.NoSymbol) ru.NoSymbol
      else if (sym.isPackage) sym
      else enclosingPackage(sym.owner)
    }
    val pkg = enclosingPackage(sym)
    if (pkg == cm.EmptyPackageClass) ""
    else pkg.fullName
  }
}
