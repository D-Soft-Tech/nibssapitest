package com.netpluspay.nibssclient.util.tlv;




import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TLV {
    private byte[] data;
    private String tag;
    private int length = -1;
    private byte[] value;

    public static TLV fromRawData(byte[] tlData, int tlOffset, byte[] vData, int vOffset)
    {
        int tLen = getTLength(tlData, tlOffset);
        int lLen = getLLength(tlData, tlOffset + tLen);
        int vLen = calcValueLength(tlData, tlOffset + tLen, lLen);
        TLV d = new TLV();
        d.data = BytesUtil.merage(new byte[][] { BytesUtil.subBytes(tlData, tlOffset, tLen + lLen), BytesUtil.subBytes(vData, vOffset, vLen) });
        d.getTag();
        d.getLength();
        d.getBytesValue();

        return d;
    }

    public static TLV fromData(String tagName, byte[] value) {
        byte[] tag = BytesUtil.hexString2Bytes(tagName);
        TLV d = new TLV();
        d.data = BytesUtil.merage(new byte[][] { tag, makeLengthData(value.length), value });
        d.tag = tagName;
        d.length = value.length;
        d.value = value;
        return d;
    }

    public static TLV fromRawData(byte[] data, int offset) {
        int len = getDataLength(data, offset);
        TLV d = new TLV();
        d.data = BytesUtil.subBytes(data, offset, len);
        d.getTag();
        d.getLength();
        d.getBytesValue();
        return d;
    }

    public String getTag() {
        if (this.tag != null) {
            return this.tag;
        }
        int tLen = getTLength(this.data, 0);
        return this.tag = BytesUtil.bytes2HexString(BytesUtil.subBytes(this.data, 0, tLen));
    }

    public int getLength() {
        if (this.length > -1) {
            return this.length;
        }
        int offset = getTLength(this.data, 0);
        int l = getLLength(this.data, offset);
        if (l == 1) {
            return this.data[offset] & 0xFF;
        }

        int afterLen = 0;
        for (int i = 1; i < l; i++) {
            afterLen <<= 8;
            afterLen |= this.data[(offset + i)] & 0xFF;
        }
        return this.length = afterLen;
    }

    public int getTLLength() {
        if (this.data == null) {
            return -1;
        }
        return this.data.length - getBytesValue().length;
    }

    public String getValue() {
        byte[] temp = getBytesValue();
        return BytesUtil.bytes2HexString(temp == null ? new byte[0] : temp);
    }

    public byte getByteValue() {
        return getBytesValue()[0];
    }

    public String getGBKValue() {
        try {
            return new String(getBytesValue(), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNumberValue() {
        String num = getValue();
        return String.valueOf(Integer.parseInt(num));
    }

    public byte[] getGBKNumberValue() {
        try {
            return getNumberValue().getBytes("GBK");
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
        }
        return null;
    }

    public byte[] getBCDValue() {
        return BytesUtil.hexString2Bytes(getGBKValue());
    }

    public byte[] getRawData() {
        return this.data;
    }

    public byte[] getBytesValue() {
        if (this.value != null) {
            return this.value;
        }
        int l = getLength();
        return this.value = BytesUtil.subBytes(this.data, this.data.length - l, l);
    }

    public boolean isValid() {
        return this.data != null;
    }

    private static int getTLength(byte[] data, int offset) {
        if ((data[offset] & 0x1F) == 31) {
            return 2;
        }
        return 1;
    }

    private static int getLLength(byte[] data, int offset) {
        if ((data[offset] & 0x80) == 0) {
            return 1;
        }
        return (data[offset] & 0x7F) + 1;
    }

    private static int getDataLength(byte[] data, int offset) {
        int tLen = getTLength(data, offset);
        int lLen = getLLength(data, offset + tLen);
        int vLen = calcValueLength(data, offset + tLen, lLen);
        return tLen + lLen + vLen;
    }

    private static int calcValueLength(byte[] l, int offset, int lLen) {
        if (lLen == 1) {
            return l[offset] & 0xFF;
        }

        int vLen = 0;
        for (int i = 1; i < lLen; i++) {
            vLen <<= 8;
            vLen |= l[(offset + i)] & 0xFF;
        }
        return vLen;
    }

    private static byte[] makeLengthData(int len) {
        if (len > 127) {
            byte[] lenData = new byte[4];
            int validIndex = -1;
            for (int i = 0; i < lenData.length; i++) {
                lenData[i] = ((byte)(len >> 8 * (3 - i) & 0xF));
                if ((lenData[(3 - i)] != 0) && (validIndex < 0)) {
                    validIndex = i;
                }
            }
            lenData = BytesUtil.subBytes(lenData, validIndex, -1);
            lenData = BytesUtil.merage(new byte[][] { { (byte)(0x80 & lenData.length) }, lenData });
            return lenData;
        }

        return new byte[] { (byte)len };
    }

    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof TLV)) {
            return false;
        }

        if ((this.data == null) || (((TLV)obj).data == null)) {
            return false;
        }

        return Arrays.equals(this.data, ((TLV)obj).data);
    }

    public String toString()
    {
        if (this.data == null) {
            return super.toString();
        }
        return BytesUtil.bytes2HexString(this.data);
    }
}
