package com.getfsc.retroserver.session;

import com.getfsc.retroserver.http.ServerRequest;
import com.getfsc.retroserver.http.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午10:28
 */
public class MemorySessionProvider extends SessionProvider {


    public MemorySessionProvider() {
    }

    @Override
    public Session newSession() {
        String id = UUID.randomUUID().toString();
        return newSession(id);
    }

    @Override
    public Session load(ServerRequest req, String value) {
        Session session = sessions.get(value);
        if (session != null)
            req.setObject(Session.class, session);
        return session;
    }

    @Override
    public Session newSession(String id) {
        MemorySession session = new MemorySession(id);
        sessions.put(id, session);
        return session;
    }

    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    private static class MemorySession implements Session {
        private final HashMap<String, Object> data;
        private String id;

        MemorySession(String id) {
            this.id = id;
            this.data = new HashMap<>();
        }

        @Override
        public <T> T get(String key) {
            return (T) data.get(key);
        }

        @Override
        public <T> void set(String key, T value) {
            data.put(key, value);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public void remove(String key) {
            data.remove(key);
        }

        @Override
        public void putAll(Map<String, Object> map) {
            data.putAll(map);
        }

        @Override
        public boolean has(String key) {
            return data.containsKey(key);
        }
    }
}
