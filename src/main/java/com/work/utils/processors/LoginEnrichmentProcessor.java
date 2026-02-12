package com.work.utils.processors;

import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import com.work.domain.dto.MessageDto;
import com.work.domain.dto.RawMessageDto;
import com.work.domain.dto.UserDto;

/**Процессор обогащения сообщений логинами пользователей.*/
public class LoginEnrichmentProcessor implements FixedKeyProcessor<String, RawMessageDto, MessageDto> {
    private final String userStoreName;
    private FixedKeyProcessorContext<String, MessageDto> processorContext;
    private ReadOnlyKeyValueStore<String, ValueAndTimestamp<UserDto>> userStore;
    private String unknownLoginMask; // Заглушка логина пользователя, не найденного в справочнике по его ID.

    public LoginEnrichmentProcessor(String userStoreName, String unknownLoginMask) {
        this.unknownLoginMask = unknownLoginMask;
        this.userStoreName = userStoreName;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, MessageDto> processorContext) {
        this.userStore = processorContext.getStateStore(userStoreName);
        this.processorContext = processorContext;
    }

    @Override
    public void process(FixedKeyRecord<String, RawMessageDto> record) {
        RawMessageDto rawMessage = record.value();

        // Извлечение данных из GlobalKTable через обертку ValueAndTimestamp (особенность RocksDB для GlobalTable).
        ValueAndTimestamp<UserDto> fromUserWrapper = userStore.get(rawMessage.fromUserId);
        ValueAndTimestamp<UserDto> toUserWrapper = userStore.get(rawMessage.toUserId);

        UserDto fromUser = (fromUserWrapper != null) ? fromUserWrapper.value() : null;
        UserDto toUser = (toUserWrapper != null) ? toUserWrapper.value() : null;

        // Преобразование "сырого" сообщения в обогащенное (с логинами)
        MessageDto message = new MessageDto(
            rawMessage,
            (fromUser != null) ? fromUser.login : unknownLoginMask,
            (toUser != null) ? toUser.login : unknownLoginMask);

        processorContext.forward(record.withValue(message));
    }

    @Override
    public void close() {}
}
