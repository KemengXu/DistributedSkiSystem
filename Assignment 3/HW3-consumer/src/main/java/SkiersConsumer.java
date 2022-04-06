import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SkiersConsumer {
  private final static String SKIER_QUEUE_NAME = "SkierServletPostQueue";
  private final static Integer NUM_THREADS = 512;

//  public static void main(String[] argv) throws Exception {
//    Jedis jedis = new Jedis("54.218.78.14", 6379);
//    System.out.println("Connection to server successfully");
//    //check whether server is running or not
//    System.out.println("Server is running: "+jedis.ping());
//  }

  public static void main(String[] argv) throws Exception {
    Gson gson = new Gson();
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(512);
    JedisPool pool = new JedisPool(poolConfig, "35.85.228.223", 6379);
    ConnectionFactory factory = new ConnectionFactory();

    factory.setHost("35.162.9.219");
    factory.setPort(5672);
    factory.setUsername("xkmmmm");
    factory.setPassword("asdf123");
    Connection connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try (Jedis jedis = pool.getResource()) {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(SKIER_QUEUE_NAME, false, false, false, null);
          // max one message per receiver
          channel.basicQos(1);

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String key = String.valueOf(json.get("skierId"));
            String seasonId = String.valueOf(json.get("seasonID"));
            String dayId = String.valueOf(json.get("dayID"));
            String liftId = String.valueOf(json.get("liftId"));
            String vertical = String.valueOf(json.get("vertical"));
            String info = dayId + "," + seasonId + ","  + liftId + "," + vertical;
            jedis.rpush(key, info);

//            // For skier N, how many days have they skied this season
//            Map<String, String> totalDaysFields = jedis.exists(totalDaysKey)? jedis.hgetAll(totalDaysKey): new HashMap<>();
//            if(totalDaysFields.containsKey(seasonId)){
//              jedis.hset(totalDaysKey, seasonId, String.valueOf(Integer.parseInt(jedis.hget(totalDaysKey, seasonId)) + 1));
//            } else {
//              jedis.hset(totalDaysKey, seasonId, "1");
//            }
////            System.out.println("1: "+jedis.hget(totalDaysKey, seasonId));
//
////            // For skier N, what are the vertical totals for each ski day?
//            Map<String, String> verticalFields = jedis.hgetAll(verticalKey);
//            if(verticalFields.containsKey(dayId)){
//              jedis.hset(verticalKey, dayId, String.valueOf(Integer.parseInt(jedis.hget(verticalKey, dayId)) + Integer.parseInt(vertical)));
//            } else {
//              jedis.hset(verticalKey, dayId, vertical);
//            }
////            System.out.println("2: " + jedis.hget(verticalKey, dayId));
//
//            // For skier N, show me the lifts they rode on each ski day
//            Map<String, String> liftFields = jedis.hgetAll(liftKey);
//            if(liftFields.containsKey(dayId)){
//              jedis.lpush(jedis.hget(liftKey, dayId), liftId);
//            } else {
//              jedis.lpush(key+"-"+dayId, liftId);
//              jedis.hset(liftKey, dayId, key+"-"+dayId);
//            }
////            System.out.println("3: " +jedis.hget(liftKey, dayId));

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
          };
          // process messages
          channel.basicConsume(SKIER_QUEUE_NAME, false, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(SkiersConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };


    List<Thread> arrThreads = new ArrayList<>();
    for (int i = 0; i < NUM_THREADS; i++) {
      Thread cons = new Thread(runnable);
      cons.start();
      arrThreads.add(cons);
    }
    for (int i = 0; i < NUM_THREADS; i++) {
      arrThreads.get(i).join();
    }

    pool.close();
  }
}
