package core.net.packet;

import core.cache.secure.ISAACCipher;
import core.cache.util.ByteBufferExtensions;

import java.nio.ByteBuffer;

/**
 * Represents the buffer used for reading/writing packets.
 *
 * @author Emperor
 */
public class IoBuffer {

    /**
     * The bit masks.
     */
    private static final int[] BIT_MASK = new int[32];

    /**
     * The packet size.
     */
    private int packetSize;

    /**
     * The opcode.
     */
    private int opcode;

    /**
     * The packet header.
     */
    private final PacketHeader header;

    /**
     * The byte buffer.
     */
    private ByteBuffer buf;

    /**
     * The bit position.
     */
    private int bitPosition = 0;

    /**
     * Constructs a new {@code IoBuffer} {@code Object}.
     */
    public IoBuffer() {
        this(-1, PacketHeader.NORMAL, ByteBuffer.allocate(2048));
    }

    /**
     * Constructs a new {@code IoBuffer} {@code Object}.
     *
     * @param opcode The opcode.
     */
    public IoBuffer(int opcode) {
        this(opcode, PacketHeader.NORMAL, ByteBuffer.allocate(2048));
    }

    /**
     * Constructs a new {@code IoBuffer} {@code Object}.
     *
     * @param opcode The opcode.
     * @param header The packet header.
     */
    public IoBuffer(int opcode, PacketHeader header) {
        this(opcode, header, ByteBuffer.allocate((1 << 16) + 1));
    }

    /**
     * Constructs a new {@code IoBuffer} {@code Object}.
     *
     * @param opcode The opcode.
     * @param header The packet header.
     * @param buf    The byte buffer.
     */
    public IoBuffer(int opcode, PacketHeader header, ByteBuffer buf) {
        this.opcode = opcode;
        this.header = header;
        this.buf = buf;
    }

    static {
        for (int i = 0; i < 32; i++) {
            BIT_MASK[i] = (1 << i) - 1;
        }
    }

    /**
     * Clears the buffer and resets bit position.
     *
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer clear() {
        buf.clear();
        bitPosition = 0;
        return this;
    }

    /**
     * Puts a single byte (value) into the buffer.
     *
     * @param value the byte value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p1(int value) {
        buf.put((byte) value);
        return this;
    }

    /**
     * Puts a byte value plus 128 into the buffer.
     *
     * @param value the byte value to add 128 to before putting
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p1add(int value) {
        buf.put((byte) (value + 128));
        return this;
    }

    /**
     * Puts a byte equal to 128 minus the value into the buffer.
     *
     * @param value the value to subtract from 128
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p1sub(int value) {
        buf.put((byte) (128 - value));
        return this;
    }

    /**
     * Puts the negative of the byte value into the buffer.
     *
     * @param value the value to negate before putting
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p1neg(int value) {
        buf.put((byte) -value);
        return this;
    }

    /**
     * Puts a 2-byte (short) value into the buffer in big-endian order.
     *
     * @param value the short value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p2(int value) {
        buf.put((byte) (value >> 8));
        buf.put((byte) value);
        return this;
    }

    /**
     * Puts a 2-byte value where the second byte is added with 128.
     *
     * @param value the short value to put with addition
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p2add(int value) {
        buf.put((byte) (value >> 8));
        buf.put((byte) (value + 128));
        return this;
    }

    /**
     * Puts a 2-byte value in little-endian order.
     *
     * @param value the short value to put in little-endian
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer ip2(int value) {
        buf.put((byte) value);
        buf.put((byte) (value >> 8));
        return this;
    }

    /**
     * Puts a 2-byte value with addition in little-endian order.
     *
     * @param value the short value to put with addition in little-endian
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer ip2add(int value) {
        buf.put((byte) (value + 128));
        buf.put((byte) (value >> 8));
        return this;
    }

    /**
     * Puts a 3-byte (tri-byte) value in big-endian order.
     *
     * @param value the tri-byte value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p3(int value) {
        buf.put((byte) (value >> 16));
        buf.put((byte) (value >> 8));
        buf.put((byte) value);
        return this;
    }

    /**
     * Puts a 3-byte value in little-endian order.
     *
     * @param value the tri-byte value to put in little-endian
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer ip3(int value) {
        buf.put((byte) value);
        buf.put((byte) (value >> 8));
        buf.put((byte) (value >> 16));
        return this;
    }

    /**
     * Puts a 4-byte (int) value in big-endian order.
     *
     * @param value the int value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p4(int value) {
        buf.put((byte) (value >> 24));
        buf.put((byte) (value >> 16));
        buf.put((byte) (value >> 8));
        buf.put((byte) value);
        return this;
    }

    /**
     * Puts a 4-byte value in little-endian order.
     *
     * @param value the int value to put in little-endian
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer ip4(int value) {
        buf.put((byte) value);
        buf.put((byte) (value >> 8));
        buf.put((byte) (value >> 16));
        buf.put((byte) (value >> 24));
        return this;
    }

    /**
     * Puts a 4-byte value in mixed order: middle two bytes swapped.
     *
     * @param value the int value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer mp4(int value) {
        buf.put((byte) (value >> 16));
        buf.put((byte) (value >> 24));
        buf.put((byte) value);
        buf.put((byte) (value >> 8));
        return this;
    }

    /**
     * Puts a 4-byte value in mixed order with addition.
     *
     * @param value the int value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer imp4(int value) {
        buf.put((byte) (value >> 8));
        buf.put((byte) value);
        buf.put((byte) (value >> 24));
        buf.put((byte) (value >> 16));
        return this;
    }

    /**
     * Puts an 8-byte (long) value in big-endian order.
     *
     * @param value the long value to put
     * @return this IoBuffer instance for chaining
     */
    public IoBuffer p8(long value) {
        buf.put((byte) (value >> 56));
        buf.put((byte) (value >> 48));
        buf.put((byte) (value >> 40));
        buf.put((byte) (value >> 32));
        buf.put((byte) (value >> 24));
        buf.put((byte) (value >> 16));
        buf.put((byte) (value >> 8));
        buf.put((byte) value);
        return this;
    }

