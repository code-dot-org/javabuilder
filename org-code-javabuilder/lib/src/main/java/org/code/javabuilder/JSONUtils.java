package org.code.javabuilder;

import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

/** Convenience methods for handling and converting data in JSON objects */
public class JSONUtils {

  /**
   * Finds the JSONArray in the JSONObject indicated by the given key and converts it to a list of
   * type T, or returns null if no such array is found.
   *
   * @param jsonObject the object in which to find the array
   * @param key the key pointing to the array in the JSONObject
   * @param <T> the type to convert the list members to
   * @return a list representing the JSONArray, or null if not found
   */
  public static <T> List<T> listFromJSONObjectMember(JSONObject jsonObject, String key) {
    if (jsonObject.has(key) && jsonObject.get(key) instanceof JSONArray) {
      return jsonArrayToList(jsonObject.getJSONArray(key));
    }

    return null;
  }

  /**
   * Finds the boolean in the JSONObject indicated by the given key, or returns false if not found
   *
   * @param jsonObject the object in which to find the boolean
   * @param key the key pointing to the stringified boolean value
   * @return a boolean representing the value, or false if not found
   */
  public static boolean booleanFromJSONObjectMember(JSONObject jsonObject, String key) {
    if (jsonObject.has(key) && jsonObject.get(key) instanceof String) {
      return Boolean.parseBoolean(jsonObject.getString(key));
    }

    return false;
  }

  /**
   * Converts the JSONArray into a list of items of type T. It is expected that the items can be
   * safely cast to the given type (typically "String")
   *
   * @param array JSONArray to convert
   * @param <T> the type to convert the list members to
   * @return a list representing the JSONArray
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> jsonArrayToList(JSONArray array) {
    return array.toList().stream().map(item -> (T) item).collect(Collectors.toList());
  }
}
