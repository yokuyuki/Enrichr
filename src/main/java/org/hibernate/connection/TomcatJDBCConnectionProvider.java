package org.hibernate.connection;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.service.jdbc.connections.internal.ConnectionProviderInitiator;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* <p>A connection provider that uses the Tomcat JDBC connection pool outside Tomcat container</p>
*
* <p>To use this connection provider set:<br>
* <code>hibernate.connection.provider_class&nbsp;org.hibernate.connection.TomcatJDBCConnectionProvider</code></p>
*
* <pre>Supported Hibernate properties:
*   hibernate.connection.driver_class
*   hibernate.connection.url
*   hibernate.connection.username
*   hibernate.connection.password
*   hibernate.connection.isolation
*   hibernate.connection.autocommit
*   hibernate.connection.pool_size
*   hibernate.connection (JDBC driver properties)</pre>
* <br>
* 
* N.B.: All Tomcat JDBC connection pool properties are also supported by using the hibernate.tomcatJdbcPool prefix.
* 
* @author Guenther Demetz
*/
public class TomcatJDBCConnectionProvider implements ConnectionProvider, Configurable {

   private static final Logger log = LoggerFactory.getLogger(TomcatJDBCConnectionProvider.class);

    private static final String PREFIX = "hibernate.tomcatJdbcPool.";
    private DataSource ds;
    PoolProperties tomcatJdbcPoolProperties;
    