    /**
     * Writes a variable-length int using varint encoding.
     */
    public IoBuffer pVarInt(int value) {
        if ((value & 0xffffff80) != 0) {
            if ((value & 0xffffc000) != 0) {
                if ((value & 0xFFE00000) != 0) {
                    if ((value & 0xF0000000) != 0) {
                        this.p1(value >>> 28 | 0x80);
                    }
                    this.p1(value >>> 21 | 0x80);
                }
                this.p1(value >>> 14 | 0x80);
            }
            this.p1(value >>> 7 | 0x80);
        }
        return this.p1(value & 0x7F);
    }

    /**
     * Writes a fixed-length long using a specified number of bytes (1–8).
     */
    public IoBuffer pVarLong(int size, long value) {
        int bytes = size - 1;
        if (bytes < 0 || bytes > 7) throw new IllegalArgumentException();
        for (int shift = bytes * 8; shift >= 0; shift -= 8)
            this.p1((byte) (value >> shift));
        return this;
    }

    /**
     * Writes an int using smart encoding (1 or 2 bytes depending on value).
     */
    public IoBuffer psmarts(int value) {
        if (value >= 0 && value < 128) this.p1(value);
        else if (value >= 0 && value < 0x8000) this.p2(value + 0x8000);
        else throw new IllegalArgumentException("smart out of range: $value");
        return this;
    }

    /**
     * Inserts packet size at correct offset using normal size format.
     */
    public IoBuffer psize(int length) {
        buf.put(buf.position() - length - 1, (byte) length);
        return this;
    }

    /**
     * Inserts packet size using +128 (A-type) transformation.
     */
    public IoBuffer psizeadd(int length) {
        buf.put(buf.position() - length - 1, (byte) (length + 128));
        return this;
    }

    /**
     * Reads an unsigned byte (0–255).
     */
    public int g1() {
        return buf.get() & 0xFF;
    }

    /**
     * Reads a signed byte (-128 to 127).
     */
    public int g1b() {
        return buf.get();
    }

    /**
     * Reads a byte and applies A-type transformation (subtracts 128).
     */
    public int g1add() {
        return (buf.get() - 128) & 0xFF;
    }

    /**
     * Reads an unsigned byte and negates the value (C-type).
     */
    public int g1neg() {
        return -(buf.get() & 0xFF);
    }

    /**
     * Reads a byte and returns (128 - byte) masked to 0–255 (S-type).
     */
    public int g1sub() {
        return (128 - buf.get()) & 0xFF;
    }

