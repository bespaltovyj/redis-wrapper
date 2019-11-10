package demo.redis.wrapper.data;

import demo.redis.wrapper.converter.IntByteArrConverter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RedisMap implements Map<String, Integer> {

    private static final byte[] DEFAULT_CURSOR = IntByteArrConverter.convert(0);
    private static final Long NO_ACTION_RESULT = 0L;

    private final Jedis jedis;
    private final String mapKey;
    private final byte[] encodedKey;

    RedisMap(Jedis jedis, String mapKey) {
        this.jedis = jedis;
        this.mapKey = mapKey;
        this.encodedKey = SafeEncoder.encode(mapKey);
    }

    RedisMap(Jedis jedis, String mapKey, Map<String, Integer> hash) {
        this.jedis = jedis;
        this.mapKey = mapKey;
        this.encodedKey = SafeEncoder.encode(mapKey);
        putAll(hash);
    }


    @Override
    public int size() {
        return jedis.hlen(encodedKey).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return jedis.hexists(encodedKey, SafeEncoder.encode(key.toString()));
    }

    @Override
    public boolean containsValue(Object value) {
        ScanParams scanParams = new ScanParams();
        scanParams.match(SafeEncoder.encode(value.toString()));
        ScanResult<Map.Entry<byte[], byte[]>> result = jedis.hscan(encodedKey, DEFAULT_CURSOR, scanParams);
        return !result.getResult().isEmpty();
    }

    @Override
    public Integer get(Object key) {
        return jedis.hmget(encodedKey, SafeEncoder.encode(key.toString())).stream().findFirst().map(IntByteArrConverter::convert).orElse(null);
    }

    @Override
    public Integer put(String key, Integer value) {
        Long result = jedis.hset(encodedKey, SafeEncoder.encode(key), IntByteArrConverter.convert(value));
        return value;
    }

    @Override
    public Integer remove(Object key) {
        Integer node = get(key);
        if (Objects.nonNull(node)) {
            Long result = jedis.hdel(encodedKey, SafeEncoder.encode(key.toString()));
            if (Objects.equals(NO_ACTION_RESULT, result)) {
                return null;
            }
            return node;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Integer> m) {
        Map<byte[], byte[]> encodedMap = new HashMap<>(m.size());
        m.forEach((k, v) -> {
            encodedMap.put(SafeEncoder.encode(k), IntByteArrConverter.convert(v));
        });
        jedis.hmset(encodedKey, encodedMap);
    }

    @Override
    public void clear() {
        jedis.del(encodedKey);
    }

    @Override
    public Set<String> keySet() {
        return jedis.hkeys(mapKey);
    }

    @Override
    public Collection<Integer> values() {
        return jedis.hvals(encodedKey).stream().map(IntByteArrConverter::convert).collect(Collectors.toList());
    }

    @Override
    public Integer computeIfAbsent(String key, Function<? super String, ? extends Integer> mappingFunction) {
        Integer newValue = mappingFunction.apply(key);
        jedis.hsetnx(encodedKey, SafeEncoder.encode(key), IntByteArrConverter.convert(newValue));
        return get(key);
    }

    @Override
    public Set<Map.Entry<String, Integer>> entrySet() {
        Map<byte[], byte[]> hash = jedis.hgetAll(encodedKey);
        Set<Map.Entry<String, Integer>> result = new HashSet<>(hash.size());
        hash.forEach((k, v) -> result.add(new Entry(IntByteArrConverter.convert(v), SafeEncoder.encode(k))));
        return result;
    }


    private static class Entry implements Map.Entry<String, Integer> {
        private Integer value;
        private String key;

        Entry(Integer value, String key) {
            this.value = value;
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            this.value = value;
            return this.value;
        }
    }

}
