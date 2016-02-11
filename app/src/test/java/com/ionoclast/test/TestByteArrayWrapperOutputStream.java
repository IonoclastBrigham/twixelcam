// TestByteArrayWrapperOutputStream.java
// Unit test for stream
// TwixelCam - Copyright © 2016 Brigham Toskin


package com.ionoclast.test;

import com.ionoclast.util.ByteArrayWrapperOutputStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class TestByteArrayWrapperOutputStream
{
	private byte[] mArray = new byte[16];
	private ByteArrayWrapperOutputStream mOutStream;

	@Before
	public void setup()
	{
		mOutStream = new ByteArrayWrapperOutputStream(mArray);
	}

	@Test
	public void testBackingArrayIdentity()
	{
		assertTrue(mArray == mOutStream.toByteArray());
	}

	@Test
	public void testWrite1()
	{
		mOutStream.write(-1);
		assertEquals(1, mOutStream.size());
		byte[] tBuf = mOutStream.toByteArray();
		assertEquals(-1, tBuf[0]);
	}

	@Test
	public void testWriteCapacityLoop()
	{
		mOutStream.reset();
		for(int i = 0; i < mOutStream.capacity(); ++i)
		{
			mOutStream.write(i);
		}

		assertEquals(mOutStream.capacity(), mOutStream.size());

		byte[] tBuf = mOutStream.toByteArray();
		for(int i = 0; i < tBuf.length; ++i)
		{
			assertEquals(i, tBuf[i]);
		}
	}

	@Test
	public void testWriteCapacityBuffered()
	{
		byte[] tData = new byte[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		mOutStream.reset();
		mOutStream.write(tData, 0, tData.length);

		assertEquals(mOutStream.capacity(), mOutStream.size());

		byte[] tBuf = mOutStream.toByteArray();
		for(int i = 0; i < tBuf.length; ++i)
		{
			assertEquals(15 - i, tBuf[i]);
		}
	}

	@Test
	public void testWriteOverCapacity()
	{
		mOutStream.reset();
		int i = 0;
		try
		{
			for(; i <= mOutStream.capacity(); ++i)
			{
				mOutStream.write(i);
			}
		}
		catch(UnsupportedOperationException e)
		{
			// should have run from 0 and fall off the end
			assertEquals(mOutStream.capacity(), i);

			// should be full
			assertEquals(mOutStream.capacity(), mOutStream.size());

			return;
		}
		fail("Did not fill past capacity");
	}
}