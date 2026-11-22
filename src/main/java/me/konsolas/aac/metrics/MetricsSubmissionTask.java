package me.konsolas.aac.metrics;

import com.google.gson.JsonObject;
import java.util.logging.Level;

public class MetricsSubmissionTask implements Runnable {
   public final JsonObject jsonObjectValue;
   public final MetricsLite metricsLiteValue;

   public MetricsSubmissionTask(MetricsLite metricsLite, JsonObject jsonObject) {
      this.metricsLiteValue = metricsLite;
      this.jsonObjectValue = jsonObject;
   }

   @Override
   public void run() {
      try {
         MetricsLite.sendData(MetricsLite.getPlugin(this.metricsLiteValue), this.jsonObjectValue);
      } catch (Exception ex) {
         if (MetricsLite.isLogFailedRequests()) {
            MetricsLite.getPlugin(this.metricsLiteValue)
               .getLogger()
               .log(Level.WARNING, "Could not submit plugin stats of " + MetricsLite.getPlugin(this.metricsLiteValue).getName(), ex);
         }
      }
   }
}
