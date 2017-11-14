package com.lambdalab.rabbitmq

import com.lambdalab.logging.Logger
import com.lambdalab.projectservice.MQEvent
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Channel, ConnectionFactory}
import com.twitter.finagle.tracing.{Annotation, Trace}
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec, ThriftStructSerializer}
import com.twitter.finagle.http.HttpTracing.Header


abstract class EventSender(
  mqEventSerializerType: EventSerializerType.EventSerializerType = EventSerializerType.COMPACT_THRIFT,
  testChannel: Option[FakeChannel] = None
) extends EventBase {
  def logger:Logger
  def DELIVERY_MODE = 2  // Persistent

  private final val mqEventSerializer = EventSerializerType.createSerializer(mqEventSerializerType, MQEvent)
  def getRequestSerializer[T <: ThriftStruct](t: ThriftStructCodec[T]): ThriftStructSerializer[T] = {
    EventSerializerType.createSerializer(REQUEST_SERIALIZER_TYPE, t)
  }

  val hostname: String = {
    import java.net.InetAddress
    val localhost = InetAddress.getLocalHost
    localhost.getHostName
  }

  private var channel: Option[Channel] = None
  private def getChannel(): Channel = {
    (testChannel, this.channel) match {
      case (Some(testChannel), _) => testChannel
      case (None, Some(channel)) => channel
      case (None, None) => {
        val factory = new ConnectionFactory()
        factory.setHost(RABBIT_MQ_HOST)
        val connection  = factory.newConnection()
        this.channel = Some(connection.createChannel())
        this.channel.get
      }
    }
  }

  def send(event: MQEvent): Unit = sendWithTracer(event)

  def getMqTraceHeaders(): Map[String, AnyRef] =
    Trace.idOption match {
      case Some(spanId) => Map(
        Header.TraceId -> spanId.traceId.toString(),
        Header.SpanId -> spanId.spanId.toString(),
        Header.ParentSpanId -> spanId.parentId.toString(),
        Header.Sampled -> spanId.sampled.getOrElse(false).toString
      )
      case None => Map.empty[String, AnyRef]
    }

  private def sendWithTracer(event: MQEvent): Unit = {
    this.getChannel().exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, EXCHANGE_DURABLE)

    val eventWithHostName = event.copy(sender = Some(event.sender.getOrElse(hostname)))
    val messageBytes = mqEventSerializer.toBytes(eventWithHostName)
    val bindingKey = getBindingKey(eventWithHostName.requestType)
    logger.info(s"Sent ${eventWithHostName.requestType} with BindingKey $bindingKey")

    // Persistent messages
    val propBuilder = new BasicProperties.Builder()
    propBuilder.deliveryMode(DELIVERY_MODE)

    import scala.collection.JavaConverters._
    val headers: Map[String, AnyRef] = getMqTraceHeaders() ++ Map(
      EVENT_SERIALIZER_TYPE_HEADER_KEY -> mqEventSerializerType.toString
    )
    propBuilder.headers(headers.asJava)
    val props = propBuilder.build()

    Trace.record(Annotation.ClientSend())
    Trace.time(s"$EVENT_TRACING_PREFIX ${event.requestType} Sent") {
      this.getChannel().basicPublish(EXCHANGE_NAME, bindingKey, props, messageBytes)
    }
  }
}
