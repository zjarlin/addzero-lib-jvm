package struct;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LEDataInputStream implements DataInput {
   private DataInputStream d;
   private InputStream in;
   byte[] w;

   public LEDataInputStream(InputStream in) {
      this.in = in;
      this.d = new DataInputStream(in);
      this.w = new byte[8];
   }

   public final short readShort() throws IOException {
      this.d.readFully(this.w, 0, 2);
      return (short)((this.w[1] & 255) << 8 | this.w[0] & 255);
   }

   public final int readUnsignedShort() throws IOException {
      this.d.readFully(this.w, 0, 2);
      return (this.w[1] & 255) << 8 | this.w[0] & 255;
   }

   public final char readChar() throws IOException {
      this.d.readFully(this.w, 0, 2);
      return (char)((this.w[1] & 255) << 8 | this.w[0] & 255);
   }

   public final int readInt() throws IOException {
      this.d.readFully(this.w, 0, 4);
      return this.w[3] << 24 | (this.w[2] & 255) << 16 | (this.w[1] & 255) << 8 | this.w[0] & 255;
   }

   public final long readLong() throws IOException {
      this.d.readFully(this.w, 0, 8);
      return (long)this.w[7] << 56 | (long)(this.w[6] & 255) << 48 | (long)(this.w[5] & 255) << 40 | (long)(this.w[4] & 255) << 32 | (long)(this.w[3] & 255) << 24 | (long)(this.w[2] & 255) << 16 | (long)(this.w[1] & 255) << 8 | (long)(this.w[0] & 255);
   }

   public final float readFloat() throws IOException {
      return Float.intBitsToFloat(this.readInt());
   }

   public final double readDouble() throws IOException {
      return Double.longBitsToDouble(this.readLong());
   }

   public final int read(byte[] b, int off, int len) throws IOException {
      return this.in.read(b, off, len);
   }

   public final void readFully(byte[] b) throws IOException {
      this.d.readFully(b, 0, b.length);
   }

   public final void readFully(byte[] b, int off, int len) throws IOException {
      this.d.readFully(b, off, len);
   }

   public final int skipBytes(int n) throws IOException {
      return this.d.skipBytes(n);
   }

   public final boolean readBoolean() throws IOException {
      return this.d.readBoolean();
   }

   public final byte readByte() throws IOException {
      return this.d.readByte();
   }

   public final int readUnsignedByte() throws IOException {
      return this.d.readUnsignedByte();
   }

   public final String readLine() throws IOException {
      return this.d.readLine();
   }

   public final String readUTF() throws IOException {
      return this.d.readUTF();
   }

   public static final String readUTF(DataInput in) throws IOException {
      return DataInputStream.readUTF(in);
   }

   public final void close() throws IOException {
      this.d.close();
   }
}
