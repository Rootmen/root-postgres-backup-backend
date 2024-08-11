package ru.rootmen.backup.backend.websoket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.LaunchMode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import ru.rootmen.backup.backend.exception.RandomString;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/postgres-backup")
public class MainWebSocket {

  private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

  @OnOpen
  public void onOpen(Session session) throws IOException {
    Uni.createFrom()
        .voidItem()
        .onItem()
        .delayIt()
        .by(Duration.ofSeconds(10))
        .onItem()
        .transform(
            Unchecked.function(
                unused -> {
                  if (!sessions.containsValue(session)) {
                    try {
                      session
                          .getBasicRemote()
                          .sendText(
                              "{\"error_message\":true,\"error_message\":\"Not"
                                  + " Authorize after 10s\"}");
                    } catch (Exception ignored) {
                    }
                    try {
                      session.close();
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  }
                  return null;
                }))
        .replaceWithVoid()
        .subscribe()
        .asCompletionStage();
  }

  @OnClose
  public void onClose(Session session) {
    closeSession(session);
  }

  private void closeSession(Session session) {
    for (Map.Entry<String, Session> next : sessions.entrySet()) {
      if (session.getId().equals(next.getValue().getId())) {
        sessions.remove(next.getKey());
        break;
      }
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    ObjectNode json;
    UUID user;
    String token, socket, fingerprint;
    try {
      try {
        json = (ObjectNode) new ObjectMapper().readTree(message);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      if (!sessions.containsValue(session) && json != null) {
        if (json.get("X-Account-id") == null) throw new RuntimeException("X-Account-id not found");
        user = UUID.fromString(json.get("X-Account-id").asText());
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
          if (json.get("X-Socket-id") != null) {
            socket = json.get("X-Socket-id").asText();
          } else {
            socket = RandomString.getRandomString(100);
          }
        } else {
          socket = RandomString.getRandomString(100);
        }
        if (json.get("X-Token") == null) throw new RuntimeException("X-Token not found");
        if (json.get("X-Fingerprint") == null)
          throw new RuntimeException("X-Fingerprint not found");
        token = json.get("X-Token").asText();
        fingerprint = json.get("X-Fingerprint").asText();
        if (sessions.containsKey(socket + ":" + user.toString())) {
          try {
            session.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          return;
        }
        sessions.put(socket + ":" + user, session);
        sendMessage(session, String.format("{\"socket\":\"%s\"}", socket))
            .subscribe()
            .with(unused -> {});
      }
    } catch (Exception e) {
      sendMessage(
              session, String.format("{\"error\":true,\"error_message\":\"%s\"}", e.getMessage()))
          .subscribe()
          .with(unused -> {});
    }
  }

  public static Uni<Void> sendMessage(UUID user, String socket, String message) {
    return sendMessage(sessions.get(socket + ":" + user.toString()), message);
  }

  public static Uni<Void> sendMessage(Session session, String message) {
    if (session == null) return Uni.createFrom().voidItem();
    return Uni.createFrom()
        .future(() -> session.getAsyncRemote().sendText(message))
        .replaceWithVoid();
  }

  public static Uni<Void> sendMessageBroadcast(String message) {
    ArrayList<Uni<Void>> unis = new ArrayList<>();
    sessions
        .values()
        .forEach(
            item -> {
              unis.add(
                  Uni.createFrom()
                      .future(() -> item.getAsyncRemote().sendText(message))
                      .replaceWithVoid());
            });
    if (unis.isEmpty()) {
      return Uni.createFrom().voidItem();
    } else {
      return Uni.join().all(unis).andCollectFailures().replaceWithVoid();
    }
  }
}
