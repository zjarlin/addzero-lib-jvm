package struct;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Constants {
   private static HashMap<String, Primitive> primitiveTypes = new HashMap();
   private static HashMap<Character, Primitive> signatures = new HashMap();

   public static final Primitive getPrimitive(Field field) {
      return !field.getType().isArray() ? getPrimitive(field.getType().getName()) : getPrimitive(field.getType().getName().charAt(1));
   }

   public static final Primitive getPrimitive(String name) {
      Primitive p = (Primitive)primitiveTypes.get(name);
      return p != null ? p : Primitive.OBJECT;
   }

   public static final Primitive getPrimitive(char signature) {
      Primitive p = (Primitive)signatures.get(signature);
      return p != null ? p : Primitive.OBJECT;
   }

   static {
      for(Primitive p : Primitive.values()) {
         primitiveTypes.put(p.type, p);
         signatures.put(p.signature, p);
      }

   }

   public static enum Primitive {
      BOOLEAN("boolean", 'Z', 0),
      BYTE("byte", 'B', 1),
      CHAR("char", 'C', 2),
      SHORT("short", 'S', 3),
      INT("int", 'I', 4),
      LONG("long", 'J', 5),
      FLOAT("float", 'F', 6),
      DOUBLE("double", 'D', 7),
      OBJECT("object", 'O', 8);

      String type;
      char signature;
      int order;

      private Primitive(String type, char signature, int order) {
         this.type = type;
         this.signature = signature;
         this.order = order;
      }
   }
}
