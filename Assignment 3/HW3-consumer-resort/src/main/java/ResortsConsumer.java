import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ResortsConsumer {
  private final static String RESORT_QUEUE_NAME = "ResortServletPostQueue";
  private final static Integer NUM_THREADS = 512;

  public static void main(String[] argv) throws Exception {
    Gson gson = new Gson();
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(512);
    JedisPool pool = new JedisPool(poolConfig, "35.80.17.12", 6379);
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
          channel.queueDeclare(RESORT_QUEUE_NAME, false, false, false, null);
          // max one message per receiver
          channel.basicQos(1);

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String skierId = String.valueOf(json.get("skierId"));
            String key = String.valueOf(json.get("dayID"));
            String liftId = String.valueOf(json.get("liftId"));
            String resortId = String.valueOf(json.get("resortID"));
            String info = resortId + "," + skierId + ","  + liftId;
            jedis.rpush(key, info);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
          };
          // process messages
          channel.basicConsume(RESORT_QUEUE_NAME, false, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(ResortsConsumer.class.getName()).log(Level.SEVERE, null, ex);
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
