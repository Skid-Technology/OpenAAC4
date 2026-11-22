package me.konsolas.aac.util;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class RollingDoubleWindow {
   private final double[] samples;
   private int index;
   private boolean filled;

   public RollingDoubleWindow(int i) {
      this.samples = new double[i];
   }

   public void addSample(double d) {
      this.samples[this.index++] = d;
      if (this.index >= this.samples.length) {
         this.index = 0;
         this.filled = true;
      }
   }

   public DoubleStream stream() {
      return this.filled ? Arrays.stream(this.samples) : Arrays.stream(this.samples).limit(this.index);
   }

   public boolean isFilled() {
      return this.filled;
   }

   public void reset() {
      this.index = 0;
      Arrays.fill(this.samples, 0.0);
      this.filled = false;
   }
}
