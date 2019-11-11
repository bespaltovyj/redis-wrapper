package demo.redis.wrapper.data;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;
import java.util.function.Consumer;

public class RedisList extends AbstractList<String> {

    private static final String OK_RESULT = "OK";
    private static final int ONE_ELEMENT = 1;

    private final Jedis jedis;
    private final String listKey;
    private final byte[] encodedKey;

    RedisList(Jedis jedis, String listKey) {
        this.jedis = jedis;
        this.listKey = listKey;
        this.encodedKey = SafeEncoder.encode(listKey);
    }

    @Override
    public int size() {
        return jedis.llen(encodedKey).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        Iterator<String> itr = new Itr(size());
        while (itr.hasNext()) {
            String el = itr.next();
            if (Objects.equals(o, el)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        return new Itr(size());
    }

    @Override
    public boolean add(String s) {
        int crntSize = size();
        if (crntSize == Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        modCount++;
        int newSize = jedis.rpush(encodedKey, SafeEncoder.encode(s)).intValue();
        return crntSize != newSize;
    }

    @Override
    public boolean remove(Object o) {
        modCount++;
        return removeIf(el -> Objects.equals(o, el));
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        int currentSize = size();
        if (Integer.MAX_VALUE - currentSize < c.size()) {
            throw new IllegalArgumentException();
        }
        modCount++;
        Long result = jedis.rpush(encodedKey, c.stream().map(SafeEncoder::encode).toArray(byte[][]::new));
        return !Objects.equals(currentSize, result.intValue());
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        int currentSize = size();
        if (Integer.MAX_VALUE - currentSize < c.size()) {
            throw new IllegalArgumentException();
        }
        if (index == 0 && currentSize == 0) {
            addAll(c);
        }
        String el = get(index);
        if (Objects.isNull(el)) {
            return false;
        }
        boolean listChanged = false;
        modCount++;
        for (String newEl : c) {
            jedis.linsert(encodedKey, ListPosition.BEFORE, SafeEncoder.encode(el), SafeEncoder.encode(newEl));
            listChanged = true;
        }
        return listChanged;
    }

    @Override
    public void clear() {
        jedis.del(encodedKey);
    }

    @Override
    public String get(int index) {
        rangeCheck(index);
        return SafeEncoder.encode(jedis.lindex(encodedKey, index));
    }

    @Override
    public String set(int index, String element) {
        rangeCheck(index);
        String result = jedis.lset(encodedKey, index, SafeEncoder.encode(element));
        if (Objects.equals(result, OK_RESULT)) {
            return element;
        }
        return null;
    }

    @Override
    public void add(int index, String element) {
        rangeCheckForAdd(index);
        String el = get(index);
        if (Objects.isNull(el)) {
            return;
        }
        modCount++;
        jedis.linsert(encodedKey, ListPosition.BEFORE, SafeEncoder.encode(el), SafeEncoder.encode(element));
    }

    @Override
    public String remove(int index) {
        rangeCheck(index);
        String el = get(index);
        if (Objects.isNull(el)) {
            return null;
        }
        modCount++;
        Long result = jedis.lrem(encodedKey, ONE_ELEMENT, SafeEncoder.encode(el));
        if (Objects.equals(result.intValue(), ONE_ELEMENT)) {
            return el;
        }
        return null;
    }

    @Override
    public int indexOf(Object o) {
        int ind = 0;
        for (String s : this) {
            ++ind;
            if (Objects.equals(o, s)) {
                return ind;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int ind = size();
        Iterator<String> reverse = new ReverseItr(ind);
        while (reverse.hasNext()) {
            if (Objects.equals(reverse.next(), o)) {
                return ind - 1;
            }
            --ind;
        }
        return -1;
    }

    private void rangeCheck(int index) {
        rangeCheck(index, size());
    }

    private void rangeCheck(int index, int size) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException("");
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index > size() || index < 0)
            throw new IndexOutOfBoundsException("");
    }


    private class Itr implements Iterator<String> {
        int step = 10;
        int leftBord = 0;
        int rightBord;

        List<String> localCache;
        int cursor = 0;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such

        int expectedModCount = modCount;
        int totalElements;

        Itr(int totalElements) {
            this.totalElements = totalElements;
            rightBord = Math.min(step - 1, totalElements - 1);
        }

        public boolean hasNext() {
            return (cursor + leftBord) != totalElements;
        }

        @SuppressWarnings("unchecked")
        public String next() {
            checkForComodification();
            int i = cursor;
            if (Objects.isNull(localCache)) {
                localCache = jedis.lrange(listKey, leftBord, rightBord);
            }
            if (i >= localCache.size()) {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                leftBord = rightBord + 1;
                if (Integer.MAX_VALUE - step <= leftBord) {
                    rightBord = Integer.MAX_VALUE;
                } else {
                    rightBord += step - 1;
                }

                localCache = jedis.lrange(listKey, leftBord, rightBord);
                i = 0;
            }
            cursor = i + 1;
            lastRet = leftBord + i;
            return localCache.get(i);
        }

        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();

            try {
                RedisList.this.remove(lastRet);
                cursor = lastRet - leftBord;
                localCache.remove(cursor);
                --rightBord;
                --totalElements;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super String> consumer) {
            Objects.requireNonNull(consumer);
            while (hasNext()) {
                consumer.accept(next());
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ReverseItr implements Iterator<String> {
        int step = 10;
        int leftBord;
        int rightBord = -1;

        List<String> localCache;
        int cursor = Integer.MAX_VALUE;       // index of next element to return

        int expectedModCount = modCount;
        int totalElements;

        ReverseItr(int totalElements) {
            this.totalElements = totalElements;
            leftBord = Math.max(-step - 1, -totalElements);
        }

        @Override
        public boolean hasNext() {
            return leftBord + totalElements <= cursor;
        }

        @Override
        public String next() {
            checkForComodification();
            if (Objects.isNull(localCache)) {
                localCache = jedis.lrange(listKey, leftBord, rightBord);
                cursor = localCache.size() - 1;
            }
            int i = cursor;
            if (i < 0) {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                rightBord = leftBord - 1;
                if (-Integer.MAX_VALUE + step >= rightBord) {
                    leftBord = -Integer.MAX_VALUE;
                } else {
                    leftBord -= (step + 1);
                }

                localCache = jedis.lrange(listKey, leftBord, rightBord);
                i = localCache.size() - 1;
            }
            cursor = i - 1;
            return localCache.get(i);
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

}
