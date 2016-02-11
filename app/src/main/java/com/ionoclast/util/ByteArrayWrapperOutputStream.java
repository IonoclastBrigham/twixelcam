/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Based on java.io.ByteArrayOutputStream.
 *  Changes Copyright © 2016 Brigham Toskin
 */


package com.ionoclast.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


/**
 * A specialized {@link OutputStream} for class for writing content to a
 * wrapped byte array. As bytes are written to this stream, the byte array
 * may be expanded to hold more bytes. When the writing is considered to be
 * finished, a copy of the byte array can be requested from the class.
 *
 * @see ByteArrayInputStream
 */
public class ByteArrayWrapperOutputStream extends OutputStream
{
	/**
     * The byte array containing the bytes written.
     */
    protected byte[] mBuf;

    /**
     * The number of bytes written.
     */
    protected int mCount;

    /**
     * Constructs a new {@code ByteArrayOutputStream} with a backing array
     * If more than {@code pBackingArray.length} bytes are written to this
     * instance, and exception will be thrown.
     *
     * @param pBackingArray
     *            backing byte array; it will NOT be resized or copied.
     * @throws IllegalArgumentException
     *             if {@code pBackingArray} is null or 0-length.
     */
    public ByteArrayWrapperOutputStream(byte[] pBackingArray)
    {
        if(pBackingArray == null || pBackingArray.length == 0)
        {
            throw new IllegalArgumentException("null or empty array");
        }

	    mBuf = pBackingArray;
    }

    /**
     * Closes this stream. This releases system resources used for this stream.
     *
     * @throws IOException
     *             if an error occurs while attempting to close this stream.
     */
    @Override
    public void close() throws IOException
    {
        /**
         * Although the spec claims "A closed stream cannot perform output
         * operations and cannot be reopened.", this implementation must do
         * nothing.
         */
        super.close();
    }

    private void expand(int i)
    {
        /* Can the buffer handle @i more bytes, if not expand it */
        if (mCount + i <= mBuf.length) {
            return;
        }

        throw new UnsupportedOperationException("Cannot resize array");
    }

    /**
     * Resets this stream to the beginning of the underlying byte array. All
     * subsequent writes will overwrite any bytes previously stored in this
     * stream.
     */
    public synchronized void reset()
    {
        mCount = 0;
    }

    /**
     * Returns the total number of bytes written to this stream so far.
     *
     * @return the number of bytes written to this stream.
     */
    public int size()
    {
        return mCount;
    }

	/**
	 * @return the storage capacity of the backing array.
	 */
	public int capacity()
	{
		return mBuf.length;
	}

    /**
     * Returns the contents of this ByteArrayWrapperOutputStream as a byte array.
     * Take care that your conceptions of the data remain in sync.
     * Modifying the array contents will affect the contents of the stream, and
     * writing to the stream will affect the contents of the returned array.
     * This could cause all kinds of havoc; <b>YOU HAVE BEEN WARNED.</b>
     *
     * @return this stream's current contents as a byte array.
     */
    public synchronized byte[] toByteArray()
    {
        return mBuf;
    }

    /**
     * Returns the contents of this ByteArrayOutputStream as a string. Any
     * changes made to the receiver after returning will not be reflected in the
     * string returned to the caller.
     *
     * @return this stream's current contents as a string.
     */
    @Override
    public String toString()
    {
        return new String(mBuf, 0, mCount);
    }

    /**
     * Returns the contents of this ByteArrayOutputStream as a string. Each byte
     * {@code b} in this stream is converted to a character {@code c} using the
     * following function:
     * {@code c == (char)(((hibyte & 0xff) << 8) | (b & 0xff))}. This method is
     * deprecated and either {@link #toString()} or {@link #toString(String)}
     * should be used.
     *
     * @param hibyte
     *            the high byte of each resulting Unicode character.
     * @return this stream's current contents as a string with the high byte set
     *         to {@code hibyte}.
     * @deprecated Use {@link #toString()} instead.
     */
    @Deprecated
    public String toString(int hibyte)
    {
        char[] newBuf = new char[size()];
        for (int i = 0; i < newBuf.length; i++)
        {
            newBuf[i] = (char) (((hibyte & 0xff) << 8) | (mBuf[i] & 0xff));
        }
        return new String(newBuf);
    }

    /**
     * Returns the contents of this ByteArrayOutputStream as a string converted
     * according to the encoding declared in {@code charsetName}.
     *
     * @param charsetName
     *            a string representing the encoding to use when translating
     *            this stream to a string.
     * @return this stream's current contents as an encoded string.
     * @throws UnsupportedEncodingException
     *             if the provided encoding is not supported.
     */
    public String toString(String charsetName) throws UnsupportedEncodingException
    {
        return new String(mBuf, 0, mCount, charsetName);
    }

    /**
     * Writes {@code mCount} bytes from the byte array {@code buffer} starting at
     * offset {@code index} to this stream.
     *
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the initial position in {@code buffer} to retrieve bytes.
     * @param len
     *            the number of bytes of {@code buffer} to write.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code len < 0}, or if
     *             {@code offset + len} is greater than the length of
     *             {@code buffer}.
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int len)
    {
//        Arrays.checkOffsetAndCount(buffer.length, offset, len);
        if (len == 0)
        {
            return;
        }
        expand(len);
        System.arraycopy(buffer, offset, mBuf, this.mCount, len);
        this.mCount += len;
    }

    /**
     * Writes the specified byte {@code oneByte} to the OutputStream. Only the
     * low order byte of {@code oneByte} is written.
     *
     * @param oneByte
     *            the byte to be written.
     */
    @Override
    public synchronized void write(int oneByte)
    {
        if (mCount == mBuf.length)
        {
            expand(1);
        }
        mBuf[mCount++] = (byte) oneByte;
    }

    /**
     * Takes the contents of this stream and writes it to the output stream
     * {@code out}.
     *
     * @param out
     *            an OutputStream on which to write the contents of this stream.
     * @throws IOException
     *             if an error occurs while writing to {@code out}.
     */
    public synchronized void writeTo(OutputStream out) throws IOException
    {
        out.write(mBuf, 0, mCount);
    }
}
