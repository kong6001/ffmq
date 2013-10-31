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
package net.timewalker.ffmq3.local.session;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Topic specific implementation of a local {@link MessageProducer}</p>
 * @see TopicPublisher
 */
public final class LocalTopicPublisher extends LocalMessageProducer implements TopicPublisher
{
    /**
     * Constructor
     */
    public LocalTopicPublisher(LocalSession session, Destination destination, IntegerID publisherId) throws JMSException
    {
        super(session, destination, publisherId);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicPublisher#getTopic()
     */
    @Override
	public final Topic getTopic()
    {
        return (Topic)destination;
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicPublisher#publish(javax.jms.Message)
     */
    @Override
	public final void publish(Message message) throws JMSException
    {
        send(message);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicPublisher#publish(javax.jms.Topic, javax.jms.Message)
     */
    @Override
	public final void publish(Topic topic, Message message) throws JMSException
    {
        send(topic,message);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicPublisher#publish(javax.jms.Message, int, int, long)
     */
    @Override
	public final void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        send(message,deliveryMode,priority,timeToLive);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.TopicPublisher#publish(javax.jms.Topic, javax.jms.Message, int, int, long)
     */
    @Override
	public final void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        send(topic, message, deliveryMode, priority, timeToLive);
    }
}
