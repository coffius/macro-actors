package io.koff.generator

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ActorGeneratorSpec
  extends TestKit(ActorSystem("TestActorSystem"))
  with FreeSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockFactory
{
  override def afterAll(): Unit = shutdown()

  "it should generate intermediate classes and exec a trait method" in {
    val fake = stub[AsyncTestService]
    (fake.asyncOperation _).when("test_value").returns(Future.successful("ok"))

    val generated = ActorGenerator.gen(system, fake)
    val asyncResult = generated.asyncOperation("test_value")
    val result = Await.result(asyncResult, 10 seconds)
    result shouldBe "ok"

    (fake.asyncOperation _).verify("test_value")
  }

  "it should throw an exception" in {
    val fake = stub[AsyncTestService]
    (fake.asyncOperation _).when(*).throws(new TestException)

    val generated = ActorGenerator.gen(system, fake)
    val asyncResult = generated.asyncOperation("test_value")
    intercept[TestException] {
      Await.result(asyncResult, 10 seconds)
    }
  }

  "it should support only scala.Future[_] methods" in {
    val fake = mock[TestService]
    intercept[IllegalArgumentException] {
      ActorGenerator.gen(system, fake)
    }
  }
}

class TestException extends Exception

trait AsyncTestService {
  def asyncOperation(value: String): Future[String]
}

trait TestService {
  def operation(value: String): Option[String]
}