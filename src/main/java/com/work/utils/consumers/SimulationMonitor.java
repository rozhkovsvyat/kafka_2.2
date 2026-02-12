package com.work.utils.consumers;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.work.domain.constants.Settings;
import com.work.domain.constants.Topics;
import com.work.domain.dto.BanEventDto;
import com.work.domain.dto.MessageDto;
import com.work.domain.dto.RawMessageDto;
import com.work.domain.dto.UserDto;
import com.work.utils.serdes.JsonSerde;

/**Визуализатор симуляции.*/
public class SimulationMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SimulationMonitor.class);
    // Локальный кэш имен, наполняемый из Kafka для человекочитаемого вывода в консоль.
    private static final Map<String, String> userCache = new HashMap<>();

    public static void main(String[] args) {
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(createProperties());
        setupShutdownHook(kafkaConsumer, Thread.currentThread());

        try {
            // Подписка на все топики системы для визуализации процесса обработки в реальном времени.
            kafkaConsumer.subscribe(Arrays.asList(Topics.USER, Topics.RAW_MESSAGE, Topics.BAN, Topics.MESSAGE));

            logger.info("Starting monitoring...");

            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(Settings.MONITOR_POLL_MS));
                for (ConsumerRecord<String, String> record : records) {
                    String messageData = record.value();
                    String topic = record.topic();

                    if (topic.equals(Topics.USER)) { // Синхронизация локального кэша имен при появлении новых пользователей.
                        UserDto user = JsonSerde.generic(UserDto.class).deserializer().deserialize(topic, messageData.getBytes());

                        if (user != null) {
                            userCache.put(user.id, user.login);
                        }
                    } 
                    else if (topic.equals(Topics.BAN)) { // Логирование всех событий блокировки/разблокировки в системе.
                        BanEventDto banEvent = JsonSerde.generic(BanEventDto.class).deserializer().deserialize(topic, messageData.getBytes());

                        String ownerUserLogin = userCache.getOrDefault(banEvent.ownerUserId, banEvent.ownerUserId);
                        String bannedUserLogin = userCache.getOrDefault(banEvent.bannedUserId, banEvent.bannedUserId);
                        
                        logger.info("{} {} {}.", ownerUserLogin, banEvent.isUnban ? "unblocked" : "blocked", bannedUserLogin);

                    } else if (topic.equals(Topics.RAW_MESSAGE)) { // Логирование всех входящих попыток отправки сообщений (без текста).
                        RawMessageDto rawMessage = JsonSerde.generic(RawMessageDto.class).deserializer().deserialize(topic, messageData.getBytes());

                        String fromUserLogin = userCache.getOrDefault(rawMessage.fromUserId, rawMessage.fromUserId);
                        String toUserLogin = userCache.getOrDefault(rawMessage.toUserId, rawMessage.toUserId);

                        logger.info("{} sent message to {}.", fromUserLogin, toUserLogin);

                    } else if (topic.equals(Topics.MESSAGE)) { // Вывод финального результата: только доставленные сообщения с примененной цензурой.
                        MessageDto message = JsonSerde.generic(MessageDto.class).deserializer().deserialize(topic, messageData.getBytes());

                        logger.info("{} received message from {}: \"{}\".", message.toLogin, message.fromLogin, message.text);
                    }
                }
            }
        } catch (WakeupException exception) {
            logger.info("Finishing monitoring...");
        } catch (Exception exception) {
            logger.error("Unexpected monitoring error.", exception);
        } finally {
            kafkaConsumer.close();
            logger.info("Monitoring finished.");
        }
    }

    private static Properties createProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, Settings.MONITOR_GROUP_ID + (Settings.MONITOR_GROUP_ID_USE_MS_POSTFIX ? getMonitorGroupIdPostfix() : ""));
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Settings.MONITOR_AUTO_COMMIT_INTERVAL_MS);

        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, Settings.MONITOR_FETCH_MIN_BYTES);
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, Settings.MONITOR_FETCH_MAX_WAIT_MS);
    
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, Settings.MONITOR_SESSION_TIMEOUT_MS);
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, Settings.MONITOR_HEARTBEAT_INTERVAL_MS);
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, Settings.MONITOR_REQUEST_TIMEOUT_MS);

        return properties;
    }

    // Групповой ID с постфиксом времени позволяет монитору всегда читать историю с начала.
    private static String getMonitorGroupIdPostfix() {
        return "_" + System.currentTimeMillis();
    }

    protected static void setupShutdownHook(KafkaConsumer<String, String> kafkaConsumer, Thread mainThread){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Waiting for monitor to shut down...");
            kafkaConsumer.wakeup();

            try {
                mainThread.join(Settings.GRACEFUL_SHUTDOWN_WAIT_MS);
            } catch (InterruptedException exception) {
                logger.warn("Monitoring interrupted.");
                mainThread.interrupt();
            }
        }));
    }
}
