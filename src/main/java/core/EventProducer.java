package core;

import helper.Constants;
import models.Operation;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
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
     * @param eventsMap the events to be published
     * @throws Exception that might occur while trying to publish
     */
    static void produceEvents(final HashMap<Integer, Operation> eventsMap) throws Exception {
        final Producer<Long, String> producer = createProducer();
        long time = System.currentTimeMillis();

        try {
            for (Map.Entry<Integer, Operation> entry : eventsMap.entrySet()) {
                Operation operation = entry.getValue();
                String topic = operation.getObjectType().name().toLowerCase();
                long index = operation.getTime();
                final ProducerRecord<Long, String> record = new ProducerRecord<>(topic, index, operation.toJson());
                RecordMetadata metadata = producer.send(record).get();
                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf(
                        "sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime);
                DatabaseRequester.deleteProcessedOperation(entry.getKey());
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }

}