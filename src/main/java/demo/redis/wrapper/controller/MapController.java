package demo.redis.wrapper.controller;

import demo.redis.wrapper.data.RedisFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final RedisFactory redisFactory;

    @GetMapping("/{hash_name}")
    public Map<String, Integer> getHash(@PathVariable("hash_name") String name) {
        return redisFactory.getMaps(name);
    }

    @PostMapping("/{hash_name}")
    public Map<String, Integer> createHash(@PathVariable("hash_name") String name, @RequestBody Map<String, Integer> hash) {
        return redisFactory.createMaps(name, hash);
    }

    @DeleteMapping("/{hash_name}")
    public void deleteMap(@PathVariable("hash_name") String name) {
        redisFactory.getMaps(name).clear();
    }

    @GetMapping("/{hash_name}/key-set")
    public Set<String> getKeySet(@PathVariable("hash_name") String name) {
        return redisFactory.getMaps(name).keySet();
    }

    @GetMapping("/{hash_name}/values")
    public Collection<Integer> getValues(@PathVariable("hash_name") String name) {
        return redisFactory.getMaps(name).values();
    }

    @GetMapping("/{hash_name}/entry-set")
    public  Set<Map.Entry<String, Integer>> getEntrySet(@PathVariable("hash_name") String name) {
        return redisFactory.getMaps(name).entrySet();
    }

    @PostMapping("/{hash_name}/size")
    public int getHashSize(@PathVariable("hash_name") String name) {
        return redisFactory.getMaps(name).size();
    }

    @GetMapping("/{hash_name}/contains-values")
    public boolean containsValues(@PathVariable("hash_name") String name, @RequestBody Integer value) {
        return redisFactory.getMaps(name).containsValue(value);
    }

    @GetMapping("/{hash_name}/key/{key_name}")
    public Integer getElement(@PathVariable("hash_name") String name, @PathVariable("key_name") String keyName) {
        return redisFactory.getMaps(name).get(keyName);
    }

    @GetMapping("/{hash_name}/key/{key_name}/contains")
    public boolean containsElement(@PathVariable("hash_name") String name, @PathVariable("key_name") String keyName) {
        return redisFactory.getMaps(name).containsKey(keyName);
    }

    @PutMapping("/{hash_name}/key/{key_name}")
    public Integer putElement(@PathVariable("hash_name") String name, @PathVariable("key_name") String keyName, @RequestBody Integer value) {
        return redisFactory.getMaps(name).put(keyName, value);
    }

    @DeleteMapping("/{hash_name}/key/{key_name}")
    public Integer deleteElement(@PathVariable("hash_name") String name, @PathVariable("key_name") String keyName) {
        return redisFactory.getMaps(name).remove(keyName);
    }


}
