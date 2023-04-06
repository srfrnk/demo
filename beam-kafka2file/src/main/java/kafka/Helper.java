package kafka;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

class Helper {
  static <K, V> KafkaConsumer<K, V> getConsumer(Options<K, V> options) {
    Properties props = new Properties();

    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.bootstrapServers());
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, options.groupId());
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        options.autoOffsetResetConfig().toString().toLowerCase());
    props.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
        options.isolationLevel().toString().toLowerCase());
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        options.keyDeserializer().getName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        options.valueDeserializer().getName());
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("internal.leave.group.on.close", "true");
    // props.setProperty("allow.auto.create.topics", "true");
    // props.setProperty("auto.commit.interval.ms", "5");
    // props.setProperty("auto.include.jmx.reporter", "true");
    // props.setProperty("check.crcs", "true");
    // props.setProperty("client.dns.lookup", "use_all_dns_ips");
    // props.setProperty("client.id", "consumer-kafka2file-1");
    // props.setProperty("client.rack", "");
    // props.setProperty("connections.max.idle.ms", "540000");
    // props.setProperty("default.api.timeout.ms", "60000");
    // props.setProperty("exclude.internal.topics", "true");
    // props.setProperty("fetch.max.bytes", "52428800");
    // props.setProperty("fetch.max.wait.ms", "500");
    // props.setProperty("fetch.min.bytes", "1");
    // props.setProperty("heartbeat.interval.ms", "3000");
    // props.setProperty("interceptor.classes", "");
    // props.setProperty("internal.throw.on.fetch.stable.offset.unsupported", "false");
    // props.setProperty("max.partition.fetch.bytes", "1048576");
    // props.setProperty("max.poll.interval.ms", "300000");
    // props.setProperty("max.poll.records", "500");
    // props.setProperty("metadata.max.age.ms", "300000");
    // props.setProperty("metric.reporters", "");
    // props.setProperty("metrics.num.samples", "2");
    // props.setProperty("metrics.recording.level", "INFO");
    // props.setProperty("metrics.sample.window.ms", "30000");
    // props.setProperty("partition.assignment.strategy",
    // "org.apache.kafka.clients.consumer.RangeAssignor,org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
    // props.setProperty("receive.buffer.bytes", "524288");
    // props.setProperty("reconnect.backoff.max.ms", "1000");
    // props.setProperty("reconnect.backoff.ms", "50");
    // props.setProperty("request.timeout.ms", "30000");
    // props.setProperty("retry.backoff.ms", "100");
    // props.setProperty("sasl.kerberos.kinit.cmd", "/usr/bin/kinit");
    // props.setProperty("sasl.kerberos.min.time.before.relogin", "60000");
    // props.setProperty("sasl.kerberos.ticket.renew.jitter", "0.05");
    // props.setProperty("sasl.kerberos.ticket.renew.window.factor", "0.8");
    // props.setProperty("sasl.login.refresh.buffer.seconds", "300");
    // props.setProperty("sasl.login.refresh.min.period.seconds", "60");
    // props.setProperty("sasl.login.refresh.window.factor", "0.8");
    // props.setProperty("sasl.login.refresh.window.jitter", "0.05");
    // props.setProperty("sasl.login.retry.backoff.max.ms", "10000");
    // props.setProperty("sasl.login.retry.backoff.ms", "100");
    // props.setProperty("sasl.mechanism", "GSSAPI");
    // props.setProperty("sasl.oauthbearer.clock.skew.seconds", "30");
    // props.setProperty("sasl.oauthbearer.jwks.endpoint.refresh.ms", "3600000");
    // props.setProperty("sasl.oauthbearer.jwks.endpoint.retry.backoff.max.ms", "10000");
    // props.setProperty("sasl.oauthbearer.jwks.endpoint.retry.backoff.ms", "100");
    // props.setProperty("sasl.oauthbearer.scope.claim.name", "scope");
    // props.setProperty("sasl.oauthbearer.sub.claim.name", "sub");
    // props.setProperty("security.protocol", "PLAINTEXT");
    // props.setProperty("send.buffer.bytes", "131072");
    // props.setProperty("session.timeout.ms", "45000");
    // props.setProperty("socket.connection.setup.timeout.max.ms", "30000");
    // props.setProperty("socket.connection.setup.timeout.ms", "10000");
    // props.setProperty("ssl.enabled.protocols", "TLSv1.2, TLSv1.3");
    // props.setProperty("ssl.endpoint.identification.algorithm", "https");
    // props.setProperty("ssl.keymanager.algorithm", "SunX509");
    // props.setProperty("ssl.keystore.type", "JKS");
    // props.setProperty("ssl.protocol", "TLSv1.3");
    // props.setProperty("ssl.trustmanager.algorithm", "PKIX");
    // props.setProperty("ssl.truststore.type", "JKS");

    KafkaConsumer<K, V> consumer = new KafkaConsumer<>(props);
    return consumer;
  }
}
