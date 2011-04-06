package sqLiteStor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

class SqLiteKeyValStorBacking{
  
  private static Hashtable<String, SqLiteKeyValStorBacking> instances;
  
  
  protected Connection conn;
  protected LinkedBlockingDeque<KeyValAction> queue;
  
  private SqLiteKeyValStorBacking(String dbFile){
    try{
      Class.forName("org.sqlite.JDBC"); //load and so static setup for the driver
      this.conn =  DriverManager.getConnection("jdbc:sqlite:" + dbFile);
      conn.setAutoCommit(false);
      this.queue = new LinkedBlockingDeque<KeyValAction>();
      new DBQueueRunner(queue, conn).start();
    }catch(Exception e){
      System.err.println("No sqlite driver found!");
      e.printStackTrace();
    }
    
    Runtime.getRuntime().addShutdownHook(new FlushQueue(this)); //Flush the queue on shutdown
  }
  
  public static synchronized SqLiteKeyValStorBacking getInstance(String dbFile){
    SqLiteKeyValStorBacking fromHash = instances.get(dbFile);
    if(!(fromHash == null)){
      return fromHash;
    }else{
      SqLiteKeyValStorBacking newBacking = new SqLiteKeyValStorBacking(dbFile);
      instances.put(dbFile, newBacking);
      return newBacking;
    }
  }
  
  static{
    instances = new Hashtable<String, SqLiteKeyValStorBacking>();
  }
  
  public Serializable get(String key, String group){
    try{
      PreparedStatement prep = this.conn.prepareStatement("SELECT value FROM keyval WHERE key = ? AND collection = ?");
      prep.setString(1, key);
      prep.setString(2, group);
      ResultSet rs = prep.executeQuery(); 
      if(!rs.next()){
        return null;
      }
      ObjectInputStream objStr = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes(1)));
      rs.close();
      Serializable obj = (Serializable)objStr.readObject();
      return obj;
    }catch(SQLException e){
      e.printStackTrace();
      return null;
    }catch(IOException e){
      e.printStackTrace();
      return null;
    }catch(ClassNotFoundException e){
      e.printStackTrace();
      return null;
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void getAllInGroup(String group, Map target){
    try{
      String key;
      ObjectInputStream objStr;
      Serializable obj;
      
      PreparedStatement prep = this.conn.prepareStatement("SELECT key, value FROM keyval WHERE collection = ?");
      prep.setString(1, group);
      ResultSet rs = prep.executeQuery(); 
      while(rs.next()){
        key = rs.getString(1);
        objStr = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes(2)));
        obj = (Serializable)objStr.readObject();
        target.put(key, obj);
      }
      rs.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public ArrayList<String> getAllKeysInGroup(String group){
    ResultSet rs;
    try{
      String key;
      ArrayList<String> keys = new ArrayList<String>();
      
      PreparedStatement prep = this.conn.prepareStatement("SELECT key FROM keyval WHERE collection = ?");
      prep.setString(1, group);
      rs = prep.executeQuery(); 
      while(rs.next()){
        key = rs.getString(1);
        keys.add(key);
      }
      rs.close();
      return keys;
    }catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Blocks until the queue is empty.
   * <br>
   * Note that notifies are sent, at most, once every 100ms.
   * When the queue is active notifies only be sent when the queue is empty.
   * Note that because of this there is a chance the wait could be much longer.
   * <p>
   * Be sure to externally synchronize any methods that might add to the queue that you want to flush
   * Since the queue can be shared other things might add to it, but if everything makes sure that it's own data is synced
   * guarantees about data durability can be made. 
   * 
   */
  
  public void flush(){
    System.err.println("Flushing the DB...");
    for(;;){
      try{
        synchronized(this.queue){
          this.queue.wait();
          System.err.println("Flushed");
          return;
        }
      }catch(InterruptedException e){
        //if we are interrupted go back to waiting some more
        //This is not a problem
      }
    }
  }
  
}


class DBQueueRunner extends Thread{
  
  private LinkedBlockingDeque<KeyValAction> queue;
  Connection conn;
  public DBQueueRunner(LinkedBlockingDeque<KeyValAction> queue, Connection conn){
    this.queue = queue;
    this.conn = conn;
  }
  
  public void run(){
    System.out.println("DB Queue runner started");
    KeyValAction action;
    PreparedStatement prepInsert = null;
    PreparedStatement prepDelete = null;
    /*
     * The following construct deserves some explanation.
     * 
     * While there are still items in the queue we will loop tightly
     * When the queue is empty we loop waiting in 100ms intervals for something to be placed on the queue, sending notifies to anything waiting to let them know the queue is empty.
     * When something is added to the queue we do not send a notify until it is empty again 
     */
    try{
      prepInsert = this.conn.prepareStatement("INSERT OR REPLACE INTO keyval (key, collection, value) VALUES (?, ?, ?)");
      prepDelete = this.conn.prepareStatement("DELETE FROM keyval WHERE key = ? AND collection = ?");
      
      while(true){
        synchronized(this.queue){
          this.queue.notifyAll(); // let any waiters know that the queue should be empty
        }
        if((action = this.queue.poll(100, TimeUnit.MILLISECONDS)) == null){
          continue;
        }
        synchronized(this.queue){
          do{
            if(action.action.equals(KVActions.PUT)){
              prepInsert.setString(1, action.key);
              prepInsert.setString(2, action.group);
              prepInsert.setBytes(3, action.value.toByteArray());
              prepInsert.executeUpdate();
            }else{
              prepDelete.setString(1, action.key);
              prepDelete.setString(2, action.group);
              prepDelete.executeUpdate();
            }
          }while((action = this.queue.poll()) != null);
          //For speed only commit when we are caught up and only after we wrote something.
          //If the load is so high that we can never commit performance will be in the HOLE anyway.
          this.conn.commit();
        }
      }
    }catch(InterruptedException e){
      e.printStackTrace();
    }catch(SQLException e){
      e.printStackTrace();
    }
    System.err.println("VERY BAD! DB Queue runner died! Cannot commit to DB anymore!");
  }
}

class FlushQueue extends Thread{
  private SqLiteKeyValStorBacking backing;
  FlushQueue(SqLiteKeyValStorBacking backing){
    this.backing = backing;
  }
  
  public void run(){
    this.backing.flush();
  }
}
