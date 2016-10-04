/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author quincy1994
 */
public class TireNode {
    private String character;           //　单个汉字
    private int frequency = -1;       //     词频, -1来区别某条路径上的字串是否是一个词组
    private double antilog = -1;    //      对数化的词频
    private Map<String, TireNode> children;  //下一个节点
    
    public String getCharacter(){
        return character;
    }
    
    public void setCharacter(String character){
        this.character = character;
    }
    
    public int getFrequency(){
        return frequency;
    }
    
    public void setFrequency(int frequency){
        this.frequency = frequency;
    }
    
    public double getAntilog(){
        return antilog;
    }
    
    public void setAntilog(double antilog){
        this.antilog = antilog;
    }
    
    public void addChild(TireNode node){
        if (children == null){
            children = new HashMap<String, TireNode>();
        }
        if (!children.containsKey(node.getCharacter())){
            children.put(node.getCharacter(), node);
        }
    }
    
    public TireNode getChild(String ch){
        if (children == null || ! children.containsKey(ch)){
            return null;
        }
        return children.get(ch);
    }
    
    public void removeChildren(String ch){
        if (children == null || !children.containsKey(ch)){
            return;
        }
        children.remove(ch);
    }
}
