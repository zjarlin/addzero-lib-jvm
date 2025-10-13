package struct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class StructData {
   Field[] fields = null;
   Method[] methods = null;
   HashMap<String, Field> lengthedArrayFields = new HashMap();
   HashMap<String, StructFieldData> fieldDataMap = new HashMap();
   static int ACCEPTED_MODIFIERS = 7;

   public StructData(Field[] fields, Method[] methods) throws StructException {
      this.fields = fields;
      this.methods = methods;

      for(Field field : fields) {
         if ((field.getModifiers() & ~ACCEPTED_MODIFIERS) != 0 || (field.getModifiers() | ACCEPTED_MODIFIERS) == 0) {
            throw new StructException("Field type should be public, private or protected : " + field.getName());
         }

         StructFieldData fieldData = new StructFieldData(field);
         ArrayLengthMarker lengthMarker = (ArrayLengthMarker)field.getAnnotation(ArrayLengthMarker.class);
         if (lengthMarker != null) {
            fieldData.setArrayLengthMarker(true);

            int i;
            for(i = 0; i < fields.length; ++i) {
               if (lengthMarker.fieldName().equals(fields[i].getName())) {
                  this.lengthedArrayFields.put(fields[i].getName(), field);
                  break;
               }
            }

            if (i == fields.length) {
               throw new StructException("Lenght Marker Fields target is not found: " + lengthMarker.fieldName());
            }
         }

         if (StructUtils.requiresGetterSetter(field.getModifiers())) {
            fieldData.setGetter(getGetterName(methods, field));
            fieldData.setSetter(getSetterName(methods, field));
            fieldData.setRequiresGetterSetter(true);
         }

         fieldData.setType(Constants.getPrimitive(field));
         this.fieldDataMap.put(field.getName(), fieldData);
      }

   }

   public StructFieldData getFieldData(String fieldName) {
      return (StructFieldData)this.fieldDataMap.get(fieldName);
   }

   private static final Method getGetterName(Method[] methods, Field field) throws StructException {
      String getterName = "get" + field.getName();
      String booleanGetterName = "is" + field.getName();

      for(Method method : methods) {
         if (method.getName().equalsIgnoreCase(getterName)) {
            return method;
         }
      }

      if (field.getType().getName().equals("boolean")) {
         for(Method method : methods) {
            if (method.getName().equalsIgnoreCase(booleanGetterName)) {
               return method;
            }
         }
      }

      throw new StructException("The field needs a getter method, but none supplied. Field: " + field.getName());
   }

   private static final Method getSetterName(Method[] methods, Field field) throws StructException {
      String setterName = "set" + field.getName();

      for(int i = 0; i < methods.length; ++i) {
         if (methods[i].getName().equalsIgnoreCase(setterName)) {
            return methods[i];
         }
      }

      throw new StructException("The field needs a setter method, but none supplied. Field: " + field.getName());
   }

   public Field[] getFields() {
      return this.fields;
   }

   public Method[] getMethods() {
      return this.methods;
   }

   public boolean isLenghtedArray(Field field) {
      return this.lengthedArrayFields.get(field.getName()) != null;
   }

   public Field getLenghtedArray(String fieldName) {
      return (Field)this.lengthedArrayFields.get(fieldName);
   }
}
