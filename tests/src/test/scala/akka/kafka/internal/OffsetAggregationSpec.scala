/*
 * Copyright (C) 2014 - 2016 Softwaremill <http://softwaremill.com>
 * Copyright (C) 2016 - 2019 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.kafka.internal

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.requests.OffsetFetchResponse
import org.scalatest.{Matchers, WordSpecLike}

class OffsetAggregationSpec extends WordSpecLike with Matchers {

  val topicA = "topicA"
  val topicB = "topicB"

  "aggregateOffsets" should {
    "give all offsets for one element" in {
      val in = Map(new TopicPartition(topicA, 1) -> new OffsetAndMetadata(12, OffsetFetchResponse.NO_METADATA))
      KafkaConsumerActor.aggregateOffsets(List(in)) shouldBe in
    }

    "give the highest offsets" in {
      val in1 = Map(new TopicPartition(topicA, 1) -> new OffsetAndMetadata(42, OffsetFetchResponse.NO_METADATA))
      val in2 = Map(new TopicPartition(topicA, 1) -> new OffsetAndMetadata(12, OffsetFetchResponse.NO_METADATA))
      KafkaConsumerActor.aggregateOffsets(List(in1, in2)) shouldBe in1
    }

    "give the highest offsets (other order)" in {
      val in1 = Map(new TopicPartition(topicA, 1) -> new OffsetAndMetadata(42, OffsetFetchResponse.NO_METADATA))
      val in2 = Map(new TopicPartition(topicA, 1) -> new OffsetAndMetadata(12, OffsetFetchResponse.NO_METADATA))
      KafkaConsumerActor.aggregateOffsets(List(in2, in1)) shouldBe in1
    }

    "give the highest offsets (when mixed)" in {
      val in1 = Map(
        new TopicPartition(topicA, 1) -> new OffsetAndMetadata(42, OffsetFetchResponse.NO_METADATA),
        new TopicPartition(topicB, 1) -> new OffsetAndMetadata(11, OffsetFetchResponse.NO_METADATA)
      )
      val in2 = Map(
        new TopicPartition(topicA, 1) -> new OffsetAndMetadata(12, OffsetFetchResponse.NO_METADATA),
        new TopicPartition(topicB, 1) -> new OffsetAndMetadata(43, OffsetFetchResponse.NO_METADATA)
      )
      KafkaConsumerActor.aggregateOffsets(List(in1, in2)) shouldBe Map(
        new TopicPartition(topicA, 1) -> new OffsetAndMetadata(42, OffsetFetchResponse.NO_METADATA),
        new TopicPartition(topicB, 1) -> new OffsetAndMetadata(43, OffsetFetchResponse.NO_METADATA)
      )
    }
  }

}
