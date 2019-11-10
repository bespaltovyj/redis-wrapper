package demo.redis.wrapper.controller;

import demo.redis.wrapper.data.RedisFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class ListController {

    private final RedisFactory redisFactory;

    @GetMapping("/{id}")
    public List<String> getList(@PathVariable("id") String name) {
        return redisFactory.getList(name);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable("id") String name) {
        redisFactory.getList(name).clear();
    }

    @GetMapping("/{id}/el/{index}")
    public String getElement(@PathVariable("id") String name, @PathVariable("index") int index) {
        return redisFactory.getList(name).get(index);
    }


    @PostMapping("/{id}/el")
    public boolean add(@PathVariable("id") String name, @RequestBody String value) {
        List<String> list = redisFactory.getList(name);
        return list.add(value);
    }

    @PostMapping(value = "/{id}/el", params = "index")
    public void addElement(@PathVariable("id") String name, @RequestParam("index") int index, @RequestBody String value) {
        redisFactory.getList(name).add(index, value);
    }

    @PutMapping("/{id}/el/{index}")
    public String replaceElement(@PathVariable("id") String name, @PathVariable("index") int index, @RequestBody String value) {
        return redisFactory.getList(name).set(index, value);
    }

    @DeleteMapping("/{id}/el/{index}")
    public String removeByIndex(@PathVariable("id") String name, @PathVariable("index") int index) {
        return redisFactory.getList(name).remove(index);
    }

    @DeleteMapping("/{id}/el")
    public boolean removeByVal(@PathVariable("id") String name, @RequestBody String value) {
        List<String> list = redisFactory.getList(name);
        return list.remove(value);
    }

    @PostMapping("/{id}/el/contains")
    public boolean contains(@PathVariable("id") String name, @RequestBody String value) {
        return redisFactory.getList(name).contains(value);
    }

    @PostMapping("/{id}/el/index-of")
    public int indexOf(@PathVariable("id") String name, @RequestBody String value) {
        return redisFactory.getList(name).indexOf(value);
    }

    @PostMapping("/{id}/el/last-index-of")
    public int lastIndexOf(@PathVariable("id") String name, @RequestBody String value) {
        return redisFactory.getList(name).lastIndexOf(value);
    }

    @GetMapping("/{id}/size")
    public int size(@PathVariable("id") String name) {
        return redisFactory.getList(name).size();
    }


    @PostMapping("/{id}/add-all")
    public boolean addAll(@PathVariable("id") String name, @RequestBody List<String> value) {
        List<String> list = redisFactory.getList(name);
        return list.addAll(value);
    }

    @PostMapping(value = "/{id}/add-all", params = "index")
    public boolean addAll(@PathVariable("id") String name, @RequestParam("index") int index, @RequestBody List<String> value) {
        List<String> list = redisFactory.getList(name);
        return list.addAll(index, value);
    }


}
