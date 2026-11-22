package me.konsolas.aac.util;


import me.konsolas.aac.compat.CollisionBoxAccess;
import me.konsolas.aac.movement.EntityDimensions;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class CollisionUtils {
   public static BoundingBox resolveCollisions(BoundingBox box, Vector vector, List<BoundingBox> list) {
      for (BoundingBox box2 : list) {
         if (box.getMax().getX() > box2.getMin().getX()
            && box.getMin().getX() < box2.getMax().getX()
            && box.getMax().getZ() > box2.getMin().getZ()
            && box.getMin().getZ() < box2.getMax().getZ()) {
            if (vector.getY() > 0.0 && box.getMax().getY() <= box2.getMin().getY()) {
               double y = box2.getMin().getY() - box.getMax().getY();
               if (y < vector.getY()) {
                  vector.setY(y);
               }
            } else if (vector.getY() < 0.0 && box.getMin().getY() >= box2.getMax().getY()) {
               double y2 = box2.getMax().getY() - box.getMin().getY();
               if (y2 > vector.getY()) {
                  vector.setY(y2);
               }
            }
         }
      }

      box = box.translate(new Vector(0.0, vector.getY(), 0.0));

      for (BoundingBox box3 : list) {
         if (box.getMax().getY() > box3.getMin().getY()
            && box.getMin().getY() < box3.getMax().getY()
            && box.getMax().getZ() > box3.getMin().getZ()
            && box.getMin().getZ() < box3.getMax().getZ()) {
            if (vector.getX() > 0.0 && box.getMax().getX() <= box3.getMin().getX()) {
               double x = box3.getMin().getX() - box.getMax().getX();
               if (x < vector.getX()) {
                  vector.setX(x);
               }
            } else if (vector.getX() < 0.0 && box.getMin().getX() >= box3.getMax().getX()) {
               double x2 = box3.getMax().getX() - box.getMin().getX();
               if (x2 > vector.getX()) {
                  vector.setX(x2);
               }
            }
         }
      }

      box = box.translate(new Vector(vector.getX(), 0.0, 0.0));

      for (BoundingBox box4 : list) {
         if (box.getMax().getY() > box4.getMin().getY()
            && box.getMin().getY() < box4.getMax().getY()
            && box.getMax().getX() > box4.getMin().getX()
            && box.getMin().getX() < box4.getMax().getX()) {
            if (vector.getZ() > 0.0 && box.getMax().getZ() <= box4.getMin().getZ()) {
               double z = box4.getMin().getZ() - box.getMax().getZ();
               if (z < vector.getZ()) {
                  vector.setZ(z);
               }
            } else if (vector.getZ() < 0.0 && box.getMin().getZ() >= box4.getMax().getZ()) {
               double z2 = box4.getMax().getZ() - box.getMin().getZ();
               if (z2 > vector.getZ()) {
                  vector.setZ(z2);
               }
            }
         }
      }

      return box.translate(new Vector(0.0, 0.0, vector.getZ()));
   }

   public static List<BoundingBox> getCollidingBoxes(Entity entity, Location location, Vector vector) {
      Entity entity2 = entity;
      EntityDimensions entityDimensions = EntityDimensions.forEntity(entity2);
      BoundingBox box = new BoundingBox(location.toVector(), entityDimensions);
      BoundingBox box2 = box.expandTowards(vector);
      Entity entity3 = entity;
      List<BoundingBox> list = CollisionBoxAccess.getCubes(entity3, box2);
      resolveCollisions(box, vector, list);
      return list;
   }

   public static CollisionResult traceCollisions(Entity entity, Location location, Vector vector) {
      Entity entity2 = entity;
      EntityDimensions entityDimensions = EntityDimensions.forEntity(entity2);
      BoundingBox box = new BoundingBox(location.toVector(), entityDimensions);
      BoundingBox box2 = box.expandTowards(vector);
      Entity entity3 = entity;
      List<BoundingBox> list = CollisionBoxAccess.getCubes(entity3, box2);
      Vector vector2 = vector.clone();
      double d = 0.0;
      if (!list.isEmpty()) {
         box = resolveCollisions(box, vector, list);
         double d2 = 0.6;

         for (BoundingBox box3 : list) {
            d2 = box3.calculateYOffset(box, d2);
         }

         BoundingBox box4 = new BoundingBox(location.toVector(), entityDimensions).translate(new Vector(0.0, d2, 0.0));
         Vector vector3 = vector2.clone().setY(0);
         box4 = box4.union(resolveCollisions(box4, vector3, list));
         double d3 = -d2;

         for (BoundingBox box5 : list) {
            d3 = box5.calculateYOffset(box4, d3);
         }

         d = d2 + d3;
      }

      return new CollisionResult(location.getWorld(), box, list, d);
   }
}
