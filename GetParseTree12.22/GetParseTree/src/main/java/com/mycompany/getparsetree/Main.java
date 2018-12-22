package com.mycompany.getparsetree;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.*;
import org.json.JSONException;  
import org.json.JSONObject;











public class Main{

	public static String[] names = null; //{"cocos2d"};//zxing
	public static String repoName = null;

	private static int num = 0;
        public static LinkedList<JSONObject> jsonList = new LinkedList<JSONObject>();

	public static void main(String args[]) throws IOException{
//数据集
		Properties props = new Properties();
		props.load(new FileInputStream("G:/Lab/code_comment/java/param.properties"));
		String param1 = props.getProperty("repoNames");
		names = param1.split(",");
		

		Bmcl.vocabulary = new Vocabulary(Bmcl.dim);
		//Bmcl.codeFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/parseTree.txt");
		//Bmcl.codePW = new PrintWriter(Bmcl.codeFW);

System.out.println("Arg comment: " + Bmcl.comment);

		try{

			Bmcl.getMethodCallGraph();//methodcallgraph调用图

		}catch (Exception e){
			e.printStackTrace();
		}

		try{
//			FileWriter fw = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/comments.txt");
//			PrintWriter pw = new PrintWriter(fw);

			for (int i = 0; i < names.length; ++i){
		System.out.println("handle " + names[i]);
				String repoFile = "H:/repo/1000/"+names[i]+"javaFile.txt";
				ThroughOneRepo throughOneRepo = new ThroughOneRepo(repoFile);
				num += throughOneRepo.extractComments();//额外注释的数量
		System.out.println("");
			}

//			pw.close();
		}catch (Exception e){
			e.printStackTrace();
		}

		System.out.println("number of comments:" + num);//有注释的方法

// generate functionIndexToName and functionNameToIndex
		Bmcl.indexFunction();
//		System.out.println("size: " + Bmcl.functionIndexToName.size());

		System.out.println("number of methods: " + Bmcl.functionIndexToName.size());//方法总数

////--------------------------------Vocabulary------------------------------------------------------------
//
//		
//		for (Entry<String,GenParseTree> en : Bmcl.codeParseTrees.entrySet()){
//		    if (en.getValue().getParseTree() != null)
//			en.getValue().getParseTree().addWord();
//		}
//
//		Bmcl.vocabulary.codeWordFilter();
//
//		System.out.println("begin to write vocabulary");
//		HashMap<Integer, String> codeIndexToWord = Bmcl.vocabulary.getCodeIndexToWord();
//		HashMap<Integer, Integer> codeWordNums = Bmcl.vocabulary.getCodeWordNums();
//		FileWriter vocabFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/vocabularyMap.txt");
//		PrintWriter vocabPW = new PrintWriter(vocabFW);
//
//		for (Entry<Integer, String> en : codeIndexToWord.entrySet()){
////			    vocabPW.println(en.getKey() + "," + en.getValue()+ "," + codeWordNums.get(en.getKey()));
////                                         编号                 单词string                          出现次数
//			    vocabPW.println(en.getKey() + "," + en.getValue().replace("\n","")+ "," + codeWordNums.get(en.getKey()));
//		}
//
//		vocabPW.close();
//		System.out.println("size of vocabulary: " + Bmcl.vocabulary.getCodeWordToIndex().size());//vocabulary总数

////---------------------------------inter node---------------------------------
//		System.out.println("begin to write internode map");
//		HashMap<Integer,String> interNodeToName = Bmcl.vocabulary.getInterNodeToName();
//		FileWriter interFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/interMap.txt");
//		PrintWriter interPW = new PrintWriter(interFW);
//
//		for (Entry<Integer, String> en : interNodeToName.entrySet()){
//                    //             编号                  internode
//		    interPW.println(en.getKey() + " , " + en.getValue());
//		}
////                               最后编号+1=总数            unk
//		interPW.println(interNodeToName.size() + " , " + "UNK");
//		interPW.close();
//
//		System.out.println("size of inter node type: " + interNodeToName.size());

////--------------------------------Method Map------------------------------------------------------------
	//	System.out.println("begin to write method map");
//		
//		FileWriter parseFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/methodHead.txt");
//		PrintWriter parsePW = new PrintWriter(parseFW);
//
//		for (Entry<String,LinkedList<String>> en : Bmcl.methodCodes.entrySet()){
//                    //               
//			parsePW.println(en.getKey() + " , " + en.getValue());
//		}
//		
//		parsePW.close();

//--------------------------------Parse Tree------------------------------------------------------------
//
//		System.out.println("begin to write parse tree");
////		System.out.print("progress: ");
//		int count = 0;
//		int numOfMethod = 0;
//		for (Entry<String,GenParseTree> en : Bmcl.codeParseTrees.entrySet()){
//			
//			if (Bmcl.functionNameToIndex.containsKey(en.getKey())){
//				numOfMethod++;
//			}
//
//		}
////词数+方法数
//		Bmcl.codePW.println(Bmcl.vocabulary.getCodeSize() + " " + numOfMethod);
//
//		for (Entry<String,GenParseTree> en : Bmcl.codeParseTrees.entrySet()){
///*			count++;
//			System.out.print(count);
//			for (int j = 0; j < String.valueOf(count).length(); j++) {
//                                    System.out.print("\b");
//                                }
//		*/
//			if (Bmcl.functionNameToIndex.containsKey(en.getKey())){
//				Bmcl.OwnMethodName = en.getKey();
//				Bmcl.codePW.println("#"+Bmcl.functionNameToIndex.get(en.getKey()));
//                                //#+方法序号
////			if (!Bmcl.functionNameToIndex.containsKey(en.getKey())){
////System.out.println(en.getKey());
////			}
//			    if (en.getValue().getParseTree() != null)
//				en.getValue().getParseTree().DFS();
//			}
//		}
////		System.out.println("");
//			Bmcl.codePW.close();

//-------------------------------Method Comment Map-----------------------------------------------------


		

                String src = "H:/repo/1000/outputjson/" + names[0]+"_Path_Comment_Method(withComment).json";
                File file = new File(src);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                file.delete();
                file.createNewFile();
                } catch (IOException e) {
                e.printStackTrace();
                }              
                FileWriter fw = new FileWriter(file, true);
                PrintWriter pw = new PrintWriter(fw);
                
               

