import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectionPoolFactory extends BasePooledObjectFactory<Channel> {

  ConnectionFactory factory = new ConnectionFactory();

  @Override
  public Channel create() throws Exception {
    factory.setHost("35.162.9.219");
    factory.setPort(5672);
    factory.setUsername("xkmmmm");
    factory.setPassword("asdf123");
//    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }

  @Override
  public void destroyObject(PooledObject<Channel> p) throws Exception {
    p.getObject().close();
  }
}
