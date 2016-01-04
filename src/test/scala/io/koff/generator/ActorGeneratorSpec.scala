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
    val fake = stub[TestService]
    (fake.asyncOperation _).when("test_value").returns(Future.successful("ok"))

    val generated = ActorGenerator.gen(system, fake)
    val asyncResult = generated.asyncOperation("test_value")
    val result = Await.result(asyncResult, 10 seconds)
    result shouldBe "ok"

    (fake.asyncOperation _).verify("test_value")
  }

  "it should throw an exception" in {
    val fake = stub[TestService]
    (fake.asyncOperation _).when(*).throws(new UnsupportedOperationException)

    val generated = ActorGenerator.gen(system, fake)
    val asyncResult = generated.asyncOperation("test_value")
    val result = Await.result(asyncResult, 10 seconds)
    result shouldBe "ok"
    //implement the test
    true shouldBe false
  }

  "it should support unit methods" in {
    //implement the test
    true shouldBe false
  }
}

trait TestService {
  def asyncOperation(value: String): Future[String]
}