    @Override
    public void configure(Map props) throws HibernateException {
        try {
            log.debug("Configure TomcatJDBCConnectionProvider");

            // Tomcat JDBC connection pool properties used to create theDataSource
            tomcatJdbcPoolProperties = new PoolProperties();

            // DriverClass & url
            String jdbcDriverClass = (String) props.get(Environment.DRIVER);
            String jdbcUrl =  (String)  props.get(Environment.URL);
            tomcatJdbcPoolProperties.setDriverClassName(jdbcDriverClass);
            tomcatJdbcPoolProperties.setUrl(jdbcUrl);
            
            //tomcatJdbcPoolProperties.setJmxEnabled(true); thats the default

            // Username / password
            String username =  (String) props.get(Environment.USER);
            String password =  (String) props.get(Environment.PASS);
            tomcatJdbcPoolProperties.setUsername(username);
            tomcatJdbcPoolProperties.setPassword(password);

            // Isolation level
            String isolationLevel = (String) props.get(Environment.ISOLATION);
            if ((isolationLevel != null) && (isolationLevel.trim().length() > 0)) {
                tomcatJdbcPoolProperties.setDefaultTransactionIsolation(Integer.parseInt(isolationLevel));
            }

//            // Turn off autocommit (unless autocommit property is set)
//          Unfortunately since hibernate3 autocommit defaults to true but usually you don't need if it, when working outside a EJB-container
//            String autocommit = props.getProperty(Environment.AUTOCOMMIT);
//            if ((autocommit != null) && (autocommit.trim().length() > 0)) {
//                tomcatJdbcPoolProperties.setDefaultAutoCommit(Boolean.parseBoolean(autocommit));
//            } else {
//                tomcatJdbcPoolProperties.setDefaultAutoCommit(false);
//            }

            // Pool size
            String poolSize = (String) props.get(Environment.POOL_SIZE);
            if ((poolSize != null) && (poolSize.trim().length() > 0))  {
                tomcatJdbcPoolProperties.setMaxActive(Integer.parseInt(poolSize));
            }

            // Copy all "driver" properties into "connectionProperties"
            // ConnectionProviderInitiator.
            Properties driverProps = ConnectionProviderInitiator.getConnectionProperties(props);
            if (driverProps.size() > 0) {
                StringBuffer connectionProperties = new StringBuffer();
                for (Iterator iter = driverProps.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    connectionProperties.append(key).append('=').append(value);
                    if (iter.hasNext()) {
                        connectionProperties.append(';');
                    }
                }
                tomcatJdbcPoolProperties.setConnectionProperties(connectionProperties.toString());
            }
            
            
          
            // Copy all Tomcat JDBCPool properties removing the prefix
            for (Iterator iter = props.entrySet().iterator() ; iter.hasNext() ;) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                if (key.startsWith(PREFIX)) {
                    String property = key.substring(PREFIX.length());
                    String value = (String) entry.getValue();
                    Method[] methods = PoolConfiguration.class.getMethods();
                    int i;
                    for (i=0; i < methods.length; i++) {
                       if (methods[i].getName().equalsIgnoreCase("set" + property)) {
                          Method m = methods[i];
                          Object parameter = convertIntoTypedValue(m.getParameterTypes()[0], value);
                          try {
                             m.invoke(tomcatJdbcPoolProperties, new Object[]{ parameter });
                          }
                          catch (Exception e) {
                             i = methods.length;
                          }
                          break;
                       }
                    }
                    if (i >= methods.length) {
                       log.error("Unable to parse property " + key + " with value: " + value); 
                     throw new RuntimeException("Unable to parse property " + key + " with value: " + value);
                    }
                }
            }

           

            // Let the factory create the pool
            ds = new DataSource();
            ds.setPoolProperties(tomcatJdbcPoolProperties);
            ds.createPool();
           
            if (ds.getPoolProperties().isJmxEnabled()) {
               MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
               ObjectName objectname = null;
             try {
                objectname = new ObjectName("ConnectionPool:name=" +  tomcatJdbcPoolProperties.getName());
                if (!mBeanServer.isRegistered(objectname)) {
                   mBeanServer.registerMBean(ds.getPool().getJmxPool(), objectname);
                   
                }
               }
             catch (Exception e) {
                e.printStackTrace();
             }
            }

            // Log pool statistics before continuing.
            logStatistics();
        }
        catch (Exception e) {
            String message = "Could not create a TomcatJDBC pool";
            log.error(message, e);
            if (ds != null) {
                try {
                    ds.close();
                }
                catch (Exception e2) {
                    // ignore
                }
                ds = null;
            }
            throw new HibernateException(message, e);
        }
        log.debug("Configure TomcatJDBCConnectionProvider complete");
    }
    
    private Object convertIntoTypedValue (Class clazz, String value) {
       if (clazz.isAssignableFrom(boolean.class)) {
          return Boolean.parseBoolean(value);
       }
       else if (clazz.isAssignableFrom(int.class)) {
          return Integer.parseInt(value);
       }
       else if (clazz.isAssignableFrom(long.class)) {
          return Long.parseLong(value);
       }
       else if (clazz.equals(String.class)) {
          return value;
       }
       else throw new RuntimeException("Unsupported Parameter type " + clazz);
    
    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
                conn = ds.getConnection();
        }
        finally {
            logStatistics();
        }
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        try {
            conn.close();
        }
        finally {
            logStatistics();
        }
    }

    public void close() throws HibernateException {
        log.debug("Close TomcatJDBCConnectionProvider");
        logStatistics();
        try {
            if (ds != null) {
                ds.close();
                    ds = null;
            }
            else {
                log.warn("Cannot close TomcatJDBCConnectionProvider, pool (not initialized)");
            }
        }
        catch (Exception e) {
            throw new HibernateException("Could not close DBCP pool", e);
        }
        log.debug("Close TomcatJDBCConnectionProvider complete");
    }

    protected void logStatistics() {
        if (log.isDebugEnabled()) {
            log.info("active: " + ds.getNumActive() + " (max: " + ds.getMaxActive() + ")   "
                    + "idle: " + ds.getNumIdle() + "(max: " + ds.getMaxIdle() + ")");
        }
    }

    public boolean supportsAggressiveRelease()
    {
        return false;
    }

   @Override
   public boolean isUnwrappableAs(Class unwrapType) {
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> unwrapType) {
      return null;
   }

}