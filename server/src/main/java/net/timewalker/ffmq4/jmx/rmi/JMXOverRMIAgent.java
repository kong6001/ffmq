/*
 * This file is part of FFMQ.
 *
 * FFMQ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FFMQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FFMQ; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.timewalker.ffmq4.jmx.rmi;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.jmx.AbstractJMXAgent;

/**
 * JMXAgent
 */
public final class JMXOverRMIAgent extends AbstractJMXAgent
{
    // Attributes
    private String agentName;
    private int jndiRmiPort;
    private String rmiListenAddr;
    
    // Runtime
    private JMXConnectorServer connectorServer;
    private JMXOverRMIServerSocketFactory mBeanServerSocketFactory;
    private Registry registry;
    
    /**
     * Constructor
     */
    public JMXOverRMIAgent( String agentName , int jndiRmiPort , String rmiListenAddr ) throws JMSException
    {
    	this.agentName = agentName;
    	this.jndiRmiPort = jndiRmiPort;
    	this.rmiListenAddr = rmiListenAddr;
        init();
    }
    
    private void init() throws JMSException
    {
        try
        {
            // Get or create an RMI registry
            if (rmiListenAddr == null || rmiListenAddr.equals("auto"))
                rmiListenAddr = InetAddress.getLocalHost().getHostName();
            
            // Connector JNDI name
            String jndiName = "jmxconnector-"+agentName;
            
            try
            {
                registry = LocateRegistry.getRegistry(rmiListenAddr,jndiRmiPort);
                registry.lookup(jndiName);
                
                // Remove the old registered connector
                registry.unbind(jndiName);
                
                log.debug("RMI registry found at "+rmiListenAddr+":"+jndiRmiPort+" with connector already registered");
            }
            catch (NotBoundException e)
            {
                // Registry already exists
                log.debug("RMI registry found at "+rmiListenAddr+":"+jndiRmiPort);
            }
            catch (RemoteException e)
            {
                log.debug("Creating RMI registry at "+rmiListenAddr+":"+jndiRmiPort);
                RMIServerSocketFactory ssf = new JMXOverRMIServerSocketFactory(10,rmiListenAddr,false);
                registry = LocateRegistry.createRegistry(jndiRmiPort,null,ssf);
            }

            // Service URL
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://"+rmiListenAddr+"/jndi/rmi://"+rmiListenAddr+":" + jndiRmiPort + "/"+ jndiName);
            log.info("JMX Service URL : "+url);
            
            // Create and start the RMIConnectorServer
            Map<String,Object> env = new HashMap<>();
            mBeanServerSocketFactory = new JMXOverRMIServerSocketFactory(10,rmiListenAddr,true);
            env.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
            //env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new JMXRMIClientSocketFactory(rmiListenAddr));
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, mBeanServerSocketFactory);
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mBeanServer);
            connectorServer.start();
        }
        catch (Exception e)
        {
            throw new FFMQException("Could not initialize JMX agent","JMX_ERROR",e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.jmx.AbstractJMXAgent#getType()
     */
    @Override
    protected String getType()
    {
    	return "RMI";
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.jmx.JMXAgent#stop()
     */
    @Override
	public void stop()
    {
        super.stop();
        if (connectorServer != null)
        {
            try
            {
                connectorServer.stop();
            }
            catch (Exception e)
            {
                log.error("Could not stop JMX connector server",e);
            }
            finally
            {
            	connectorServer = null;
            }
        }
        if (registry != null)
        {
	        try
	        {
	        	String jndiName = "jmxconnector-"+agentName;
	            registry.unbind(jndiName);
	        }
	        catch (Exception e)
	        {
	        	// Ignore
	        }
	        finally
	        {
	        	registry = null;
	        }
        }
        if (mBeanServerSocketFactory != null)
        {
        	try
            {
        		mBeanServerSocketFactory.close();
            }
        	catch (Exception e)
            {
                log.error("Could not close MBeans server socket factory",e);
            }
        	finally
            {
        		mBeanServerSocketFactory = null;
            }
        }
    }
}
