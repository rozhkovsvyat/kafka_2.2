package com.work.utils.topologies;

import java.util.HashSet;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import com.work.domain.constants.Settings;
import com.work.domain.constants.Stores;
import com.work.domain.constants.Topics;
import com.work.domain.dto.BanEventDto;
import com.work.domain.dto.MessageDto;
import com.work.domain.dto.RawMessageDto;
import com.work.domain.dto.UserDto;
import com.work.domain.models.BanModel;
import com.work.utils.processors.LoginEnrichmentProcessor;
import com.work.utils.processors.ProhibitedWordsProcessor;
import com.work.utils.serdes.JsonSerde;

/**Конвейер обработки.*/
public class SimulationTopology {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();

        // Агрегация потока событий блокировки в таблицу состояний (KTable).
        // Хранит актуальный список заблокированных пользователей для каждого владельца стоп-листа.
        KTable<String, BanModel> banTable = builder
            .stream(Topics.BAN, Consumed.with(Serdes.String(), JsonSerde.generic(BanEventDto.class)))
            .groupByKey() // Key: ownerUserId (владелец стоп-листа).
            .aggregate(
                () -> new BanModel(new HashSet<>()),
                (ownerUserId, banEvent, ban) -> {
                    if (banEvent.isUnban) {
                        ban.bannedUserIds.remove(banEvent.bannedUserId);
                    } else {
                        ban.bannedUserIds.add(banEvent.bannedUserId);
                    }
                    return ban;
                },
                Materialized.<String, BanModel, KeyValueStore<Bytes, byte[]>>as(Stores.BAN)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(JsonSerde.generic(BanModel.class)));

        // Глобальные справочники (GlobalKTable) для цензуры и имен пользователей.
        // Доступны всем узлам обработки без необходимости секционирования по ключу.            
        builder.globalTable(
            Topics.PROHIBITED_WORD,
            Consumed.with(Serdes.String(), Serdes.String()),
            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(Stores.PROHIBITED_WORD)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String()));

        builder.globalTable(
            Topics.USER,
            Consumed.with(Serdes.String(), JsonSerde.generic(UserDto.class)),
            Materialized.<String, UserDto, KeyValueStore<Bytes, byte[]>>as(Stores.USER)
                .withKeySerde(Serdes.String())
                .withValueSerde(JsonSerde.generic(UserDto.class)));

        builder
            .stream(Topics.RAW_MESSAGE, Consumed.with(Serdes.String(), JsonSerde.generic(RawMessageDto.class)))  // Key: toUserId (адресат).
            .leftJoin( // Левое соединение с таблицей банов: проверяем, не заблокирован ли отправитель получателем (одресат <-> владелец стоп-листа).
                banTable,
                (message, ban) -> ban != null && ban.bannedUserIds.contains(message.fromUserId) ? null : message,
                Joined.with(Serdes.String(), JsonSerde.generic(RawMessageDto.class), JsonSerde.generic(BanModel.class)))
            .filter((toUserId, message) -> message != null) // Фильтрация: отбрасываем сообщения, которые вернули null после проверки блокировки.
            // Последовательная обработка: сначала цензура текста, затем обогащение логинами.
            .processValues(() -> new ProhibitedWordsProcessor(Stores.PROHIBITED_WORD, Settings.TOPOLOGY_PROHIBITED_MASK))
            .processValues(() -> new LoginEnrichmentProcessor(Stores.USER, Settings.TOPOLOGY_UNKNOWN_LOGIN_MASK))
            .to(Topics.MESSAGE, Produced.with(Serdes.String(), JsonSerde.generic(MessageDto.class)));

        KafkaStreams streams = new KafkaStreams(builder.build(), createProperties());               
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();
    }

    private static Properties createProperties() {
        Properties properties = new Properties();

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, Settings.TOPOLOGY_APPLICATION_ID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Settings.BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        properties.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, Settings.TOPOLOGY_STATESTORE_CACHE_MAX_KB * 1024L);
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
        properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, Settings.REPLICATION_FACTOR);

        properties.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, Settings.REQUEST_TIMEOUT_MS);

        return properties;
    }
}
