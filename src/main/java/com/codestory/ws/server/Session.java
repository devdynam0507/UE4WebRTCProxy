package com.codestory.ws.server;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Session {

    private static Logger logger = Logger.getLogger("Session");
    private static Map<String, Session> session = new ConcurrentHashMap<>();

    private String id;
    private Channel channel;

    protected Session(Channel channel) {
        this.id = channel.id().asLongText();
        this.channel = channel;
    }

    public Channel getChannel() { return channel; }
    public String getId() { return id; }

    private static void register(Channel channel) {
        final String id = channel.id().asLongText();

        if((channel.isActive() || channel.isOpen()) && session.containsKey(id)) {
            session.put(id, new Session(channel));
            logger.log(Level.INFO, "Registered session id: " + id);
        }
    }

    public static Optional<Session> getSession(Channel channel) {
        return getSession(channel.id().asLongText());
    }

    public static Optional<Session> getSession(String id) {
        return session.containsKey(id) ? Optional.of(session.get(id)) : Optional.empty();
    }

    public static void checkAndRegisterSession(Channel channel) {
        register(channel);
    }

}
