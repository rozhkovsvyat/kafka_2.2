package com.work.utils.producers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.work.domain.constants.Data;
import com.work.domain.constants.Settings;
import com.work.domain.constants.Topics;
import com.work.domain.dto.BanEventDto;
import com.work.domain.dto.RawMessageDto;
import com.work.domain.dto.UserDto;
import com.work.utils.serdes.JsonSerde;

/**Симулятор активности.*/
public class SimulationProducer {
    protected static final Logger logger = LoggerFactory.getLogger(SimulationProducer.class);

    public static void main(String[] args) {
        KafkaProducer<String, byte[]> kafkaProducer = new KafkaProducer<>(createProperties());
        setupShutdownHook(kafkaProducer);

        logger.info("Starting simulation...");

         // Первичное наполнение справочников запрещенных слов и пользователей в Kafka.
        Data.PROHIBITED_WORDS.forEach(word -> send(kafkaProducer, Topics.PROHIBITED_WORD, word, word));
        Data.USER_MAP.forEach((id, login) -> send(kafkaProducer, Topics.USER, id, new UserDto(id, login)));

        List<String> userIds = new ArrayList<>(Data.USER_MAP.keySet());
        Random random = new Random();
        
        logger.info("Simulation started.");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Имитация случайной задержки между событиями в системе (от 1 секунды до SIMULATION_CYCLE_MAX_WAIT_MS).
                Thread.sleep(random.nextInt(Math.max(Settings.PRODUCER_SIMULATION_CYCLE_MAX_WAIT_MS, 1000) - 1000) + 1000);
                
                // Случайный выбор отправителя и получателя для генерации трафика.
                String sourceUserId = userIds.get(random.nextInt(userIds.size()));
                String targetUserId = userIds
                    .stream()
                    .filter(userId -> !userId.equals(sourceUserId))
                    .collect(Collectors.toList())
                    .get(random.nextInt(userIds.size() - 1));

                int nextInt = random.nextInt(10);
                if (nextInt < 2) { // Событие блокировки (вероятность 20%).
                    boolean isUnban = nextInt % 2 == 0; // Бан или анбан (50% на 50%).
                    send(kafkaProducer, Topics.BAN, sourceUserId, new BanEventDto(sourceUserId, targetUserId, isUnban));
                    logger.info("{} {} {}.", Data.USER_MAP.get(sourceUserId), isUnban ? "unblocked" : "blocked", Data.USER_MAP.get(targetUserId));
                } else { // Отправка сообщения (вероятность 80%).
                    String messageText = Data.MESSAGES.get(random.nextInt(Data.MESSAGES.size()));
                    // Ключ сообщения — ID получателя (критично для корректного Join в топологии).
                    send(kafkaProducer, Topics.RAW_MESSAGE, targetUserId, new RawMessageDto(sourceUserId, targetUserId, messageText));
                    logger.info("{} sent message to {}: \"{}\".", Data.USER_MAP.get(sourceUserId), Data.USER_MAP.get(targetUserId), messageText);
                }
            } 
         } catch (InterruptedException exception) {
            logger.warn("Simulation interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception exception){
            logger.error("Simulation error.", exception);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <TMessage> void send(KafkaProducer<String, byte[]> kafkaProducer, String topic, String key, TMessage value) {

        byte[] data = (value instanceof String stringValue) 
            ? stringValue.getBytes(StandardCharsets.UTF_8) 
            : JsonSerde.generic((Class<Object>) value.getClass()).serializer().serialize(topic, value);
        
        kafkaProducer.send(new ProducerRecord<>(topic, key, data));
    }

    private static Properties createProperties() {
        Properties properties = new Properties();
        
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Settings.PRODUCER_DELIVERY_TIMEOUT_MS);

        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, Settings.PRODUCER_LINGER_MS);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, Settings.PRODUCER_BATCH_SIZE_BYTES);

        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Settings.REQUEST_TIMEOUT_MS);
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, Settings.PRODUCER_RETRY_BACKOFF_MS);

        return properties;
    }

    protected static void setupShutdownHook(KafkaProducer<String, byte[]> kafkaProducer){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Finishing simulation...");
            kafkaProducer.close(Duration.ofMillis(Settings.GRACEFUL_SHUTDOWN_WAIT_MS)); 
            logger.info("Simulation finished.");
        }));
    }
}
