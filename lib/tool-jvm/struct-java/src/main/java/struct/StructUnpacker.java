package struct;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public class StructUnpacker extends StructInput {
   DataInput dataInput;

   protected void init(InputStream inStream, ByteOrder order) {
      if (order == ByteOrder.LITTLE_ENDIAN) {
         this.dataInput = new LEDataInputStream(inStream);
      } else {
         this.dataInput = new DataInputStream(inStream);
      }

   }

   public StructUnpacker(byte[] bufferToUnpack) {
      this((InputStream)(new ByteArrayInputStream(bufferToUnpack)), ByteOrder.BIG_ENDIAN);
   }

   public StructUnpacker(byte[] bufferToUnpack, ByteOrder order) {
      this((InputStream)(new ByteArrayInputStream(bufferToUnpack)), order);
   }

   public StructUnpacker(InputStream is, ByteOrder order) {
      this.init(is, order);
   }

   public void unpack(Object objectToUnpack) throws StructException {
      this.readObject(objectToUnpack);
   }

   public void readObject(Object obj) throws StructException {
      if (obj == null) {
         throw new StructException("Struct objects cannot be null.");
      } else {
         StructData info = StructUtils.getStructInfo(obj);
         Field[] fields = info.getFields();

         for(Field currentField : fields) {
            StructFieldData fieldData = info.getFieldData(currentField.getName());
            if (fieldData == null) {
               throw new StructException("Field Data not found for field: " + currentField.getName());
            }

            int arrayLength = -1;
            boolean lengthedArray = false;

            try {
               if (info.isLenghtedArray(currentField)) {
                  Field f = info.getLenghtedArray(currentField.getName());
                  StructFieldData lengthMarker = info.getFieldData(f.getName());
                  if (lengthMarker.requiresGetterSetter()) {
                     arrayLength = ((Number)lengthMarker.getGetter().invoke(obj, (Object[])null)).intValue();
                  } else {
                     arrayLength = ((Number)lengthMarker.getField().get(obj)).intValue();
                  }

                  lengthedArray = true;
               }

               if (fieldData.requiresGetterSetter()) {
                  Method getter = fieldData.getGetter();
                  Method setter = fieldData.getSetter();
                  if (getter == null || setter == null) {
                     throw new StructException(" getter/setter required for : " + currentField.getName());
                  }

                  if (lengthedArray && arrayLength >= 0) {
                     Object ret = Array.newInstance(currentField.getType().getComponentType(), arrayLength);
                     setter.invoke(obj, ret);
                     if (!currentField.getType().getComponentType().isPrimitive()) {
                        Object[] array = ret;

                        for(int j = 0; j < arrayLength; ++j) {
                           array[j] = currentField.getType().getComponentType().newInstance();
                        }
                     }
                  }

                  if (!lengthedArray && currentField.getType().isArray() && getter.invoke(obj, (Object[])null) == null) {
                     throw new StructException("Arrays can not be null :" + currentField.getName());
                  }

                  this.readField(fieldData, getter, setter, obj);
               } else {
                  if (lengthedArray && arrayLength >= 0) {
                     Object ret = Array.newInstance(currentField.getType().getComponentType(), arrayLength);
                     currentField.set(obj, ret);
                     if (!currentField.getType().getComponentType().isPrimitive()) {
                        Object[] array = ret;

                        for(int j = 0; j < arrayLength; ++j) {
                           array[j] = currentField.getType().getComponentType().newInstance();
                        }
                     }
                  }

                  if (!lengthedArray && currentField.getType().isArray() && currentField.get(obj) == null) {
                     throw new StructException("Arrays can not be null. : " + currentField.getName());
                  }

                  if (!lengthedArray || lengthedArray && arrayLength >= 0) {
                     this.readField(fieldData, (Method)null, (Method)null, obj);
                  }
               }
            } catch (Exception e) {
               throw new StructException(e);
            }
         }

      }
   }

   protected boolean readBoolean() throws IOException {
      return this.dataInput.readBoolean();
   }

   protected byte readByte() throws IOException {
      return this.dataInput.readByte();
   }

   protected short readShort() throws IOException {
      return this.dataInput.readShort();
   }

   protected int readInt() throws IOException {
      return this.dataInput.readInt();
   }

   protected long readLong() throws IOException {
      return this.dataInput.readLong();
   }

   protected char readChar() throws IOException {
      return this.dataInput.readChar();
   }

   protected float readFloat() throws IOException {
      return this.dataInput.readFloat();
   }

   protected double readDouble() throws IOException {
      return this.dataInput.readDouble();
   }

   protected void readBooleanArray(boolean[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readBoolean();
      }

   }

   protected void readByteArray(byte[] buffer) throws IOException {
      this.dataInput.readFully(buffer);
   }

   protected void readCharArray(char[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readChar();
      }

   }

   protected void readShortArray(short[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readShort();
      }

   }

   protected void readIntArray(int[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readInt();
      }

   }

   protected void readLongArray(long[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readLong();
      }

   }

   protected void readFloatArray(float[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readFloat();
      }

   }

   protected void readDoubleArray(double[] buffer) throws IOException {
      for(int i = 0; i < buffer.length; ++i) {
         buffer[i] = this.readDouble();
      }

   }

   protected void readObjectArray(Object[] objects) throws IOException, StructException {
      for(int i = 0; i < objects.length; ++i) {
         this.readObject(objects[i]);
      }

   }
}
