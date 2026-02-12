package com.work.domain.constants;

/**Настройки (переменные окружения).*/
public class Settings {
    /**Количество реплик для каждого топика.*/
    public static final int REPLICATION_FACTOR = Integer.parseInt(System.getenv().getOrDefault("REPLICATION_FACTOR", "2"));
    /**Время ожидания корректного закрытия ресурсов при выходе из приложения.*/
    public static final long GRACEFUL_SHUTDOWN_WAIT_MS = Long.parseLong(System.getenv().getOrDefault("GRACEFUL_SHUTDOWN_WAIT_MS", "5000"));
    /**Число партиций для справочников.*/
    public static final int GLOBAL_PARTITIONS_COUNT = Integer.parseInt(System.getenv().getOrDefault("GLOBAL_PARTITIONS_COUNT", "1"));
    /**Число партиций для основных потоков данных.*/
    public static final int PARTITIONS_COUNT = Integer.parseInt(System.getenv().getOrDefault("PARTITIONS_COUNT", "3"));
    /**Общий таймаут ожидания ответа от брокера.*/
    public static final int REQUEST_TIMEOUT_MS = Integer.parseInt(System.getenv().getOrDefault("REQUEST_TIMEOUT_MS_CONFIG", "30000"));
    /**Список адресов брокеров кластера.*/
    public static final String BOOTSTRAP_SERVERS = System.getenv().getOrDefault("BOOTSTRAP_SERVERS", "localhost:9092");
    
    /**Пауза между попытками переподключения к брокерам при старте.*/
    public static final int INITIALIZER_RECONNECT_BACKOFF_MS = Integer.parseInt(System.getenv().getOrDefault("INITIALIZER_RECONNECT_BACKOFF_MS_CONFIG", "1000"));
    /**Максимальное количество попыток создания топиков при сбоях.*/
    public static final int INITIALIZER_RETRIES_CONFIG = Integer.parseInt(System.getenv().getOrDefault("INITIALIZER_RETRIES_CONFIG", "5"));
    /**Имя клиента инициализатора для логов Kafka.*/
    public static final String INITIALIZER_CLIENT_ID = System.getenv().getOrDefault("INITIALIZER_CLIENT_ID", "simulation-initializer");
    
    /**Максимальная пауза между циклами генерации случайных событий в симуляции.*/
    public static final int PRODUCER_SIMULATION_CYCLE_MAX_WAIT_MS = Integer.parseInt(System.getenv().getOrDefault("PRODUCER_SIMULATION_CYCLE_MAX_WAIT_MS", "4000"));
    /**Максимальное время на доставку сообщения (включая ретраи).*/
    public static final int PRODUCER_DELIVERY_TIMEOUT_MS = Integer.parseInt(System.getenv().getOrDefault("PRODUCER_DELIVERY_TIMEOUT_MS", "120000"));
    /**Максимальный размер пачки сообщений перед отправкой в Kafka.*/
    public static final int PRODUCER_BATCH_SIZE_BYTES = Integer.parseInt(System.getenv().getOrDefault("PRODUCER_BATCH_SIZE_BYTES", "16384"));
    /**Время ожидания для накопления пачки перед принудительной отправкой.*/
    public static final int PRODUCER_LINGER_MS = Integer.parseInt(System.getenv().getOrDefault("PRODUCER_LINGER_MS", "5"));
    /**Пауза перед повторной попыткой отправки сообщения.*/
    public static final int PRODUCER_RETRY_BACKOFF_MS = Integer.parseInt(System.getenv().getOrDefault("PRODUCER_RETRY_BACKOFF_MS", "100"));

    /**Максимальный объем оперативной памяти для кэширования состояний RocksDB.*/
    public static final int TOPOLOGY_STATESTORE_CACHE_MAX_KB = Integer.parseInt(System.getenv().getOrDefault("TOPOLOGY_STATESTORE_CACHE_MAX_KB", "10240"));
    /**Уникальный идентификатор приложения, использующийся для хранения состояния и в качестве имени группы потребителей.*/
    public static final String TOPOLOGY_APPLICATION_ID = System.getenv().getOrDefault("TOPOLOGY_APPLICATION_ID", "simulation-topology");
    /**Строка, которой будут заменяться запрещенные слова.*/
    public static final String TOPOLOGY_PROHIBITED_MASK = System.getenv().getOrDefault("TOPOLOGY_PROHIBITED_MASK", "***");
    /**Заглушка, если в справочнике не найден логин пользователя по его ID.*/
    public static final String TOPOLOGY_UNKNOWN_LOGIN_MASK = System.getenv().getOrDefault("TOPOLOGY_UNKNOWN_LOGIN_MASK", "Unknown");

    /**Интервал автоматического подтверждения прочитанных сообщений (offsets).*/
    public static final int MONITOR_AUTO_COMMIT_INTERVAL_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_AUTO_COMMIT_INTERVAL_MS", "1000"));
    /**Частота опроса Kafka на наличие новых сообщений.*/
    public static final int MONITOR_POLL_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_POLL_MS", "100"));
    /**Минимальный объем данных, который брокер должен собрать перед ответом консумеру.*/
    public static final int MONITOR_FETCH_MIN_BYTES = Integer.parseInt(System.getenv().getOrDefault("MONITOR_FETCH_MIN_BYTES", "1"));
    /**Максимальное время ожидания брокером накопления минимального объема данных (MONITOR_FETCH_MIN_BYTES).*/
    public static final int MONITOR_FETCH_MAX_WAIT_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_FETCH_MAX_WAIT_MS", "500"));
    /**Время, по истечении которого брокер сочтет монитор «мертвым» и исключит из группы.*/
    public static final int MONITOR_SESSION_TIMEOUT_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_SESSION_TIMEOUT_MS", "45000"));
    /**Частота отправки «сигналов жизни» брокеру (heartbeats), должна быть в 3 раза меньше сессии.*/
    public static final int MONITOR_HEARTBEAT_INTERVAL_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_HEARTBEAT_INTERVAL_MS", "15000"));
    /**Таймаут сетевого запроса монитора, должен быть больше MONITOR_FETCH_MAX_WAIT_MS + время обработки запроса.*/
    public static final int MONITOR_REQUEST_TIMEOUT_MS = Integer.parseInt(System.getenv().getOrDefault("MONITOR_REQUEST_TIMEOUT_MS", "60000"));
    /**Имя группы потребителей для монитора.*/
    public static final String MONITOR_GROUP_ID = System.getenv().getOrDefault("MONITOR_GROUP_ID", "simulation-monitor");
    /**Флаг временного постфикса в имени группы, при установке true монитор будет всегда создавать новую группу и читать историю с начала.*/
    public static final boolean MONITOR_GROUP_ID_USE_MS_POSTFIX = Boolean.parseBoolean(System.getenv().getOrDefault("MONITOR_GROUP_ID_USE_MS_POSTFIX", "true"));
}  
