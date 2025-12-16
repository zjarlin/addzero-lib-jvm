package struct;

import java.lang.reflect.Field;
import java.util.HashMap;

public class StructUtils {
   private static HashMap<String, StructData> structInfoCache = new HashMap();

   public static synchronized StructData getStructInfo(Object obj) throws StructException {
      StructData info = (StructData)structInfoCache.get(obj.getClass().getName());
      if (info != null) {
         return info;
      } else if (obj.getClass().getAnnotation(StructClass.class) == null) {
         throw new StructException("No struct Annotation found for " + obj.getClass().getName());
      } else {
         isAccessible(obj);
         Field[] annotatedFields = obj.getClass().getDeclaredFields();
         Field[] tmpStructFields = new Field[annotatedFields.length];
         int annotatedFieldCount = 0;

         for(Field f : annotatedFields) {
            StructField sf = (StructField)f.getAnnotation(StructField.class);
            if (sf != null) {
               int order = sf.order();
               if (order < 0 || order >= annotatedFields.length) {
                  throw new StructException("Order is illegal for StructField : " + f.getName());
               }

               ++annotatedFieldCount;
               tmpStructFields[order] = f;
            }
         }

         Field[] structFields = new Field[annotatedFieldCount];

         for(int i = 0; i < annotatedFieldCount; ++i) {
            if (tmpStructFields[i] == null) {
               throw new StructException("Order error for annotated fields! : " + obj.getClass().getName());
            }

            structFields[i] = tmpStructFields[i];
         }

         info = new StructData(structFields, obj.getClass().getDeclaredMethods());
         structInfoCache.put(obj.getClass().getName(), info);
         return info;
      }
   }

   public static void isAccessible(Object obj) throws StructException {
      int modifiers = obj.getClass().getModifiers();
      if ((modifiers & 1) == 0) {
         throw new StructException("Struct operations are only accessible for public classes. Class: " + obj.getClass().getName());
      } else if ((modifiers & 1536) != 0) {
         throw new StructException("Struct operations are not accessible for abstract classes and interfaces. Class: " + obj.getClass().getName());
      }
   }

   public static boolean requiresGetterSetter(int modifier) {
      return modifier == 0 || (modifier & 6) != 0;
   }
}
