package com.work.utils.processors;

import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import com.work.domain.dto.RawMessageDto;

/**Процессор скрытия запрещенных слов в сообщениях.*/
public class ProhibitedWordsProcessor implements FixedKeyProcessor<String, RawMessageDto, RawMessageDto> {
    private final String prohibitedWordStoreName;
    private FixedKeyProcessorContext<String, RawMessageDto> processorContext;
    private ReadOnlyKeyValueStore<String, ValueAndTimestamp<String>> prohibitedWordStore;
    private String prohibitedMask; // Маска для замены запрещенных слов.

    public ProhibitedWordsProcessor(String prohibitedWordStoreName, String prohibitedMask) {
        this.prohibitedWordStoreName = prohibitedWordStoreName;
        this.prohibitedMask = prohibitedMask;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, RawMessageDto> processorContext) {
        this.prohibitedWordStore = processorContext.getStateStore(prohibitedWordStoreName);
        this.processorContext = processorContext;
    }

    @Override
    public void process(FixedKeyRecord<String, RawMessageDto> record) {
        RawMessageDto message = record.value();

        if (message.text != null && !message.text.isEmpty()){
            StringBuilder stringBuilder = new StringBuilder();
            String[] rawWords = message.text.split("\\s+");

            for (String rawWord : rawWords) {
                 // Очистка слова от символов и приведение к нижнему регистру для поиска в справочнике.
                String word = rawWord.replaceAll("[^a-zA-Zа-яА-Я0-9]", "").toLowerCase();

                // Проверка слова по глобальному хранилищу стоп-слов.
                ValueAndTimestamp<String> wordWrapper = prohibitedWordStore.get(word);

                if (wordWrapper != null && wordWrapper.value() != null) {
                    stringBuilder.append(prohibitedMask); // Замена на маску.
                } else {
                    stringBuilder.append(rawWord);
                }
                stringBuilder.append(" ");
                }

            message.text = stringBuilder.toString().trim();
        }

        processorContext.forward(record.withValue(message));
    }

    @Override
    public void close() {}
}
