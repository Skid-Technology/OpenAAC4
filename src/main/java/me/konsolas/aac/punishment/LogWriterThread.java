package me.konsolas.aac.punishment;


import java.io.PrintWriter;

public class LogWriterThread extends Thread {
   public final ViolationLogger violationLoggerValue;

   public LogWriterThread(ViolationLogger violationLogger, String str) {
      super(str);
      this.violationLoggerValue = violationLogger;
   }

   @Override
   public void run() {
      try {
         while (true) {
            String str = (String)ViolationLogger.getPendingLines(this.violationLoggerValue).take();
            PrintWriter printWriter = ViolationLogger.getWriter(this.violationLoggerValue);
            String str2 = str;
            PrintWriter printWriter2 = printWriter;
            printWriter2.println(str2);
         }
      } catch (InterruptedException ex) {
         ViolationLogger.getPlugin(this.violationLoggerValue).getLogger().info("Thread AAC-LogWriter exiting");
      }
   }
}
