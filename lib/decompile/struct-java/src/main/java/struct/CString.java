package struct;

import java.io.Serializable;

@StructClass
public class CString implements Serializable {
   private static final long serialVersionUID = -3393948411351663341L;
   @StructField(
      order = 0
   )
   private byte[] buffer = null;

   public CString(int len) {
      this.buffer = new byte[len];
   }

   public CString(String str, int len) {
      this.buffer = new byte[len];
      this.copyData(str.getBytes(), len);
   }

   public CString(byte[] data, int len) {
      this.buffer = new byte[len];
      this.copyData(data, len);
   }

   public CString(String str, char fillChar, int len) {
      if (str == null) {
         str = "";
      }

      this.buffer = new byte[len];

      for(int i = 0; i < this.buffer.length; ++i) {
         this.buffer[i] = (byte)fillChar;
      }

      this.copyData(str.getBytes(), len);
   }

   public CString(byte[] data, char fillChar, int len) {
      this.buffer = new byte[len];

      for(int i = 0; i < this.buffer.length; ++i) {
         this.buffer[i] = (byte)fillChar;
      }

      this.copyData(data, len);
   }

   private void copyData(byte[] data, int len) {
      if (data.length < len) {
         System.arraycopy(data, 0, this.buffer, 0, data.length);
      } else {
         System.arraycopy(data, 0, this.buffer, 0, len);
      }

   }

   public boolean equals(Object obj) {
      CString str = (CString)obj;
      return str.toString().equals(this.toString());
   }

   public void setString(String str) {
      System.arraycopy(str.getBytes(), 0, this.buffer, 0, str.getBytes().length);
   }

   public String toString() {
      return (new String(this.buffer)).trim();
   }

   public String asCString() {
      int i;
      for(i = 0; i < this.buffer.length && this.buffer[i] != 0; ++i) {
      }

      String str = new String(this.buffer, 0, i);
      return str;
   }

   public byte[] getBuffer() {
      return this.buffer;
   }

   public void setBuffer(byte[] buffer) {
      this.buffer = buffer;
   }
}
