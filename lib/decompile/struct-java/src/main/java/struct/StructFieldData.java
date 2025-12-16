package struct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StructFieldData {
   private Field field;
   private boolean requiresGetterSetter = false;
   private Method getter;
   private Method setter;
   private boolean arrayLengthMarker = false;
   private Constants.Primitive type;

   public StructFieldData(Field field, boolean requiresGetterSetter, Method getter, Method setter, Constants.Primitive type, boolean isArrayLenghtMarker, Field lengthDefinedArray) {
      this.field = field;
      this.requiresGetterSetter = requiresGetterSetter;
      this.getter = getter;
      this.setter = setter;
      this.type = type;
   }

   public StructFieldData(Field field) {
      this.field = field;
   }

   public Field getField() {
      return this.field;
   }

   public void setField(Field field) {
      this.field = field;
   }

   public boolean requiresGetterSetter() {
      return this.requiresGetterSetter;
   }

   public void setRequiresGetterSetter(boolean requiresGetterSetter) {
      this.requiresGetterSetter = requiresGetterSetter;
   }

   public Method getGetter() {
      return this.getter;
   }

   public void setGetter(Method getter) {
      this.getter = getter;
   }

   public Method getSetter() {
      return this.setter;
   }

   public void setSetter(Method setter) {
      this.setter = setter;
   }

   public boolean isArrayLengthMarker() {
      return this.arrayLengthMarker;
   }

   public void setArrayLengthMarker(boolean arrayLengthMarker) {
      this.arrayLengthMarker = arrayLengthMarker;
   }

   public Constants.Primitive getType() {
      return this.type;
   }

   public void setType(Constants.Primitive type) {
      this.type = type;
   }

   public boolean isRequiresGetterSetter() {
      return this.requiresGetterSetter;
   }
}
