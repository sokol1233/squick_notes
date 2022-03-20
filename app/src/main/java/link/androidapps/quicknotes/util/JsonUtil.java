package link.androidapps.quicknotes.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonUtil {
  private static ObjectMapper objectMapper;

  private JsonUtil(){}
  
  static {
    //TODO: write object mapper settings here
    objectMapper = new ObjectMapper();
  }

  public static String toJson(Object object) throws IOException {
    return objectMapper.writeValueAsString(object);
  }

  public static <T> T toObject(Class<T> clazz, String json) throws IOException {
    return objectMapper.readValue(json, clazz);
  }
}