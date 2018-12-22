package com.mycompany.getparsetree;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentRemover {
    
public static LinkedList<JSONObject> jsonList = new LinkedList<JSONObject>();
//    private static final String FILE_PATH = "G:/Lab/Top1000JavaWarehouse/output/javafile.txt";
//    private static final String FILE_PATH = "G:/Lab/Top1000JavaWarehouse/output/first100aa_Path_Comment_Method(withComment).java";

    public static void main(String[] args) throws Exception {
            File fr = new File("H:/repo/1000/outputjson/1000ag_Path_Comment_Method(withComment).json");// 读取原始json文件 
            InputStreamReader read = new InputStreamReader(new FileInputStream(fr), "utf-8");
            BufferedReader brjson = new BufferedReader(read);
//            System.out.println("read old file");

            File fw = new File("H:/repo/1000/outputjson/1000ag_Path_Comment_Method.json");
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(fw), "utf-8");
            BufferedWriter bw = new BufferedWriter(write);
//            System.out.println("creat new file");
        
            String repoPath = "H:/repo/1000/output/1000agjavafile.txt";
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(repoPath)));
            String s = null ;
            JSONArray features = new JSONArray(brjson.readLine());//这个地方有可能抛异常（找不到文件之类的）
            

            int index = -1;
            while((s = br.readLine())!=null){
               
                index++;
                try{
//                    System.out.println(index);

//                    System.out.println(br.readLine());
                    CompilationUnit cu = JavaParser.parse(new File(s));
//                    System.out.println("jiexiGOOD");

                    List<Comment> comments = cu.getAllContainedComments();
                    List<Comment> unwantedComments = comments
                        .stream()
                        .filter(p -> !p.getCommentedNode().isPresent() || p instanceof Comment)
                        .collect(Collectors.toList());
                    unwantedComments.forEach(Node::remove);
                    cu.removePackageDeclaration();
                    cu.getImports().remove(0); 
//                    System.out.println("ASTGOOG");
                    List<MethodDeclaration> methodDeclarations =cu.findAll(MethodDeclaration.class);
                    for(MethodDeclaration md : methodDeclarations){
                        if (md.getBody() != null){
                             
                             String M = md.getBody().toString().substring(9, md.getBody().toString().length()-1);
                             M = M.replaceAll("\r\n\r\n", "\r\n");
//                             System.out.println(M);
                             JSONObject info = features.getJSONObject(index);
//                             System.out.println(info.getString("Path"));
                             
                              JSONObject jsonObject = new JSONObject();
                              jsonObject.put("Path", info.getString("Path")); 
                              jsonObject.put("Comment", info.getString("Comment"));  
                              jsonObject.put("Method", info.getString("Head")+M);
                              jsonList.add(jsonObject);
                        }
                    }
                
                }
                catch(Exception e){
//                    System.out.println("BadMethod!!");
//                        e.printStackTrace();
            }
            }
             bw.write(jsonList.toString());
             bw.close();
    }
}