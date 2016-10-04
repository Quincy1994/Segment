/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author quincy1994
 */
public class Nlp {

    private String m_sResult = ""; // 切分后的结果串
    private int m_nPosIndex;  // 指向待切分语料的指针的具体位置
    private int m_MaxLen; // 最大取词长
    private int totalMaxLen; //总最大取词长
    private Set<String> dictionary; // 分词字典
    
    public Nlp(int maxLen){
        this.m_MaxLen = maxLen;
        this.m_nPosIndex = 0;
        this.totalMaxLen = maxLen;
        try {
            this.dictionary = this.loadFile();
        } catch (IOException ex) {
            Logger.getLogger(Nlp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Nlp(){
        this.m_MaxLen = 3;
        this.totalMaxLen = 3;
        this.m_nPosIndex = 0;
        try {
            this.dictionary = this.loadFile();
        } catch (IOException ex) {
            Logger.getLogger(Nlp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Set<String> loadFile() throws FileNotFoundException, IOException{
        Set<String> dictionary = new HashSet<String>();
        String filename = "dict.txt";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String tmp;
        while( ( tmp = br.readLine() )!=null){
            String[] token = tmp.split(",");
            String word = token[0];
            dictionary.add(word);
        }
        return dictionary;
    }
    public String MMSegment(String source){
         int len = totalMaxLen;
         int frompos = 0;
         MM(source, len, frompos);
         return m_sResult;
     }
    public String getSubString(String source, int m_nPosIndex, int len){
        int endIndex = m_nPosIndex + len;
        int length = source.length();
        while(endIndex > length){
            endIndex -= 1;
        }
        String sub = source.substring(m_nPosIndex, endIndex);
        return sub;
    }
    public void MM(String source, int len , int frompos){
         if (m_nPosIndex >= source.length()) return;
        String sub = getSubString(source, m_nPosIndex,len);
        if(dictionary.contains(sub)){
            //匹配
            m_sResult += sub + "/ ";
            m_nPosIndex = m_nPosIndex + m_MaxLen;
            m_MaxLen = totalMaxLen;
            MM(source, m_MaxLen, m_nPosIndex);
        }
        else{
            //不匹配
            if(m_MaxLen > 1){
                m_MaxLen = m_MaxLen - 1;
                MM(source, m_MaxLen, m_nPosIndex);
            }
            else{
//                m_sResult += "字典中没有(" + sub +")字/ ";
                m_sResult += sub+ "/ ";
                m_nPosIndex  += 1;
                m_MaxLen = totalMaxLen;
                MM(source, m_MaxLen, m_nPosIndex);
            }
    }
}
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Nlp nlp = new Nlp();
        String source = "今天天气不错！";
        String result = nlp.MMSegment(source);
        System.out.println(result);
    } 
}
