package me.konsolas.aac.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class MapValueSorter {
   public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
      LinkedList<Entry<K, V>> linkedList = new LinkedList<>(map.entrySet());
      linkedList.sort(Comparator.comparing(Entry::getValue));
      LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();

      for (Entry<K, V> entry : linkedList) {
         linkedHashMap.put(entry.getKey(), entry.getValue());
      }

      return linkedHashMap;
   }
}
