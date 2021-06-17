package core;

import helper.Constants;
import models.Operation;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Properties;

public class EventProducer {

    /**
     * creates a kafka producer
     * @return the producer
     */
    private static Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "core.LogFileReader");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    /**
     * publishes the events to the topic
     * @param events the events to be published
     * @throws Exception that might occur while trying to publish
     */
    static void produceEvents(final ArrayList<Operation> events) throws Exception {
        final Producer<Long, String> producer = createProducer();
        long time = System.currentTimeMillis();

        try {
            for (Operation operation : events) {
                String topic = operation.getObjectType().name().toLowerCase();
                long index = operation.getTime();
                final ProducerRecord<Long, String> record = new ProducerRecord<>(topic, index, operation.toJson());
                RecordMetadata metadata = producer.send(record).get();
                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf(
                        "sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime);
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }

}