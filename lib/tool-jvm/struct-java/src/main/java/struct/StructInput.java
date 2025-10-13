//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package struct;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class StructInput extends InputStream {
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

    public void readField(StructFieldData fieldData, Method getter, Method setter, Object obj) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, StructException {
        Field field = fieldData.getField();
        if (!field.getType().isArray()) {
            switch (fieldData.getType()) {
                case BOOLEAN:
                    if (setter != null) {
                        setter.invoke(obj, this.readBoolean());
                    } else {
                        field.setBoolean(obj, this.readBoolean());
                    }
                    break;
                case BYTE:
                    if (setter != null) {
                        setter.invoke(obj, this.readByte());
                    } else {
                        field.setByte(obj, this.readByte());
                    }
                    break;
                case SHORT:
                    if (setter != null) {
                        setter.invoke(obj, this.readShort());
                    } else {
                        field.setShort(obj, this.readShort());
                    }
                    break;
                case INT:
                    if (setter != null) {
                        setter.invoke(obj, this.readInt());
                    } else {
                        field.setInt(obj, this.readInt());
                    }
                    break;
                case LONG:
                    if (setter != null) {
                        setter.invoke(obj, this.readLong());
                    } else {
                        field.setLong(obj, this.readLong());
                    }
                    break;
                case CHAR:
                    if (setter != null) {
                        setter.invoke(obj, this.readChar());
                    } else {
                        field.setChar(obj, this.readChar());
                    }
                    break;
                case FLOAT:
                    if (setter != null) {
                        setter.invoke(obj, this.readFloat());
                    } else {
                        field.setFloat(obj, this.readFloat());
                    }
                    break;
                case DOUBLE:
                    if (setter != null) {
                        setter.invoke(obj, this.readDouble());
                    } else {
                        field.setDouble(obj, this.readDouble());
                    }
                    break;
                default:
                    if (setter != null) {
                        Object object = getter.invoke(obj, (Object[])null);
                        if (object == null) {
                            if (field.getName().endsWith("CString")) {
                                throw new StructException("CString objects should be initialized :" + field.getName());
                            }

                            object = field.getType().newInstance();
                        }

                        this.readObject(object);
                        setter.invoke(obj, object);
                    } else {
                        this.handleObject(field, obj);
                    }
            }
        } else {
            if (getter != null && getter.invoke(obj, (Object[])null) == null) {
                throw new StructException("Arrays can not be null : " + field.getName());
            }

            switch (fieldData.getType()) {
                case BOOLEAN:
                    if (getter != null) {
                        this.readBooleanArray((boolean[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readBooleanArray((boolean[])field.get(obj));
                    }
                    break;
                case BYTE:
                    if (getter != null) {
                        this.readByteArray((byte[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readByteArray((byte[])field.get(obj));
                    }
                    break;
                case SHORT:
                    if (getter != null) {
                        this.readShortArray((short[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readShortArray((short[])field.get(obj));
                    }
                    break;
                case INT:
                    if (getter != null) {
                        this.readIntArray((int[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readIntArray((int[])field.get(obj));
                    }
                    break;
                case LONG:
                    if (getter != null) {
                        this.readLongArray((long[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readLongArray((long[])field.get(obj));
                    }
                    break;
                case CHAR:
                    if (getter != null) {
                        this.readCharArray((char[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readCharArray((char[])field.get(obj));
                    }
                    break;
                case FLOAT:
                    if (getter != null) {
                        this.readFloatArray((float[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readFloatArray((float[])field.get(obj));
                    }
                    break;
                case DOUBLE:
                    if (getter != null) {
                        this.readDoubleArray((double[])getter.invoke(obj, (Object[])null));
                    } else {
                        this.readDoubleArray((double[])field.get(obj));
                    }
                    break;
                default:
                    if (getter != null) {
                        this.readObjectArray(new Object[]{getter.invoke(obj, (Object[]) null)});
                    } else {
                        this.readObjectArray(field.get(obj));
                    }
            }
        }

    }

    public void handleObject(Field field, Object obj) throws IllegalArgumentException, StructException, IOException, InstantiationException, IllegalAccessException {
        if (field.get(obj) == null) {
            if (field.getType().getName().endsWith("CString")) {
                throw new StructException("CString objects should be initialized before unpacking :" + field.getName());
            }

            field.set(obj, field.getType().newInstance());
        }

        this.readObject(field.get(obj));
    }

    public void close() throws IOException {
    }

    public int read() throws IOException {
        return -1;
    }

    protected abstract boolean readBoolean() throws IOException;

    protected abstract byte readByte() throws IOException;

    protected abstract short readShort() throws IOException;

    protected abstract int readInt() throws IOException;

    protected abstract long readLong() throws IOException;

    protected abstract char readChar() throws IOException;

    protected abstract float readFloat() throws IOException;

    protected abstract double readDouble() throws IOException;

    protected abstract void readBooleanArray(boolean[] var1) throws IOException;

    protected abstract void readByteArray(byte[] var1) throws IOException;

    protected abstract void readCharArray(char[] var1) throws IOException;

    protected abstract void readShortArray(short[] var1) throws IOException;

    protected abstract void readIntArray(int[] var1) throws IOException;

    protected abstract void readLongArray(long[] var1) throws IOException;

    protected abstract void readFloatArray(float[] var1) throws IOException;

    protected abstract void readDoubleArray(double[] var1) throws IOException;

    protected abstract void readObjectArray(Object[] var1) throws IOException, StructException;
}
