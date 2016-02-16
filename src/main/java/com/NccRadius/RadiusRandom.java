package com.NccRadius;

import java.util.Random;

/**
 * A Random Number Generator (wrapper) for JRadius
 *
 * @author David Bird
 */
public class RadiusRandom
{
    static final Random rand = new Random();

    /**
     * Generates an array of random bytes.
     * @param length number of random bytes to generate
     * @return array of random bytes
     */
    public static byte[] getBytes(int length)
    {
        byte result[] = new byte[length];
        synchronized (rand)
        {
            for (int i = 0; i < length; i++)
            {
                try
                {
                    result[i] ^= rand.nextInt();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String getRandomPassword(int length)
    {
        String pseudo[] = { "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "m", "n", "o", "p", "q", "r", "u", "s", "t", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        StringBuffer out = new StringBuffer(length);
        byte[] in = getBytes(length);
        for (int i=0; i < length; i++)
        {
            out.append(pseudo[((char)in[i]) % pseudo.length]);
        }
        String rslt = new String(out);
        return rslt;
    }

    public static String getRandomPassword(int length, String allowedCharacters)
    {
        StringBuffer out = new StringBuffer(length);
        byte[] in = getBytes(length);
        for (int i=0; i < length; i++)
        {
            out.append(allowedCharacters.charAt(((char)in[i]) % allowedCharacters.length()));
        }
        String rslt = new String(out);
        return rslt;
    }

    public static String getRandomString(int length)
    {
        return RadiusUtils.byteArrayToHexString(getBytes(length));
    }
}