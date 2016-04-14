package com.getfsc.retroserver.session;

import com.getfsc.retroserver.http.Session;

import java.util.HashMap;
import java.util.UUID;

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
        return new MemorySession(UUID.randomUUID().toString());
    }

    @Override
    public Session load(String value) {
        return null;
    }

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
    }
}
