package com.softwaremill.sttp.asynchttpclient.future

import java.nio.ByteBuffer

import com.softwaremill.sttp.asynchttpclient.AsyncHttpClientBackend
import com.softwaremill.sttp.{FollowRedirectsBackend, FutureMonad, SttpBackend}
import org.asynchttpclient.{
  AsyncHttpClient,
  AsyncHttpClientConfig,
  DefaultAsyncHttpClient
}
import org.reactivestreams.Publisher

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class AsyncHttpClientFutureBackend private (
    asyncHttpClient: AsyncHttpClient,
    closeClient: Boolean)(implicit ec: ExecutionContext)
    extends AsyncHttpClientBackend[Future, Nothing](asyncHttpClient,
                                                    new FutureMonad,
                                                    closeClient) {

  override protected def streamBodyToPublisher(
      s: Nothing): Publisher[ByteBuffer] = s // nothing is everything

  override protected def publisherToStreamBody(
      p: Publisher[ByteBuffer]): Nothing =
    throw new IllegalStateException("This backend does not support streaming")

  override protected def publisherToString(
      p: Publisher[ByteBuffer]): Future[String] =
    throw new IllegalStateException("This backend does not support streaming")
}

object AsyncHttpClientFutureBackend {

  private def apply(asyncHttpClient: AsyncHttpClient, closeClient: Boolean)(
      implicit ec: ExecutionContext): SttpBackend[Future, Nothing] =
    new FollowRedirectsBackend[Future, Nothing](
      new AsyncHttpClientFutureBackend(asyncHttpClient, closeClient))

  /**
    * @param ec The execution context for running non-network related operations,
    *           e.g. mapping responses. Defaults to the global execution
    *           context.
    */
  def apply(connectionTimeout: FiniteDuration =
              SttpBackend.DefaultConnectionTimeout)(
      implicit ec: ExecutionContext = ExecutionContext.Implicits.global)
    : SttpBackend[Future, Nothing] =
    AsyncHttpClientFutureBackend(
      AsyncHttpClientBackend.defaultClient(connectionTimeout.toMillis.toInt),
      closeClient = true)

  /**
    * @param ec The execution context for running non-network related operations,
    *           e.g. mapping responses. Defaults to the global execution
    *           context.
    */
  def usingConfig(cfg: AsyncHttpClientConfig)(
      implicit ec: ExecutionContext = ExecutionContext.Implicits.global)
    : SttpBackend[Future, Nothing] =
    AsyncHttpClientFutureBackend(new DefaultAsyncHttpClient(cfg),
                                 closeClient = true)

  /**
    * @param ec The execution context for running non-network related operations,
    *           e.g. mapping responses. Defaults to the global execution
    *           context.
    */
  def usingClient(client: AsyncHttpClient)(implicit ec: ExecutionContext =
                                             ExecutionContext.Implicits.global)
    : SttpBackend[Future, Nothing] =
    AsyncHttpClientFutureBackend(client, closeClient = false)
}
