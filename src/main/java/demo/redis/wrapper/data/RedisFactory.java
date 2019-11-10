package demo.redis.wrapper.data;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisFactory {

    private final Jedis jedis;

    public Map<String, Integer> getMaps(String name) {
        return new RedisMap(jedis, name);
    }

    public Map<String, Integer> createMaps(String name, Map<String, Integer> hash) {
        return new RedisMap(jedis, name, hash);
    }

    public List<String> getList(String name) {
        return new RedisList(jedis, name);
    }
}
