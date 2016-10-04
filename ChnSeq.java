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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author quincy1994
 */
public class ChnSeq {

    private TireNode tire = null;

    public List<String> loadFile() throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<String>();
        String filename = "wordFre.txt";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String tmp;
        while ((tmp = br.readLine()) != null) {
            lines.add(tmp);
        }
        br.close();
        return lines;
    }

    public void init() throws IOException {
        List<String> lines = loadFile();
        tire = new TireNode();

        for (String line : lines) {
            String[] tokens = line.split(",");
            String word = tokens[0];
            int freq = Integer.parseInt(tokens[1]);
            double antilog =  Math.log(1+0.01/Double.parseDouble(tokens[2].replace("%", ""))) ;
//            System.out.println(antilog);
            //构建词典树
            TireNode root = tire;
            for (int i = 0; i < word.length(); i++) {
                String c = "" + word.charAt(i);
                TireNode node = root.getChild(c);
                if (node == null) {
                    node = new TireNode();
                    node.setCharacter(c);
                    root.addChild(node);
                }
                root = node;
            }
            root.setFrequency(freq);    //为每个词设立词频
            root.setAntilog(antilog);   //为每个词设立逆文档频率
        }

    }

    public TireNode getTire() {
        return tire;
    }

    public TireNode getNodeByWord(String word) {
        TireNode node = tire;
        for (int i = 0; i < word.length(); i++) {
            String ch = word.charAt(i) + "";
            if (node == null) {
                break;
            } else {
                node = node.getChild(ch);
            }
        }
        return node;
    }

    private class Segment {

        public String word;     //词
        public String endChar; //结束词
        public String lastChar; //前缀词
        public double cost;

        public final static String START_SIGN = "<< STARTING >>";
        public final static String END_SIGN = "<< ENDING >>";
    }
    //寻找候选词

    public List<Segment> preSegment(String sentence) {
        List<Segment> segs = new ArrayList<Segment>();

        //设置句子的开始标志
        Segment terminal = new Segment();
        terminal.word = Segment.START_SIGN;
        terminal.endChar = Segment.START_SIGN;
        terminal.lastChar = null;
        segs.add(terminal);

        for (int i = 0; i < sentence.length(); i++) {
            for (int j = i + 1; j <= sentence.length(); j++) {
                String word = sentence.substring(i, j);
                TireNode tnode = this.getNodeByWord(word);
                if (tnode == null) {
                    break;
                }
                if (tnode.getFrequency() <= 0) {
                    continue;
                }

                Segment seg = new Segment();
                seg.word = word;
//                    System.out.println(word);
                seg.endChar = word.substring(word.length() - 1, word.length());
                if (i == 0) {
                    seg.lastChar = Segment.START_SIGN;
                } else {
                    seg.lastChar = sentence.substring(i - 1, i);
                }
                seg.cost = tnode.getAntilog();
                System.out.println(word + " " + seg.cost +" " + tnode.getFrequency());
                segs.add(seg);
            }
        }

        //设置句子的结束标志
        terminal = new Segment();
        terminal.word = Segment.END_SIGN;
        terminal.endChar = Segment.END_SIGN;
        terminal.lastChar = sentence.substring(sentence.length() - 1, sentence.length());
        segs.add(terminal);

        return segs;
    }

    public String dynamicSegment(List<Segment> segs) {

        //基于动态规划的概率统计分词
        final double INFINITE = 9999999;

        if (segs == null || segs.size() == 0) {
            System.out.println("找不到候选词");
            return null;
        }

        int n = segs.size();    //候选词的个数

        //单个词
        double[][] costs = new double[n][n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n; j++) {
                String endChar = segs.get(i).endChar;
                if (j == i && endChar.equals(segs.get(j).word)) {
                    costs[i][j] = segs.get(j).cost;    //候选词j的概率
                    continue;
                }
                costs[i][j] = INFINITE;
            }
        }

        //寻找前一个候选词
        for (int i = 0; i < n - 1; i++) {
            String endChar = segs.get(i).endChar;
            for (int j = i + 1; j < n; j++) {
                String lastChar = segs.get(j).lastChar;
                if (lastChar != null && lastChar.equals(endChar) &&( j- i < 4)) {       //ｊ前缀词不为空，同时ｊ的前缀词等于ｉ的后缀词
                    costs[i][j] = segs.get(j).cost;    //候选词j的概率
                }
            }
        }

        int sp = 0;   //开始点
        int fp = n - 1;    //结束点

        double[] dist = new double[n];         // 记录累计概率, n为候选词的个数
        List<List<Integer>> sPaths = new ArrayList<List<Integer>>();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            dist[i] = costs[sp][i];    //ｉ的累计概率的初始值为索引sp到索引ｉ的词的概率
            if (sp != i) {
                list.add(i);   //记录候选词的索引位置
            }
            if (dist[i] < INFINITE) {
                List<Integer> spa = new ArrayList<Integer>();     //如果索引sp到索引ｉ构成一个词，则开启一条划分路径
                sPaths.add(spa);
            } else {
                sPaths.add(null);
            }
        }
        while (!list.isEmpty()) {

            //选切分点
            Integer minIdx = list.get(0);
//            for (int i : list) {
//                if (dist[i] < dist[minIdx]) {
//                    minIdx = i;
//                }
//            }
            list.remove(minIdx);
            if(dist[minIdx] == INFINITE){
                continue;
            }
            for (int i = minIdx+1; i < n; i++) {
                if (dist[i] > dist[minIdx] + costs[minIdx][i]) {
                    dist[i] = dist[minIdx] + costs[minIdx][i];
                    List<Integer> tmp = new ArrayList<Integer>(sPaths.get(minIdx));
                    tmp.add(minIdx);
                    sPaths.set(i, tmp);  //记录最佳前候选词列表
                }
            }
        }
        //String[] result = new String[sPaths.get(fp).size()];
        String result = "";
        for (int i = 0; i < sPaths.get(fp).size(); i++) {
            result += segs.get(sPaths.get(fp).get(i)).word + "/ ";
        }
        return result;
    }

    public String segment(String sentences) {
//        String[] tokens= sentences.split("。");
//        String result = "";
//        for(String sentence : tokens){
//             result += dynamicSegment(preSegment(sentence)) + "。";
//        }
//        return result;
        return dynamicSegment(preSegment(sentences));
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        ChnSeq cs = new ChnSeq();
        cs.init();
        String sentence = "在这一年中，改革开放和现代化建设继续向前迈进。经济保持了“高增长、低通胀”的良好发展态势。农业生产再次获得好的收成，企业改革继续深化，人民生活进一步改善。对外经济技术合作与交流不断扩大。";
        String segs = cs.segment(sentence);
        System.out.println(segs);
    }
}
