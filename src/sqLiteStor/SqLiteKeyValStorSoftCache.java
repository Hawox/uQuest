package sqLiteStor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.ref.SoftReference;

import com.facebook.infrastructure.utils.CountingBloomFilter;


public class SqLiteKeyValStorSoftCache<U extends Serializable>{
  private String group;
  private SqLiteKeyValStorBacking backing;
  private Hashtable<String, SoftReference<U>> cache;
  private CountingBloomFilter bloom;
  
  /**
   * Creates a new instance of the key value store with a SoftReference cache to save memory.
   * Loads all current keys in the group from disk.
   * <p>
   * DO NOT instantiate more than one for the same group and DB file. It will not cause any direct failure or error
   * but because of in memory caching neither will see the other's changes. This will lead to subtle bugs in your code.
   * <p>
   * You have been warned.
   * <p>
   * The backing DB file is asynchronous so that puts and updates can return faster. The back-end flushes on exit,
   * but be sure that at that point in time nothing is trying to add to the queue. It might not make it.
   * <p>
   * All functions are thread safe.
   * @author \\
   * @param group
   * @param dbFile
   */
  public SqLiteKeyValStorSoftCache(String group, String dbFile){
    this.backing = SqLiteKeyValStorBacking.getInstance(dbFile);
    this.group = group;
    this.cache = new Hashtable<String, SoftReference<U>>();
    this.bloom = new CountingBloomFilter(262144, 256);
    for(String key : this.backing.getAllKeysInGroup(this.group)){
      this.bloom.add(key);
    }
    new Janitor<U>(this.cache).start();
  }
  
  @SuppressWarnings("unchecked")
  public U get(String key){
    synchronized(this.cache){
      U obj;
      SoftReference<U> ref;
      if((ref = this.cache.get(key)) != null){
        if((obj = ref.get()) != null){
          return obj;
        }
      }else{
        if(!this.bloom.isPresent(key)){ //if the key is in the cache but it just has expired then we do not ALSO need to check the bloom filter
          return null;
        }
      }
      obj = (U)this.backing.get(key, this.group);
      if(obj != null){
        this.cache.put(key, new SoftReference<U>(obj));
      }
      return obj;
    }
  }
  
  /**
   * 
   * Returns an array of Map Entries. Note that if they are manipulated you will have to sync them externally.
   * 
   * @return array of Map.Entry's 
   */
  public Map<String,U> getAllEntryArray(){
    synchronized(this.cache){
      Hashtable<String, U> ret = new Hashtable<String, U>();
      this.backing.getAllInGroup(this.group, ret);
      SoftReference<U> obj;
      for(Map.Entry<String,U> ent : ret.entrySet()){
        if((obj = this.cache.get(ent.getKey())) != null){
          ent.setValue(obj.get());
        }else{
          this.cache.put(ent.getKey(), new SoftReference<U>(ent.getValue())); //needed to maintain continuity of soft references. If not for this then a get could return a DIFFERENT instance of what should be the same object!
        }
      }
      return ret;
    }
  }
  
  /**
   * Adds an entry to the table and puts a copy on disk.
   * If you have modified an existing entry use {@link update} instead.
   * <p>
   * Caution: if you have created a new object and wish to replace an existing key with that use {@link updateRef}
   * This will keep the bloom filter as clean as possible.
   * 
   * @param key
   * @param obj
   */
  
  public void put(String key, U obj){
    synchronized(this.cache){
      this.bloom.add(key);
      this.cache.put(key, new SoftReference<U>(obj));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream objOut;
      try{
        objOut = new ObjectOutputStream(baos);
        objOut.writeObject(obj);
        objOut.close();
        this.backing.queue.add(new KeyValAction(key, this.group, baos, KVActions.PUT, obj));
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Removes the entry from the cache and disk 
   * @param key
   */
  
  public void remove(String key){
    synchronized(this.cache){
      this.cache.remove(key);
      if(bloom.isPresent(key)){
        this.bloom.delete(key);
      }
      this.backing.queue.add(new KeyValAction(key, this.group, null, KVActions.REMOVE));
    }
  }
  
  /**
   * Updates the entry on-disk. Only requires a key because it assumes you have modified the reference.
   * <p>
   * If you created a whole new reference for an existing key use {@link updateRef} instead.
   * <p>
   * If the object is not in the cache, or it has fallen out of memory we will throw exceptions. If there is a hard reference anywhere in the application this won't happen.
   * If you get any exceptions from this call, make sure you have a copy of the object somewhere when this is called.
   * @see put 
   * @param key
   */
  
  public void update(String key){
    synchronized(this.cache){
      U fromCache;
      SoftReference<U> ref;
      if((ref = this.cache.get(key)) == null){
        throw new IllegalArgumentException("key is not present");
      }
      fromCache = ref.get();
      if(!(fromCache == null)){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objOut;
        try{
          objOut = new ObjectOutputStream(baos);
          objOut.writeObject(fromCache);
          objOut.close();
          this.backing.queue.add(new KeyValAction(key, this.group, baos, KVActions.PUT));
        }catch(IOException e){
          e.printStackTrace();
        }
      }else{
        throw new IllegalArgumentException("Object has fallen out of memory");
      }
    }
  }
  
  /**
   * Almost the same as put, but does not update the bloom filter.
   * If something is known to already be in the store, use this instead o the bloom filter does not start returning false positives any more than need be
   * <p>
   * Though it is not a hard guarantee of correctness it will throw an exception if the key does not exist in the bloom filter.
   * This will at least stop unreachable keys from being created and should demonstrate when client code is incorrectly calling this function.
   * 
   * @param key
   * @param obj
   */
  
  public void updateRef(String key, U obj){
    if(!this.bloom.isPresent(key)){
      throw new IllegalArgumentException("key is not present");
    }
    synchronized(this.cache){
      this.cache.put(key, new SoftReference<U>(obj));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream objOut;
      try{
        objOut = new ObjectOutputStream(baos);
        objOut.writeObject(obj);
        objOut.close();
        this.backing.queue.add(new KeyValAction(key, this.group, baos, KVActions.PUT, obj));
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Blocks until the backer's queue is empty.
   * <br>
   * Note that since the backer can be shared there still may be a considerable amount of activity in the queue
   * <p>
   * 
   * Here is how it works: The queue is locked until it is empty and commits to the DB. Then it is unlocked and notifies are sent.
   * <p>
   * At this point the flush returns and any data that was in the queue until the notify was sent is on disk. 
   * <p>
   * Because all methods that can modify the queue are synchronized this means that anything that was added to the queue before the flush is on disk.
   * 
   */
  
  public void flush(){
    synchronized(this.cache){
      this.backing.flush();
    }
  }
  
}

/**
 * Every 5 minutes it checks to see if any of the soft references have fallen out of memory and clears the soft reference objects from the cache
 * @author \\
 *
 */

class Janitor<U> extends Thread{
  Hashtable<String, SoftReference<U>>  cache;
  Janitor(Hashtable<String, SoftReference<U>> cache){
    this.cache = cache;
  }
  
  public void run(){
    for(;;){
      try{
        Thread.sleep(1000*60*5);
      }catch(InterruptedException e){
        //no problem, really
      }
      synchronized(cache){
        for(Entry<String, SoftReference<U>> ent : cache.entrySet()){
          if(ent.getValue().get() == null){
            cache.remove(ent.getKey());
          }
        }
      }
    }
  }
  
}