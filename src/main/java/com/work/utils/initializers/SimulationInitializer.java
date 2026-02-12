package com.work.utils.initializers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.work.domain.constants.Settings;
import com.work.domain.constants.Topics;

/**Сервис инициализации.*/
public class SimulationInitializer {
    protected static final Logger logger = LoggerFactory.getLogger(SimulationInitializer.class);
        
    public static void main(String[] args) {
        try (AdminClient adminClient = AdminClient.create(createProperties())) {
            List<NewTopic> topicsToCreate = Arrays.asList(
                new NewTopic(Topics.BAN, Settings.PARTITIONS_COUNT, (short) Settings.REPLICATION_FACTOR),
                new NewTopic(Topics.RAW_MESSAGE, Settings.PARTITIONS_COUNT, (short) Settings.REPLICATION_FACTOR),
                new NewTopic(Topics.MESSAGE, Settings.PARTITIONS_COUNT, (short) Settings.REPLICATION_FACTOR),

                // Справочные топики настраиваются с политикой compact для вечного хранения ключей.
                new NewTopic(Topics.USER, Settings.GLOBAL_PARTITIONS_COUNT, (short) Settings.REPLICATION_FACTOR)
                    .configs(Collections.singletonMap("cleanup.policy", "compact")),
                new NewTopic(Topics.PROHIBITED_WORD, Settings.GLOBAL_PARTITIONS_COUNT, (short) Settings.REPLICATION_FACTOR)
                    .configs(Collections.singletonMap("cleanup.policy", "compact")));
                    
            for (NewTopic topicToCreate : topicsToCreate) {
                try { // Асинхронное создание топика и ожидание подтверждения от всех брокеров.
                    adminClient.createTopics(Collections.singletonList(topicToCreate)).all().get();
                    logger.info("Topic created: \"{}\".", topicToCreate.name());
                } catch (Exception exception) {
                    if (exception.getCause() instanceof TopicExistsException) { // Игнорирование ошибки, если топик уже был создан ранее.
                        logger.info("Topic already exists: \"{}\".", topicToCreate.name());
                    } else {
                        logger.error("Failed to create topic {}: \"{}\".", topicToCreate.name(), exception.getMessage());
                    }
                }
            }
        } catch (Exception exception) {
            logger.error("Could not connect to Kafka to create topics.", exception);
        }
    }

    private static Properties createProperties() {
        Properties properties = new Properties();

        properties.put(AdminClientConfig.CLIENT_ID_CONFIG, Settings.INITIALIZER_CLIENT_ID);
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, Settings.REQUEST_TIMEOUT_MS);
        properties.put(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG, Settings.INITIALIZER_RECONNECT_BACKOFF_MS);
        properties.put(AdminClientConfig.RETRIES_CONFIG, Settings.INITIALIZER_RETRIES_CONFIG);

        return properties;
    }
}
