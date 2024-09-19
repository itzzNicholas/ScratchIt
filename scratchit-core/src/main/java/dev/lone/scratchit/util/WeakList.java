package dev.lone.scratchit.util;

import java.util.Collection;
import java.util.WeakHashMap;

public class WeakList<K>
{
    private final WeakHashMap<K, Object> hashMap;

    public WeakList()
    {
        this.hashMap = new WeakHashMap<>();
    }

    public boolean contains(K obj)
    {
        return hashMap.containsKey(obj);
    }

    public void add(K o)
    {
        hashMap.put(o, null);
    }

    public void remove(K o)
    {
        hashMap.remove(o);
    }

    public Collection<K> getIterable()
    {
        return hashMap.keySet();
    }
}