                FileWriter writertxt = new FileWriter("H:/repo/1000/output/" + names[0]+"javafile.txt", true);  


//                
		for (Entry<String,String> en : Bmcl.methodCommentMap.entrySet())
			if (Bmcl.functionNameToIndex.containsKey(en.getKey())){
				if (Bmcl.codeParseTrees.get(en.getKey()).getParseTree() != null && Bmcl.codeParseTrees.get(en.getKey()).getParseTree().length() > 1){
				//方法名和编号	

                                  
                                        String head = new String();
                                        for(int i = 0; i < Bmcl.methodCodes.get(en.getKey()).size(); i ++){
                                            head = head + Bmcl.methodCodes.get(en.getKey()).get(i);
                                            if(i ==1){
                                                head = head + "(";
                                            }
                                            else {
                                                if(i % 2 == 1&& i!=Bmcl.methodCodes.get(en.getKey()).size()-1){
                                                    head = head + ",";
                                                }
                                                else{
                                                    if(i!=Bmcl.methodCodes.get(en.getKey()).size()-1){
                                                        head = head + " ";
                                                    }
                                                    
                                                }
                                            }
                                        }
                                        head = head + ")";
//                                        System.out.println(head);

                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Path", en.getKey()); 
                                        jsonObject.put("Comment", en.getValue().trim()); 
                                        jsonObject.put("Head", head);
                                        jsonObject.put("Method", head+Bmcl.methodBodyMap.get(en.getKey()));
                                        jsonList.add(jsonObject);
                                        
//                                        System.out.println(en.getValue().trim());
                                        
                                        
                                        String srcjava = "H:/repo/1000/output/"+en.getKey()+".java";
                                        writertxt.write(srcjava);
                                        writertxt.write("\n");
                                        FileWriter writer = new FileWriter(srcjava, true);  
                                        String javahead =   "package org.webbitserver.handler;\n" +
                                                            "\n" +
                                                            "import org.webbitserver.HttpControl;\n" +                                                          
                                                            "\n" +
                                                            "public abstract class AbstractResourceHandler implements HttpHandler {"
                                    ;
                        
                                        writer.write (javahead);
                                        writer.write(head+Bmcl.methodBodyMap.get(en.getKey()));  
                                        writer.write("}");
                                        writer.close(); 
                
//                                        pwjava.println(head+Bmcl.methodBodyMap.get(en.getKey()));
//                                       System.out.println(head+Bmcl.methodBodyMap.get(en.getKey()));
                                  

                                }
                        }
		
