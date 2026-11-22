package me.konsolas.aac.util;


import me.konsolas.aac.movement.EntityDimensions;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BoundingBox {
   private final Vector minimum;
   private final Vector maximum;

   public BoundingBox(Vector vector, double d, double d2) {
      double d3 = d / 2.0;
      this.minimum = new Vector(vector.getX() - d3, vector.getY(), vector.getZ() - d3);
      this.maximum = new Vector(vector.getX() + d3, vector.getY() + d2, vector.getZ() + d3);
   }

   public BoundingBox(Vector vector, EntityDimensions entityDimensions) {
      this(vector, entityDimensions.getWidth(), entityDimensions.getHeight());
   }

   public BoundingBox(Location location) {
      this.minimum = new Vector(location.getX(), location.getY(), location.getZ());
      this.maximum = new Vector(location.getX() + 1.0, location.getY() + 1.0, location.getZ() + 1.0);
   }

   public BoundingBox(double d, double d2, double d3, double d4, double d5, double d6) {
      this.minimum = new Vector(d, d2, d3);
      this.maximum = new Vector(d4, d5, d6);
   }

   public BoundingBox clampY(double d, double d2) {
      return new BoundingBox(
         this.minimum.getX(),
         Math.max(d, this.minimum.getY()),
         this.minimum.getZ(),
         this.maximum.getX(),
         Math.min(d2, this.maximum.getY()),
         this.maximum.getZ()
      );
   }

   public BoundingBox expandTowards(Vector vector) {
      return new BoundingBox(
         this.minimum.getX() + (vector.getX() < 0.0 ? vector.getX() : 0.0),
         this.minimum.getY() + (vector.getY() < 0.0 ? vector.getY() : 0.0),
         this.minimum.getZ() + (vector.getZ() < 0.0 ? vector.getZ() : 0.0),
         this.maximum.getX() + (vector.getX() > 0.0 ? vector.getX() : 0.0),
         this.maximum.getY() + (vector.getY() > 0.0 ? vector.getY() : 0.0),
         this.maximum.getZ() + (vector.getZ() > 0.0 ? vector.getZ() : 0.0)
      );
   }

   public BoundingBox expand(Vector vector) {
      return this.expand(vector.getX(), vector.getY(), vector.getZ());
   }

   public BoundingBox expand(double d, double d2, double d3) {
      return new BoundingBox(
         this.minimum.getX() - d,
         this.minimum.getY() - d2,
         this.minimum.getZ() - d3,
         this.maximum.getX() + d,
         this.maximum.getY() + d2,
         this.maximum.getZ() + d3
      );
   }

   public BoundingBox translate(Vector vector) {
      return this.translate(vector.getX(), vector.getY(), vector.getZ());
   }

   public BoundingBox translate(double d, double d2, double d3) {
      return new BoundingBox(
         this.minimum.getX() + d,
         this.minimum.getY() + d2,
         this.minimum.getZ() + d3,
         this.maximum.getX() + d,
         this.maximum.getY() + d2,
         this.maximum.getZ() + d3
      );
   }

   public Vector getBottomCenter() {
      return new Vector((this.minimum.getX() + this.maximum.getX()) / 2.0, this.minimum.getY(), (this.minimum.getZ() + this.maximum.getZ()) / 2.0);
   }

   public BoundingBox withMinX(double d) {
      return new BoundingBox(d, this.minimum.getY(), this.minimum.getZ(), this.maximum.getX(), this.maximum.getY(), this.maximum.getZ());
   }

   public BoundingBox withMinY(double d) {
      return new BoundingBox(this.minimum.getX(), d, this.minimum.getZ(), this.maximum.getX(), this.maximum.getY(), this.maximum.getZ());
   }

   public BoundingBox withMinZ(double d) {
      return new BoundingBox(this.minimum.getX(), this.minimum.getY(), d, this.maximum.getX(), this.maximum.getY(), this.maximum.getZ());
   }

   public BoundingBox withMaxX(double d) {
      return new BoundingBox(this.minimum.getX(), this.minimum.getY(), this.minimum.getZ(), d, this.maximum.getY(), this.maximum.getZ());
   }

   public BoundingBox withMaxY(double d) {
      return new BoundingBox(this.minimum.getX(), this.minimum.getY(), this.minimum.getZ(), this.maximum.getX(), d, this.maximum.getZ());
   }

   public BoundingBox withMaxZ(double d) {
      return new BoundingBox(this.minimum.getX(), this.minimum.getY(), this.minimum.getZ(), this.maximum.getX(), this.maximum.getY(), d);
   }

   public double calculateYOffset(BoundingBox box, double d) {
      if (box.maximum.getX() > this.minimum.getX()
         && box.minimum.getX() < this.maximum.getX()
         && box.maximum.getZ() > this.minimum.getZ()
         && box.minimum.getZ() < this.maximum.getZ()) {
         if (d > 0.0 && box.maximum.getY() <= this.minimum.getY()) {
            double y = this.minimum.getY() - box.maximum.getY();
            if (y < d) {
               d = y;
            }
         } else if (d < 0.0 && box.minimum.getY() >= this.maximum.getY()) {
            double y2 = this.maximum.getY() - box.minimum.getY();
            if (y2 > d) {
               d = y2;
            }
         }

         return d;
      } else {
         return d;
      }
   }

   public double calculateXOffset(BoundingBox box, double d) {
      if (box.maximum.getY() > this.minimum.getY()
         && box.minimum.getY() < this.maximum.getY()
         && box.maximum.getZ() > this.minimum.getZ()
         && box.minimum.getZ() < this.maximum.getZ()) {
         if (d > 0.0 && box.maximum.getX() <= this.minimum.getX()) {
            double x = this.minimum.getX() - box.maximum.getX();
            if (x < d) {
               d = x;
            }
         } else if (d < 0.0 && box.minimum.getX() >= this.maximum.getX()) {
            double x2 = this.maximum.getX() - box.minimum.getX();
            if (x2 > d) {
               d = x2;
            }
         }

         return d;
      } else {
         return d;
      }
   }

   public double calculateZOffset(BoundingBox box, double d) {
      if (box.maximum.getX() > this.minimum.getX()
         && box.minimum.getX() < this.maximum.getX()
         && box.maximum.getY() > this.minimum.getY()
         && box.minimum.getY() < this.maximum.getY()) {
         if (d > 0.0 && box.maximum.getZ() <= this.minimum.getZ()) {
            double z = this.minimum.getZ() - box.maximum.getZ();
            if (z < d) {
               d = z;
            }
         } else if (d < 0.0 && box.minimum.getZ() >= this.maximum.getZ()) {
            double z2 = this.maximum.getZ() - box.minimum.getZ();
            if (z2 > d) {
               d = z2;
            }
         }

         return d;
      } else {
         return d;
      }
   }

   public BoundingBox resolveCollision(Collection<BoundingBox> collection) {
      BoundingBox box = this;

      for (BoundingBox box2 : collection) {
         double d;
         if (this.minimum.getX() < box2.maximum.getX() && this.maximum.getX() > box2.minimum.getX()) {
            double x = box2.minimum.getX() - box.maximum.getX();
            double x2 = box2.maximum.getX() - box.minimum.getX();
            if (Math.abs(x) > Math.abs(x2)) {
               d = x2;
            } else {
               d = x;
            }
         } else {
            d = Double.NaN;
         }

         double d2;
         if (this.minimum.getY() < box2.maximum.getY() && this.maximum.getY() > box2.minimum.getY()) {
            double y = box2.minimum.getY() - box.maximum.getY();
            double y2 = box2.maximum.getY() - box.minimum.getY();
            if (Math.abs(y) > Math.abs(y2)) {
               d2 = y2;
            } else {
               d2 = y;
            }
         } else {
            d2 = Double.NaN;
         }

         double d3;
         if (this.minimum.getZ() < box2.maximum.getZ() && this.maximum.getZ() > box2.minimum.getZ()) {
            double z = box2.minimum.getZ() - box.maximum.getZ();
            double z2 = box2.maximum.getZ() - box.minimum.getZ();
            if (Math.abs(z) > Math.abs(z2)) {
               d3 = z2;
            } else {
               d3 = z;
            }
         } else {
            d3 = Double.NaN;
         }

         boolean flag = Math.abs(d2) < Math.abs(d3);
         boolean flag2 = Math.abs(d) < Math.abs(d3);
         boolean flag3 = Math.abs(d) < Math.abs(d2);
         if (!Double.isNaN(d) || !Double.isNaN(d2) || !Double.isNaN(d3)) {
            if (Double.isNaN(d) && Double.isNaN(d2)) {
               box = box.translate(0.0, 0.0, d3);
            } else if (Double.isNaN(d) && Double.isNaN(d3)) {
               box = box.translate(0.0, d2, 0.0);
            } else if (Double.isNaN(d2) && Double.isNaN(d3)) {
               box = box.translate(d, 0.0, 0.0);
            } else if (Double.isNaN(d)) {
               if (flag) {
                  box = box.translate(0.0, d2, 0.0);
               } else {
                  box = box.translate(0.0, 0.0, d3);
               }
            } else if (Double.isNaN(d2)) {
               if (flag2) {
                  box = box.translate(d, 0.0, 0.0);
               } else {
                  box = box.translate(0.0, 0.0, d3);
               }
            } else if (Double.isNaN(d3)) {
               if (flag3) {
                  box = box.translate(d, 0.0, 0.0);
               } else {
                  box = box.translate(0.0, d2, 0.0);
               }
            } else if (flag3 && flag2) {
               box = box.translate(d, 0.0, 0.0);
            } else if (!flag3 && flag) {
               box = box.translate(0.0, d2, 0.0);
            } else {
               box = box.translate(0.0, 0.0, d3);
            }
         }
      }

      return box;
   }

   public double rayTraceDistance(Vector vector, Vector vector2) {
      double[] dArray = this.rayIntersectionDistances(vector, vector2);
      double d = Math.max(Math.max(Math.min(dArray[0], dArray[1]), Math.min(dArray[2], dArray[3])), Math.min(dArray[4], dArray[5]));
      double d2 = Math.min(Math.min(Math.max(dArray[0], dArray[1]), Math.max(dArray[2], dArray[3])), Math.max(dArray[4], dArray[5]));
      return !(d2 < 0.0) && !(d > d2) ? d : Double.NaN;
   }

   private double[] rayIntersectionDistances(Vector vector, Vector vector2) {
      Vector x = new Vector(1.0 / vector2.getX(), 1.0 / vector2.getY(), 1.0 / vector2.getZ());
      return new double[]{
         (this.minimum.getX() - vector.getX()) * x.getX(),
         (this.maximum.getX() - vector.getX()) * x.getX(),
         (this.minimum.getY() - vector.getY()) * x.getY(),
         (this.maximum.getY() - vector.getY()) * x.getY(),
         (this.minimum.getZ() - vector.getZ()) * x.getZ(),
         (this.maximum.getZ() - vector.getZ()) * x.getZ()
      };
   }

   public BoundingBox union(BoundingBox box) {
      return box == null
         ? this
         : new BoundingBox(
            Math.min(this.minimum.getX(), box.minimum.getX()),
            Math.min(this.minimum.getY(), box.minimum.getY()),
            Math.min(this.minimum.getZ(), box.minimum.getZ()),
            Math.max(this.maximum.getX(), box.maximum.getX()),
            Math.max(this.maximum.getY(), box.maximum.getY()),
            Math.max(this.maximum.getZ(), box.maximum.getZ())
         );
   }

   public boolean isDisjointWith(Collection<BoundingBox> collection) {
      for (BoundingBox box : collection) {
         if (this.intersects(box)) {
            return false;
         }
      }

      return true;
   }

   public boolean intersects(BoundingBox box) {
      return this.minimum.getX() < box.maximum.getX()
         && this.maximum.getX() > box.minimum.getX()
         && this.minimum.getY() < box.maximum.getY()
         && this.maximum.getY() > box.minimum.getY()
         && this.minimum.getZ() < box.maximum.getZ()
         && this.maximum.getZ() > box.minimum.getZ();
   }

   public boolean intersectsWithOffset(BoundingBox box, double d, double d2, double d3) {
      return this.minimum.getX() < box.maximum.getX() + d
         && this.maximum.getX() > box.minimum.getX() + d
         && this.minimum.getY() < box.maximum.getY() + d2
         && this.maximum.getY() > box.minimum.getY() + d2
         && this.minimum.getZ() < box.maximum.getZ() + d3
         && this.maximum.getZ() > box.minimum.getZ() + d3;
   }

   public Vector getMin() {
      return this.minimum.clone();
   }

   public Vector getMax() {
      return this.maximum.clone();
   }

   public double getVolume() {
      return (this.maximum.getX() - this.minimum.getX()) * (this.maximum.getY() - this.minimum.getY()) * (this.maximum.getZ() - this.minimum.getZ());
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder().append("[{");
      Vector vector = this.minimum;
      sb = sb.append(VectorFormatter.formatVector(vector)).append("},{");
      Vector vector2 = this.maximum;
      return sb.append(VectorFormatter.formatVector(vector2)).append("}]").toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof BoundingBox)) {
         return false;
      }

      BoundingBox box = (BoundingBox)obj;
      return this.minimum.equals(box.minimum) && this.maximum.equals(box.maximum);
   }

   @Override
   public int hashCode() {
      return this.minimum.hashCode() ^ this.maximum.hashCode();
   }
}
