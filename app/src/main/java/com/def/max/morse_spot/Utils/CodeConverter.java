package com.def.max.morse_spot.Utils;

import java.util.HashMap;

public class CodeConverter {

	private static String[] ALPHA = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
			"s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "!", ",", "?",
			".", "'","@","=","-","/",":"};
	private static String[] MORSE = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..",
			"--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", ".----",
			"..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", "-----", "-.-.--", "--..--",
			"..--..", ".-.-.-", ".----.",".--.-.","-...-","-....-","-..-.","---..."};

	private static HashMap<String, String> ALPHA_TO_MORSE = new HashMap<>();

    static
    {
        for (int i = 0; i < ALPHA.length  &&  i < MORSE.length; i++)
        {
            ALPHA_TO_MORSE.put(ALPHA[i], MORSE[i]);
        }
    }

    public static String alphaToMorse(String englishCode)
    {
        StringBuilder builder = new StringBuilder();
        String[] words = englishCode.trim().split(" ");

        for (String word : words)
        {
            for (int i = 0; i < word.length(); i++)
            {
                String morse = ALPHA_TO_MORSE.get(word.substring(i, i + 1).toLowerCase());

                if (morse != null)
                {
                    builder.append(morse).append(" ");
                }
                else
                {
                    builder.append(word.substring(i, i + 1).toLowerCase()).append(" ");
                }
            }

            builder.append("  ");
        }

        return builder.toString();
    }

    public static String alphaToBinary(String alphaText)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : alphaText.toCharArray())
        {
            stringBuilder.append(Integer.toBinaryString(c)).append(" ");
        }

        return stringBuilder.toString();
    }

}