    /**
     * Reads a 2-byte unsigned short in big-endian order.
     */
    public int g2() {
        return ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF);
    }

    /**
     * Reads a 2-byte short with A-type transformation (128 subtracted from 2nd byte).
     */
    public int g2add() {
        return ((buf.get() & 0xff) << 8) + ((buf.get() - 128) & 0xFF);
    }

    /**
     * Reads a signed 2-byte short (big-endian).
     */
    public int g2b() {
        int value = ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF);
        if (value > 32767) value -= 0x10000;
        return value;
    }

    /**
     * Reads a 2-byte unsigned short in little-endian order.
     */
    public int ig2() {
        return (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads a 2-byte short in little-endian order with A-type transformation.
     */
    public int ig2add() {
        return ((buf.get() - 128) & 0xFF) + ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads a 3-byte unsigned integer in big-endian order.
     */
    public int g3() {
        return ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF);
    }

    /**
     * Reads a 3-byte unsigned integer in little-endian order.
     */
    public int ig3() {
        return (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8) + ((buf.get() & 0xFF) << 16);
    }

    /**
     * Reads a 4-byte signed integer in big-endian order.
     */
    public int g4() {
        return ((buf.get() & 0xFF) << 24) + ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF);
    }

    /**
     * Reads a 4-byte signed integer in little-endian order.
     */
    public int ig4() {
        return (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8) + ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 24);
    }

    /**
     * Reads a 4-byte int in mixed byte order: [2][3][0][1].
     */
    public int m4() {
        return ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 24) + (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads a 4-byte int in inverse mixed byte order: [1][0][3][2].
     */
    public int im4() {
        return ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 24) + ((buf.get() & 0xFF) << 16);
    }

    /**
     * Reads an 8-byte long composed of two 4-byte big-endian ints (high | low).
     */
    public long g8() {
        long low = (long) this.g4() & 0xFFFFFFFFL;
        long high = (long) this.g4() & 0xFFFFFFFFL;
        return high + (low << 32);
    }

    /**
     * Writes a single byte.
     */
    public IoBuffer put(int val) {
        buf.put((byte) val);
        return this;
    }


    public IoBuffer putBytes(byte[] datas, int offset, int len) {
        for (int i = offset; i < len; i++) {
            put(datas[i]);
        }
        return this;
    }

    public final void getBytes(byte data[], int off, int len) {
        for (int k = off; k < len + off; k++) {
            data[k] = data[off++];
        }
    }

    /**
     * Writes a byte with +128 transformation (A-type).
     */
    public IoBuffer putA(int val) {
        buf.put((byte) (val + 128));
        return this;
    }

    /**
     * Writes a negated byte (C-type).
     */
    public IoBuffer putC(int val) {
        buf.put((byte) -val);
        return this;
    }

    /**
     * Writes a byte with 128 - val transformation (S-type).
     */
    public IoBuffer putS(int val) {
        buf.put((byte) (128 - val));
        return this;
    }

    /**
     * Writes 3 bytes as a 24-bit integer (big-endian).
     */
    public IoBuffer putTri(int val) {
        buf.put((byte) (val >> 16));
        buf.put((byte) (val >> 8));
        buf.put((byte) val);
        return this;
    }

    /**
     * Writes a 2-byte short (big-endian).
     */
    public IoBuffer putShort(int val) {
        buf.putShort((short) val);
        return this;
    }

    /**
     * Writes a 2-byte short (little-endian).
     */
    public IoBuffer putLEShort(int val) {
        buf.put((byte) val);
        buf.put((byte) (val >> 8));
        return this;
    }

    /**
     * Writes a short (A-type) big-endian.
     */
    public IoBuffer putShortA(int val) {
        buf.put((byte) (val >> 8));
        buf.put((byte) (val + 128));
        return this;
    }

    /**
     * Writes a short (A-type) little-endian.
     */
    public IoBuffer putLEShortA(int val) {
        buf.put((byte) (val + 128));
        buf.put((byte) (val >> 8));
        return this;
    }

    /**
     * Writes a 4-byte integer (big-endian).
     */
    public IoBuffer putInt(int val) {
        buf.putInt(val);
        return this;
    }

    /**
     * Writes a 4-byte integer (little-endian).
     */
    public IoBuffer putLEInt(int val) {
        buf.put((byte) val);
        buf.put((byte) (val >> 8));
        buf.put((byte) (val >> 16));
        buf.put((byte) (val >> 24));
        return this;
    }

    /**
     * Writes a 4-byte int using A-type mixed byte order.
     */
    public IoBuffer putIntA(int val) {
        buf.put((byte) (val >> 8));
        buf.put((byte) val);
        buf.put((byte) (val >> 24));
        buf.put((byte) (val >> 16));
        return this;
    }

    /**
     * Writes a 4-byte int using B-type mixed byte order.
     */
    public IoBuffer putIntB(int val) {
        buf.put((byte) (val >> 16));
        buf.put((byte) (val >> 24));
        buf.put((byte) val);
        buf.put((byte) (val >> 8));
        return this;
    }

    /**
     * Writes an 8-byte long (big-endian).
     */
    public IoBuffer putLong(long val) {
        buf.putLong(val);
        return this;
    }

    /**
     * Writes a value using 1 or 2 bytes depending on size.
     */
    public IoBuffer putSmart(int val) {
        if (val > Byte.MAX_VALUE) {
            buf.putShort((short) (val + 32768));
        } else {
            buf.put((byte) val);
        }
        return this;
    }

    /**
     * Writes a value using 2 or 4 bytes depending on size.
     */
    public IoBuffer putIntSmart(int val) {
        if (val > Short.MAX_VALUE) {
            buf.putInt(val + 32768);
        } else {
            buf.putShort((short) val);
        }
        return this;
    }

    /**
     * Writes a JAG-compliant string.
     */
    public IoBuffer writeJagString(String str) {
        for (int i = 0; i < str.length(); i++) {
            this.p1(str.charAt(i));
        }
        this.p1(0);
        return this;
    }

    /**
     * Writes a single byte (same as p1, kept for Kotlin p1b equivalent).
     */
    public IoBuffer p1b(int value) {
        return this.p1(value);
    }

    /**
     * Writes a 2-byte value in big-endian order using two p1 calls.
     */
    public IoBuffer p2b(int value) {
        this.p1((value >> 8) & 0xFF);
        this.p1(value & 0xFF);
        return this;
    }

    /**
     * Writes a null-terminated string.
     */
    public IoBuffer putString(String val) {
        buf.put(val.getBytes());
        buf.put((byte) 0);
        return this;
    }

    /**
     * Writes a string prefixed by 0 and terminated by 0 (Jag format).
     */
    public IoBuffer putJagString(String val) {
        buf.put((byte) 0);
        buf.put(val.getBytes());
        buf.put((byte) 0);
        return this;
    }

    /**
     * Writes a packed string using Jag encoding.
     */
    public IoBuffer putJagString2(String val) {
        byte[] packed = new byte[256];
        int length = ByteBufferExtensions.packGJString2(0, packed, val);
        buf.put((byte) 0).put(packed, 0, length).put((byte) 0);
        return this;
    }

    /**
     * Writes raw byte array.
     */
    public IoBuffer put(byte[] val) {
        buf.put(val);
        return this;
    }

    /**
     * Writes bytes in reverse order using A-type transformation.
     */
    public void putReverseA(byte[] data, int start, int offset) {
        for (int i = offset + start; i >= start; i--) {
            putA(data[i]);
        }
    }

    /**
     * Writes bytes in reverse order.
     */
    public void putReverse(byte[] data, int start, int offset) {
        for (int i = offset + start; i >= start; i--) {
            put(data[i]);
        }
    }

    /**
     * Puts a specified number of bits from a value into the buffer.
     *
     * @param numBits the number of bits to write
     * @param value   the value containing bits to write
     */
    public IoBuffer putBits(int numBits, int value) {
        int bytePos = getBitPosition() >> 3;
        int bitOffset = 8 - (getBitPosition() & 7);
        bitPosition += numBits;
        for (; numBits > bitOffset; bitOffset = 8) {
            byte b = buf.get(bytePos);
            buf.put(bytePos, b &= ~BIT_MASK[bitOffset]);
            buf.put(bytePos++, b |= value >> numBits - bitOffset & BIT_MASK[bitOffset]);
            numBits -= bitOffset;
        }
        byte b = buf.get(bytePos);
        if (numBits == bitOffset) {
            buf.put(bytePos, b &= ~BIT_MASK[bitOffset]);
            buf.put(bytePos, b |= value & BIT_MASK[bitOffset]);
        } else {
            buf.put(bytePos, b &= ~(BIT_MASK[numBits] << bitOffset - numBits));
            buf.put(bytePos, b |= (value & BIT_MASK[numBits]) << bitOffset - numBits);
        }
        return this;
    }

    /**
     * Gets a specified number of bits from the buffer.
     *
     * @param numBits the number of bits to read
     * @return the bits read as an int
     */
    public int getBits(int numBits) {
        int bytePos = bitPosition >> 3;
        int bitOffset = 8 - (bitPosition & 7);
        int value = 0;
        bitPosition += numBits;

        while (numBits > bitOffset) {
            value += (buf.get(bytePos) & BIT_MASK[bitOffset]) << (numBits - bitOffset);
            numBits -= bitOffset;
            bytePos++;
            bitOffset = 8;
        }
        if (numBits == bitOffset) {
            value += buf.get(bytePos) & BIT_MASK[bitOffset];
        } else {
            value += (buf.get(bytePos) >> (bitOffset - numBits)) & BIT_MASK[numBits];
        }
        return value;
    }

    /**
     * Writes the contents of another {@link IoBuffer} into buffer.
     *
     * @param buffer The source {@link IoBuffer} to write from.
     * @return This buffer instance, for chaining.
     */
    public IoBuffer put(IoBuffer buffer) {
        buffer.toByteBuffer().flip();
        buf.put(buffer.toByteBuffer());
        return this;
    }

    /**
     * Writes the contents of another {@link IoBuffer} into buffer.
     *
     * @param buffer The source {@link IoBuffer} to write from.
     * @return This buffer instance, for chaining.
     */
    public IoBuffer putA(IoBuffer buffer) {
        buffer.toByteBuffer().flip();
        while (buffer.toByteBuffer().hasRemaining()) {
            putA(buffer.toByteBuffer().get());
        }
        return this;
    }

    /**
     * Writes the contents of a {@link ByteBuffer} directly into this buffer.
     *
     * @param buffer The {@link ByteBuffer} to write from.
     * @return This buffer instance, for chaining.
     */
    public IoBuffer put(ByteBuffer buffer) {
        buf.put(buffer);
        return this;
    }

    /**
     * Switches buffer into bit access mode by setting bitPosition.
     */
    public IoBuffer setBitAccess() {
        bitPosition = buf.position() * 8;
        return this;
    }

    /**
     * Switches buffer out of bit access mode by syncing byte position.
     */
    public IoBuffer setByteAccess() {
        buf.position((getBitPosition() + 7) / 8);
        return this;
    }

    /**
     * Reads a single byte from the buffer.
     *
     * @return The byte value as a signed int.
     */
    public int get() {
        return buf.get();
    }

    /**
     * Reads a byte and subtracts 128 from it.
     *
     * @return The adjusted byte value.
     */
    public int getA() {
        return (buf.get() & 0xFF) - 128;
    }

    /**
     * Reads a byte and returns its negation.
     *
     * @return The negated byte value.
     */
    public int getC() {
        return -buf.get();
    }

    /**
     * Reads a byte and returns 128 minus its value.
     *
     * @return The transformed byte value.
     */
    public int getS() {
        return 128 - (buf.get() & 0xFF);
    }

    /**
     * Reads a 3-byte integer (tri-byte) from the buffer in big-endian order.
     *
     * @return The tri-byte value as an int.
     */
    public int getTri() {
        return ((buf.get() << 16) & 0xFF) | ((buf.get() << 8) & 0xFF) | (buf.get() & 0xFF);
    }

    /**
     * Reads a 2-byte (short) value from the buffer.
     *
     * @return The short value as an int.
     */
    public int getShort() {
        return buf.getShort();
    }

    /**
     * Reads a 2-byte little-endian short.
     *
     * @return The short value in little-endian format.
     */
    public int getLEShort() {
        return (buf.get() & 0xFF) | ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads a 2-byte short with transformation: second byte is subtracted by 128.
     *
     * @return The transformed short value.
     */
    public int getShortA() {
        return ((buf.get() & 0xFF) << 8) | (buf.get() - 128 & 0xFF);
    }

    /**
     * Reads a little-endian short where the first byte is subtracted by 128.
     *
     * @return The transformed little-endian short value.
     */
    public int getLEShortA() {
        return (buf.get() - 128 & 0xFF) | ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads a 4-byte integer (int) in big-endian order.
     *
     * @return The int value.
     */
    public int getInt() {
        return buf.getInt();
    }

    /**
     * Reads a 4-byte integer in little-endian order.
     *
     * @return The int value in little-endian format.
     */
    public int getLEInt() {
        return (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8) + ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 24);
    }

    /**
     * Reads a 4-byte integer with mixed byte order (custom transformation A).
     *
     * @return The transformed int value.
     */
    public int getIntA() {
        return ((buf.get() & 0xFF) << 8) + (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 24) + ((buf.get() & 0xFF) << 16);
    }

    /**
     * Reads a 4-byte integer with mixed byte order (custom transformation B).
     *
     * @return The transformed int value.
     */
    public int getIntB() {
        return ((buf.get() & 0xFF) << 16) + ((buf.get() & 0xFF) << 24) + (buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8);
    }

    /**
     * Reads an 8-byte long using custom method combining two getIntB calls.
     *
     * @return The reconstructed long value.
     */
    public long getLongL() {
        long first = getIntB();
        long second = getIntB();
        if (second < 0) second = second & 0xffffffffL;
        return (first << -41780448) + second;
    }

    /**
     * Reads a standard 8-byte (long) value from the buffer.
     *
     * @return The long value.
     */
    public long getLong() {
        return buf.getLong();
    }

    /**
     * Reads a smart value (variable-length short). Used in RS protocol.
     *
     * @return The smart value.
     */
    public int getSmart() {
        int peek = buf.get(buf.position());
        if (peek <= (0xFF & peek)) {
            return buf.get() & 0xFF;
        }
        return (buf.getShort() & 0xFFFF) - 32768;
    }

    /**
     * Reads a smart int value. Short if small, otherwise full int.
     *
     * @return The smart int value.
     */
    public int getIntSmart() {
        int peek = buf.getShort(buf.position());
        if (peek <= Short.MAX_VALUE) {
            return buf.getShort() & 0xFFFF;
        }
        return (buf.getInt() & 0xFFFFFFFF) - 32768;
    }

    /**
     * Reads a string from the buffer until a null-terminator or delimiter.
     *
     * @return The decoded string.
     */
    public String getString() {
        return ByteBufferExtensions.getString(buf);
    }

    /**
     * Reads a JAG-compliant string (used in RuneScape protocol).
     * First byte is a prefix, then string bytes follow.
     *
     * @return The decoded JAG string.
     */
    public String getJagString() {
        byte b = buf.get();
        if (b == 0) return "";
        return ((char) b) + ByteBufferExtensions.getString(buf);
    }

    /**
     * Reads a byte array from the buffer in reverse order with -128 transformation.
     *
     * @param is     Target byte array.
     * @param offset Starting offset in the array.
     * @param length Number of bytes to read.
     * @return This buffer for chaining.
     */
    public IoBuffer getReverseA(byte[] is, int offset, int length) {
        for (int i = (offset + length - 1); i >= offset; i--) {
            is[i] = (byte) (buf.get() - 128);
        }
        return this;
    }

    /**
     * Applies an ISAAC cipher to the opcode.
     *
     * @param cipher The ISAAC cipher instance.
     */
    public void cypherOpcode(ISAACCipher cipher) {
        this.opcode += (byte) cipher.getNextValue();
    }

    /**
     * Returns the underlying {@link ByteBuffer}.
     *
     * @return The ByteBuffer used by this buffer.
     */
    public ByteBuffer toByteBuffer() {
        return buf;
    }

    /**
     * Returns the packet opcode.
     *
     * @return The opcode.
     */
    public int opcode() {
        return opcode;
    }

    /**
     * Returns the number of readable bytes remaining in the buffer.
     *
     * @return Number of readable bytes.
     */
    public int readableBytes() {
        return buf.capacity() - buf.remaining();
    }

    /**
     * Returns the packet header type.
     *
     * @return The header.
     */
    public PacketHeader getHeader() {
        return header;
    }

    /**
     * Returns the underlying byte array of the buffer.
     *
     * @return The byte array.
     */
    public byte[] array() {
        return buf.array();
    }

    /**
     * Returns the packet size (if known).
     *
     * @return The packet size.
     */
    public int getPacketSize() {
        return packetSize;
    }

    /**
     * @param packetSize the packetSize to set.
     */
    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    /**
     * Gets the bitPosition.
     *
     * @return The bitPosition.
     */
    public int getBitPosition() {
        return bitPosition;
    }

}
