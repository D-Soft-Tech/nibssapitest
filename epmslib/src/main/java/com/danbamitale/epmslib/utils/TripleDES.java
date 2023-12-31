package com.danbamitale.epmslib.utils;
/**
 * @author Derek
 * 
 */

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TripleDES {
	/**
	 * get correct length key for triple DES operation
	 * @param key
	 * @return
	 */
	private static byte[] GetKey(byte[] key)
	{
		byte[] bKey = new byte[24];
		int i;
		
		if (key.length == 8)
		{
			for (i=0; i<8; i++)
			{
				bKey[i] = key[i];
				bKey[i+8] = key[i];
				bKey[i+16] = key[i];
			}
		}
		else if (key.length == 16)
		{
			for (i=0; i<8; i++)
			{
				bKey[i] = key[i];
				bKey[i+8] = key[i+8];
				bKey[i+16] = key[i];
			}
		}
		else if (key.length == 24)
		{
			for (i=0; i<24; i++)
				bKey[i] = key[i];
		}
		
		return bKey;
	}
	
	/**
	 * encrypt data in ECB mode
	 * @param data
	 * @param key
	 * @return
	 */
    public static byte[] encrypt(byte[] data, byte[] key)
    {

    	SecretKey sk = new SecretKeySpec(GetKey(key), "DESede");
    	try {
    		Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
    		cipher.init(Cipher.ENCRYPT_MODE, sk);
			return cipher.doFinal(data);
        } catch (NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		}

    	return null;
    }

    /**
     * decrypt data in ECB mode
     * @param data
     * @param key
     * @return
     */
    public static byte[] decrypt(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(GetKey(key), "DESede");
    	try {
    		Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
    		cipher.init(Cipher.DECRYPT_MODE, sk);
			byte[] enc = cipher.doFinal(data);
			return enc;
        } catch (NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		}

    	return null;
    }

    /**
     * encrypt data in CBC mode
     * @param data
     * @param key
     * @return
     */
    public static byte[] encrypt_CBC(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(GetKey(key), "DESede");
    	try {
    		Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
    		cipher.init(Cipher.ENCRYPT_MODE, sk);
    		byte[] enc = new byte[data.length];
    		byte[] dataTemp1 = new byte[8];
    		byte[] dataTemp2 = new byte[8];
    		for (int i=0; i<data.length; i+=8)
    		{
    			for (int j=0; j<8; j++)
    				dataTemp1[j] = (byte)(data[i+j] ^ dataTemp2[j]);
    			dataTemp2 = cipher.doFinal(dataTemp1);
    			for (int j=0; j<8; j++)
    				enc[i+j] = dataTemp2[j];
    		}
			return enc;
        } catch (NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		}

    	return null;
    }

    public static byte[] encrypt_CBC(byte[] data, byte[] key, byte[] IV)
    {
    	SecretKey sk = new SecretKeySpec(GetKey(key), "DESede");
    	try {
    		Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
    		cipher.init(Cipher.ENCRYPT_MODE, sk);
    		byte[] enc = new byte[data.length];
    		byte[] dataTemp1 = new byte[8];
    		byte[] dataTemp2 = new byte[8];
    		for (int i=0; i<8; i++)
    			dataTemp2[i] = IV[i];
    		for (int i=0; i<data.length; i+=8)
    		{
    			for (int j=0; j<8; j++)
    				dataTemp1[j] = (byte)(data[i+j] ^ dataTemp2[j]);
    			dataTemp2 = cipher.doFinal(dataTemp1);
    			for (int j=0; j<8; j++)
    				enc[i+j] = dataTemp2[j];
    		}
			return enc;
        } catch (NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		}

    	return null;
    }

    /**
     * decrypt data in CBC mode
     * @param data
     * @param key
     * @return
     */
    public static byte[] decrypt_CBC(byte[] data, byte[] key)
    {
    	SecretKey sk = new SecretKeySpec(GetKey(key), "DESede");
    	try {
    		Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
    		cipher.init(Cipher.DECRYPT_MODE, sk);
			byte[] enc = cipher.doFinal(data);

			for (int i=8; i<enc.length; i++)
				enc[i] ^= data[i-8];

			return enc;
        } catch (NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        } catch (BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (NullPointerException e) {
		} 
    	
    	return null;
    }
    
    /**
     * encrypt data in ECB mode
     * @param data
     * @param key
     * @return
     */
    public static String encrypt(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = encrypt(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    /**
     * decrypt data in ECB mode
     * @param data
     * @param key
     * @return
     */
    public static String decrypt(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = decrypt(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    /**
     * encrypt data in CBC mode
     * @param data
     * @param key
     * @return
     */
    public static String encrypt_CBC(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = encrypt_CBC(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    /**
     * decrypt data in CBC mode
     * @param data
     * @param key
     * @return
     */
    public static String decrypt_CBC(String data, String key)
    {
    	byte[] bData, bKey, bOutput;
    	String result;
    	
    	bData = String2Hex(data);
    	bKey = String2Hex(key);
    	bOutput = decrypt_CBC(bData, bKey);
    	result = Hex2String(bOutput);
    	
    	return result;
    }

    /**
     * Convert Byte Array to Hex String
     * @param data
     * @return
     */
    public static String Hex2String(byte[] data)
    {
    	if(data == null) {
    		return "";
    	}
		String result = "";
		for (int i=0; i<data.length; i++)
		{
			int tmp = (data[i] >> 4);
			result += Integer.toString((tmp & 0x0F), 16);
			tmp = (data[i] & 0x0F);
			result += Integer.toString((tmp & 0x0F), 16);
		}
	
		return result;
    }
    
    /**
     * Convert Hex String to byte array
     * @param data
     * @return
     */
	public static byte[] String2Hex(String data)
	{
		byte[] result;
		
		result = new byte[data.length()/2];
		for (int i=0; i<data.length(); i+=2)
			result[i/2] = (byte)(Integer.parseInt(data.substring(i, i+2), 16));
		
		return result;
	}


}
