package struct;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class StructOutput extends OutputStream {
   public void write(int arg0) throws IOException {
   }

   public void writeObject(Object obj) throws StructException {
      if (obj == null) {
         throw new StructException("Struct classes cant be null. ");
      } else {
         StructData info = StructUtils.getStructInfo(obj);
         boolean lengthedArray = false;
         int arrayLength = 0;

         for(Field currentField : info.getFields()) {
            StructFieldData fieldData = info.getFieldData(currentField.getName());
            if (fieldData == null) {
               throw new StructException("Field Data not found for field: " + currentField.getName());
            }

            lengthedArray = false;
            arrayLength = 0;

            try {
               if (fieldData.isArrayLengthMarker()) {
                  if (fieldData.requiresGetterSetter()) {
                     arrayLength = ((Number)fieldData.getGetter().invoke(obj, (Object[])null)).intValue();
                  } else {
                     arrayLength = ((Number)fieldData.getField().get(obj)).intValue();
                  }

                  lengthedArray = true;
               }

               if (fieldData.requiresGetterSetter()) {
                  if (lengthedArray && arrayLength >= 0) {
                     this.writeField(fieldData, fieldData.getGetter(), obj, arrayLength);
                  } else {
                     this.writeField(fieldData, fieldData.getGetter(), obj, -1);
                  }
               } else if (lengthedArray && arrayLength >= 0) {
                  this.writeField(fieldData, (Method)null, obj, arrayLength);
               } else {
                  this.writeField(fieldData, (Method)null, obj, -1);
               }
            } catch (Exception e) {
               throw new StructException(e);
            }
         }

      }
   }

   public void writeField(StructFieldData fieldData, Method getter, Object obj, int len) throws IllegalAccessException, IOException, InvocationTargetException, StructException {
      Field field = fieldData.getField();
      if (!field.getType().isArray()) {
         switch (fieldData.getType()) {
            case BOOLEAN:
               if (getter != null) {
                  this.writeBoolean((Boolean)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeBoolean(field.getBoolean(obj));
               }
               break;
            case BYTE:
               if (getter != null) {
                  this.writeByte((Byte)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeByte(field.getByte(obj));
               }
               break;
            case SHORT:
               if (getter != null) {
                  this.writeShort((Short)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeShort(field.getShort(obj));
               }
               break;
            case INT:
               if (getter != null) {
                  this.writeInt((Integer)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeInt(field.getInt(obj));
               }
               break;
            case LONG:
               long longValue;
               if (getter != null) {
                  longValue = (Long)getter.invoke(obj, (Object[])null);
               } else {
                  longValue = field.getLong(obj);
               }

               this.writeLong(longValue);
               break;
            case CHAR:
               if (getter != null) {
                  this.writeChar((Character)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeChar(field.getChar(obj));
               }
               break;
            case FLOAT:
               if (getter != null) {
                  this.writeFloat((Float)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeFloat(field.getFloat(obj));
               }
               break;
            case DOUBLE:
               if (getter != null) {
                  this.writeDouble((Double)getter.invoke(obj, (Object[])null));
               } else {
                  this.writeDouble(field.getDouble(obj));
               }
               break;
            default:
               if (getter != null) {
                  this.handleObject(field, getter.invoke(obj, (Object[])null));
               } else {
                  this.handleObject(field, obj);
               }
         }
      } else {
         switch (fieldData.getType()) {
            case BOOLEAN:
               if (getter != null) {
                  this.writeBooleanArray((boolean[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeBooleanArray((boolean[])field.get(obj), len);
               }
               break;
            case BYTE:
               if (getter != null) {
                  this.writeByteArray((byte[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeByteArray((byte[])field.get(obj), len);
               }
               break;
            case SHORT:
               if (getter != null) {
                  this.writeShortArray((short[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeShortArray((short[])field.get(obj), len);
               }
               break;
            case INT:
               if (getter != null) {
                  this.writeIntArray((int[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeIntArray((int[])field.get(obj), len);
               }
               break;
            case LONG:
               if (getter != null) {
                  this.writeLongArray((long[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeLongArray((long[])field.get(obj), len);
               }
               break;
            case CHAR:
               if (getter != null) {
                  this.writeCharArray((char[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeCharArray((char[])field.get(obj), len);
               }
               break;
            case FLOAT:
               if (getter != null) {
                  this.writeFloatArray((float[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeFloatArray((float[])field.get(obj), len);
               }
               break;
            case DOUBLE:
               if (getter != null) {
                  this.writeDoubleArray((double[])getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeDoubleArray((double[])field.get(obj), len);
               }
               break;
            default:
               if (getter != null) {
                  this.writeObjectArray(getter.invoke(obj, (Object[])null), len);
               } else {
                  this.writeObjectArray(field.get(obj), len);
               }
         }
      }

   }

   public void handleObject(Field field, Object obj) throws IllegalArgumentException, StructException, IllegalAccessException, IOException {
      this.writeObject(field.get(obj));
   }

   public abstract void writeBoolean(boolean var1) throws IOException;

   public abstract void writeByte(byte var1) throws IOException;

   public abstract void writeShort(short var1) throws IOException;

   public abstract void writeInt(int var1) throws IOException;

   public abstract void writeLong(long var1) throws IOException;

   public abstract void writeChar(char var1) throws IOException;

   public abstract void writeFloat(float var1) throws IOException;

   public abstract void writeDouble(double var1) throws IOException;

   public abstract void writeBooleanArray(boolean[] var1, int var2) throws IOException;

   public abstract void writeByteArray(byte[] var1, int var2) throws IOException;

   public abstract void writeCharArray(char[] var1, int var2) throws IOException;

   public abstract void writeShortArray(short[] var1, int var2) throws IOException;

   public abstract void writeIntArray(int[] var1, int var2) throws IOException;

   public abstract void writeLongArray(long[] var1, int var2) throws IOException;

   public abstract void writeFloatArray(float[] var1, int var2) throws IOException;

   public abstract void writeDoubleArray(double[] var1, int var2) throws IOException;

   public abstract void writeObjectArray(Object[] var1, int var2) throws IOException, IllegalAccessException, InvocationTargetException, StructException;
}