		pw.write(jsonList.toString());
                pw.close();

                

//--------------------------------------End-------------------------------------------------------------
//
//		System.out.println(" true " + Bmcl.abc1);
//		System.out.println(" false " + Bmcl.abc2);
//		System.out.println(" equal " + Bmcl.abc3);
//
//		getMethodsBody();
//		getMethodsFullBody();
                
              
	}

//	static void getMethodsBody(){
//	    try{
//	    FileWriter mbFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/methodBodyMap.txt");
//	    PrintWriter mbPW = new PrintWriter(mbFW);
//
//	    FileWriter mbOriFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/methodOriBodyMap.txt");
//	    PrintWriter mbOriPW = new PrintWriter(mbOriFW);
//
//	    for (Entry<String,String> en : Bmcl.methodBodyMap.entrySet()){
//		String body = en.getValue().replace("{"," ").replace("\n"," ").replace("\""," ").replace("}"," ").replace("("," ").replace(")"," ").replace(";"," ").replace("."," ").replace(","," ").replace("\'"," ").replace("\\"," ").replace("|"," ").replace("!"," ").replace("&"," ").replace("\t"," ").replace("["," ").replace("]"," ").replace("<"," ").replace("="," ").replace(">"," ").replace(":"," ").replace("?", " ").replace("/"," ").replace("%"," ").replace("+"," ").replace("_"," ").replace("-"," ").replace("*"," ").replace("#"," ").replace("@"," ").replace("^"," ");
//		String [] bodyVocabulary = body.split(" ");
//
////		System.out.println(Bmcl.functionNameToIndex.get(en.getKey())+"====================");	
//		String oneBodyWords = "";
//
//		List<String> oneBodyVocabulary = new LinkedList<String>();
//		for (String a : bodyVocabulary){
//		    if (!a.equals("")){
//			if (MyVisit.split(a).size() > 0){
//			    List<String> splitWords = MyVisit.split(a).get(0);
////			    System.out.println(splitWords.toString());
//			    for (String b : splitWords){
//				oneBodyVocabulary.add(b);
//				oneBodyWords += " " + b;
//			    }
//			}
//		    }
//		}
////		System.out.println(oneBodyVocabulary.toString());
////		System.out.println(oneBodyWords);
////		if (!oneBodyWords.equals("")){
//		    if (Bmcl.functionNameToIndex.containsKey(en.getKey())){
//			mbPW.println(Bmcl.functionNameToIndex.get(en.getKey())+" ,"+oneBodyWords);
//			mbOriPW.println(Bmcl.functionNameToIndex.get(en.getKey())+" ,"+en.getValue().replace("\n"," ").replace("\t"," "));
//		    }
////		}
//	    }
//	    System.out.println(Bmcl.methodBodyMap.size());
//
//	    mbPW.close();
//	    mbOriPW.close();
//	    }catch(Exception e){
//		e.printStackTrace();
//	    }
//
//	}
//
//	static void getMethodsFullBody(){
//	    try{
//	    FileWriter mbFW = new FileWriter("F:/Lab/sjtu_code_comments/CodeComment_Code/GetParseTree/output/methodFullBodyMap.txt");
//	    PrintWriter mbPW = new PrintWriter(mbFW);
//
//	    for (Entry<String,String> en : Bmcl.methodBodyMap.entrySet()){
////		String body = en.getValue().replace("{"," ").replace("\n"," ").replace("\""," ").replace("}"," ").replace("("," ").replace(")"," ").replace(";"," ").replace("."," ").replace(","," ").replace("\'"," ").replace("\\"," ").replace("|"," ").replace("!"," ").replace("&"," ").replace("\t"," ").replace("["," ").replace("]"," ").replace("<"," ").replace("="," ").replace(">"," ").replace(":"," ").replace("?", " ").replace("/"," ").replace("%"," ").replace("+"," ").replace("_"," ").replace("-"," ").replace("*"," ").replace("#"," ").replace("@"," ").replace("^"," ");
//
//		String body = en.getValue().replace("\t"," ").replace("\n"," ");
//
//		    if (Bmcl.functionNameToIndex.containsKey(en.getKey())){
//			mbPW.println(Bmcl.functionNameToIndex.get(en.getKey())+" ,"+body);
//		    }
//	    }
//	    System.out.println(Bmcl.methodBodyMap.size());
//
//	    mbPW.close();
//	    }catch(Exception e){
//		e.printStackTrace();
//	    }
//
//	}
}
