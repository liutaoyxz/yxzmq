package javax.jms;


/**
 * 连接工程,迎合jms规范
 */
public interface ConnectionFactory {
    /**
     * 创建普通连接
     * @return
     * @throws JMSException
     */
    Connection createConnection() throws JMSException;

    Connection createConnection(String userName, String password)
            throws JMSException;

}
