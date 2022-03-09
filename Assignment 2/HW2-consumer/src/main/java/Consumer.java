import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
  private final static String QUEUE_NAME = "SkierServletPostQueue";
  private final static Integer NUM_THREADS = 512;

  public static void main(String[] argv) throws Exception {
    Gson gson  = new Gson();
    ConnectionFactory factory = new ConnectionFactory();
    ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();

    factory.setHost("52.32.221.65");
    factory.setPort(5672);
    factory.setUsername("xkmmmm");
    factory.setPassword("asdf123");
    Connection connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          // max one message per receiver
          channel.basicQos(1);
//          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            JsonObject json = gson.fromJson(message, JsonObject.class);
            Integer key = Integer.valueOf(String.valueOf(json.get("skierId")));
            if(map.containsKey(key)){
              map.get(key).add(json);
            } else{
              List<JsonObject> value = new ArrayList<>();
              value.add(json);
              map.put(key, value);
            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };

    for (int i = 0; i < NUM_THREADS; i ++){
      Thread cons = new Thread(runnable);
      cons.start();
    }

//    Channel channel = connection.createChannel();
//
//    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//      String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//      System.out.println(" [x] Received '" + message + "'");
//    };
//    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }

}

