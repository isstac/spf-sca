package multirun.adaptive.tree;

import java.io.*;

public class LZ77T
{
    protected static final int mBufferSize = 1024;


  public static byte[] compress(final byte[] in) throws IOException {
       	
    StringBuffer mSearchBuffer = new StringBuffer(1024);
    
    String result = "";
    
    String currentMatch = "";
    int matchIndex = 0;
    int tempIndex = 0;
    int nextChar;
    //while ((nextChar = mIn.read()) != -1) {
    for (int i = 0; i < in.length; i++){
    	//System.out.println("i "+i+" length "+in.length);
    	nextChar = in[i];
    	
        tempIndex = mSearchBuffer.indexOf(currentMatch + (char)nextChar);
        if (tempIndex != -1) {
            currentMatch += (char)nextChar;
            matchIndex = tempIndex;
        }
        else {
            final String codedString = new StringBuilder().append("~").append(matchIndex).append("~").append(currentMatch.length()).append("~").append((char)nextChar).toString();
            final String concat = currentMatch + (char)nextChar;
            if (codedString.length() <= concat.length()) {
                //mOut.print(codedString);
            	result=result+codedString;
                mSearchBuffer.append(concat);
                currentMatch = "";
                matchIndex = 0;
            }
            else {
                for (currentMatch = concat, matchIndex = -1; currentMatch.length() > 1 && matchIndex == -1; currentMatch = currentMatch.substring(1, currentMatch.length()), matchIndex = mSearchBuffer.indexOf(currentMatch)) {
                    //mOut.print(currentMatch.charAt(0));
                	result=result+currentMatch.charAt(0);
                    mSearchBuffer.append(currentMatch.charAt(0));
                }
            }
            if (mSearchBuffer.length() <= 1024) {
                continue;
            }
            mSearchBuffer = mSearchBuffer.delete(0, mSearchBuffer.length() - 1024);
        }
    }
    if (matchIndex != -1) {
        final String codedString = new StringBuilder().append("~").append(matchIndex).append("~").append(currentMatch.length()).toString();
        if (codedString.length() <= currentMatch.length()) {
            //mOut.print(new StringBuilder().append("~").append(matchIndex).append("~").append(currentMatch.length()).toString());
            result=result+new StringBuilder().append("~").append(matchIndex).append("~").append(currentMatch.length()).toString();
        }
        else {
            //mOut.print(currentMatch);
        	result=result+currentMatch;
        }
    }
    //mIn.close();
    //mOut.flush();
    final byte[] bytes = result.getBytes();//oStream.toByteArray();
    //mOut.close();
    return bytes;
}

}
