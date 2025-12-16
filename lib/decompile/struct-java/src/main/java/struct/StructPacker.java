package struct;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;

public class StructPacker extends StructOutput {
   private ByteArrayOutputStream bos;
   protected DataOutput dataOutput;

   protected void init(OutputStream outStream, ByteOrder order) {
      if (order == ByteOrder.LITTLE_ENDIAN) {
         this.dataOutput = new LEDataOutputStream(outStream);
      } else {
         this.dataOutput = new DataOutputStream(outStream);
      }

   }

   public StructPacker() {
      this(new ByteArrayOutputStream(), ByteOrder.BIG_ENDIAN);
   }

   public StructPacker(ByteOrder order) {
      this(new ByteArrayOutputStream(), order);
   }

   public StructPacker(OutputStream os, ByteOrder order) {
      this.init(os, order);
      this.bos = (ByteArrayOutputStream)os;
   }

   public byte[] pack(Object objectToPack) throws StructException {
      this.writeObject(objectToPack);
      return this.bos.toByteArray();
   }

   public void writeBoolean(boolean value) throws IOException {
      this.dataOutput.writeBoolean(value);
   }

   public void writeByte(byte value) throws IOException {
      this.dataOutput.writeByte(value);
   }

   public void writeShort(short value) throws IOException {
      this.dataOutput.writeShort(value);
   }

   public void writeInt(int value) throws IOException {
      this.dataOutput.writeInt(value);
   }

   public void writeLong(long value) throws IOException {
      this.dataOutput.writeLong(value);
   }

   public void writeChar(char value) throws IOException {
      this.dataOutput.writeChar(value);
   }

   public void writeFloat(float value) throws IOException {
      this.dataOutput.writeFloat(value);
   }

   public void writeDouble(double value) throws IOException {
      this.dataOutput.writeDouble(value);
   }

   public void writeBooleanArray(boolean[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeBoolean(buffer[i]);
      }

   }

   public void writeByteArray(byte[] buffer, int len) throws IOException {
      if (len != 0) {
         if (len == -1 || len > buffer.length) {
            len = buffer.length;
         }

         this.dataOutput.write(buffer, 0, len);
      }
   }

   public void writeCharArray(char[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeChar(buffer[i]);
      }

   }

   public void writeShortArray(short[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeShort(buffer[i]);
      }

   }

   public void writeIntArray(int[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeInt(buffer[i]);
      }

   }

   public void writeLongArray(long[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeLong(buffer[i]);
      }

   }

   public void writeFloatArray(float[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeFloat(buffer[i]);
      }

   }

   public void writeDoubleArray(double[] buffer, int len) throws IOException {
      if (len == -1 || len > buffer.length) {
         len = buffer.length;
      }

      for(int i = 0; i < len; ++i) {
         this.dataOutput.writeDouble(buffer[i]);
      }

   }

   public void writeObjectArray(Object[] buffer, int len) throws IOException, IllegalAccessException, InvocationTargetException, StructException {
      if (buffer != null && len != 0) {
         if (len == -1 || len > buffer.length) {
            len = buffer.length;
         }

         for(int i = 0; i < len; ++i) {
            this.writeObject(buffer[i]);
         }

      }
   }
}
