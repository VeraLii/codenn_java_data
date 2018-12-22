package com.mycompany.getparsetree.callGraph;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.github.javaparser.TokenMgrError;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ParseException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.ImportDeclaration;
import com.mycompany.getparsetree.Bmcl;
import com.mycompany.getparsetree.MyFileRepresenter;

public class GenMethodCallGraph{
	
	private LinkedList<String> codes = new LinkedList<String>();//代码
	private LinkedList<String> comments = new LinkedList<String>();//注释
	//private HashMap<String,LinkedList<String>> methodCodes = new HashMap<String,LinkedList<String>>();//方法代码
	private HashMap<String,LinkedList<String>> methodComments = new HashMap<String, LinkedList<String>>();//方法注释
	private String ownPackage;//所在包
	private LinkedList<String> ownImport = new LinkedList<String>();//引用
	private HashMap<String,Integer> classNameIndex = new HashMap<String,Integer>();//类名-编号
	private HashMap<String,String> variableType = new HashMap<String,String>();//变量类型
	private String className = null;
	private HashMap<String,MyFileRepresenter> classFileRep = new HashMap<String,MyFileRepresenter>();//类名+（文件名+代码+注释+方法代码+方法注释）

	private HashMap<String,String> pairs = new HashMap<String,String>();
	
	private String filePath;

	private HashMap<String,LinkedList<String>> methodCallGraph = new HashMap<String,LinkedList<String>>();

	private LinkedList<String> methodCallTopoSort = new LinkedList<String>();

	private Element methods;

	public Element getMethods(){
		return methods;
	}

	public HashMap<String,String> getPairs(){
		return pairs;
	}

	public LinkedList<String> getMethodCallTopoSort(){
		return methodCallTopoSort;
	}

	public HashMap<String,LinkedList<String>> getMethodCallGraph(){
		return methodCallGraph;
	}
	
	public String getClassName(){
		return className;
	}
	public HashMap<String,String> getVariableType(){
		return variableType;
	}

	public LinkedList<String> getCodes(){
		return codes;
	}

	public LinkedList<String> getComments(){
		return comments;
	}

//	public HashMap<String, LinkedList<String>> getMethodCodes(){
//		return methodCodes;
//	}

	public HashMap<String, LinkedList<String>> getMethodComments(){
		return methodComments;
	}

	public GenMethodCallGraph(String filePath, HashMap<String,Integer> classNameIndex,HashMap<String,MyFileRepresenter> classFileRep,Element methods)throws ParseException, FileNotFoundException, IOException {
		this.classNameIndex = classNameIndex;
		this.classFileRep = classFileRep;
		this.methods = methods;
		this.filePath = filePath;

		FileInputStream in = new FileInputStream(filePath);
		CompilationUnit cu;

		try {
			cu = JavaParser.parse(in);//是将java代码的文本解析为一棵CompilationUnit类型的树
		
		List<Comment> cmts = cu.getComments();//??
		for (Comment c: cmts){
			String raw = c.getContent();
			raw = raw.replace('\n',' ');
			comments.add(raw);
		}//将java代码中的comment放入comments(Linklist<string>)，将换行用空格替代

		ownPackage = cu.getPackage().getName().toString();//获得package
		if (cu.getImports() != null){
			for (ImportDeclaration n : cu.getImports()){
				ownImport.add(n.getName().toString());//获得import
			}
		}
		if (cu.getTypes() != null){
			for (TypeDeclaration t : cu.getTypes()){
				if (t instanceof ClassOrInterfaceDeclaration)
					parseWords((ClassOrInterfaceDeclaration)t);
				else if (t instanceof EnumDeclaration)
					parseWords((EnumDeclaration)t);
			}
		}

		VisitImportPackage vIp = new VisitImportPackage();//访问import包，把包名放进去
		vIp.visit(cu,null);
		LinkedList<String> vCodes = vIp.getCodes();
		if (vCodes != null)
			for (int tmp = 0; tmp < vCodes.size(); ++tmp)
				//codes.add("importpackage");
                                codes.add(vCodes.get(tmp));

		
		}catch (ParseException e){
                                        ;
                                }catch (NullPointerException e){
                                        e.printStackTrace();
                                }catch (RuntimeException e){
                                        ;
                                }catch (TokenMgrError e){
                                        ;
                                }
		 finally {
			in.close();
		}
		if (className == null){
			int pos1 = filePath.lastIndexOf('/');
			int pos2 = filePath.lastIndexOf('.');
			className = ownPackage+"."+filePath.substring(pos1+1,pos2);
		}
//System.out.println("GOOD");
		methodCallTopoSort = getTopology(methodCallGraph);//把所有key值放进去，应该是所有类名？
//		System.out.println("WHY  "+methodCodes);	
		addTopoSort(methodCallTopoSort);//拓扑排序
//System.out.println("STUDY");
	}

	private void addTopoSort(LinkedList<String> topoSort){
//		System.out.println(topoSort);
		for (String tmp:topoSort){
			if (Bmcl.methodCodes.containsKey(className+"."+tmp)){
//				System.out.println(tmp);
//				System.out.println("	"+Bmcl.methodCodes.get(className+"."+tmp));
				for(String str:methodCallGraph.get(tmp)){
//					System.out.println("123 "+str);
					if (Bmcl.methodCodes.containsKey(className+"."+str) && !tmp.equals(str)){
						for(String str1:Bmcl.methodCodes.get(className+"."+str)){//？？？这里是要干嘛？？？
//							System.out.println("3456 "+str1);
//这里出的异常不是空指针的异常,因为递归出现的异常
							Bmcl.methodCodes.get(className+"."+tmp).add(str1);
						}
					}
				}
//				System.out.println("QQQ "+Bmcl.methodCodes.get(className+"."+tmp));
			}
		}
	}

	public static LinkedList<String> getTopology(HashMap<String,LinkedList<String>> inGraph){//这个ingraph里面存放的是什么东西？？？
		LinkedList<String> res = new LinkedList<String>();
//System.out.println(inGraph);
		// outGraph 键值的函数被后面函数所引用
		HashMap<String,LinkedList<String>> outGraph = new HashMap<String,LinkedList<String>>();

		for (Entry<String,LinkedList<String>> et:inGraph.entrySet()){
			String method = et.getKey();
			LinkedList<String> _methods = et.getValue();
	//		System.out.print(method+" 	");

			if (!outGraph.containsKey(method)){
				LinkedList<String> tmp = new LinkedList<String>();
                                outGraph.put(method,tmp);
			}
			int j = 0;
			for (j = 0; j < _methods.size(); ++j){
	//			System.out.print(methods.get(j)+" ");

				if (outGraph.containsKey(_methods.get(j)))
					outGraph.get(_methods.get(j)).add(method);//将method的值加入outgruph（保持树结构）
				else{
					LinkedList<String> tmp = new LinkedList<String>();
					tmp.add(method);
					outGraph.put(_methods.get(j),tmp);
				}
			}
	//		System.out.println(" ");
		}
//		System.out.println(outGraph);

		HashMap<String,Integer> indegrees = new HashMap<String,Integer>();
		int totalIndegree = 0;
		for (Entry<String,LinkedList<String>> et:inGraph.entrySet()){
			String key = et.getKey();
			LinkedList<String> value = et.getValue();
			totalIndegree += value.size();
			indegrees.put(key,value.size());//key和value.size（）
		}

		while (totalIndegree > 0){
			boolean find = false;
			for(Entry<String,Integer> et:indegrees.entrySet()){
				String key = et.getKey();
				Integer value = et.getValue();
				if (value.intValue() == 0){//ingraph的valuesize为空的加入res
					res.add(key);
					LinkedList<String> influenced = outGraph.get(key);//这里面不是没有嘛？？
					for (String i : influenced){
						int tmp = indegrees.get(i);
						if (tmp > 0){
							tmp--;
							totalIndegree--;
//System.out.println("ABC " + totalIndegree);
						}
						indegrees.put(i,tmp);
					}
					indegrees.put(key,-1);
					find = true;
				}
			}

			if (find == false){
				for (Entry<String,Integer> et:indegrees.entrySet()){
					String key = et.getKey();
					int value = et.getValue();
					if (value != -1 && value != 0){//value.size大于零的放进去
						res.add(key);
						totalIndegree -= value;
//System.out.println("EDF " + totalIndegree);
						LinkedList<String> influenced = outGraph.get(key);
						for(String i:influenced){
							int tmp = indegrees.get(i);
							if (tmp > 0){
								tmp--;
								totalIndegree--;
//System.out.println("SSS " + totalIndegree);
							}
							indegrees.put(i,tmp);
						}
						indegrees.put(key,-1);
						break;
					}
				}
			}
		}
		for(Entry<String,Integer> et:indegrees.entrySet()){//如果入度为零，把所有点放入了
			String key = et.getKey();
			Integer value = et.getValue();
			if(value.intValue() != -1){
				res.add(key);
			}
		}
//		System.out.println(res);
		return res;
	}

	private void parseWords(EnumDeclaration n){
		codes.add(n.getName());
		List<EnumConstantDeclaration> eds = n.getEntries();
		if (eds != null){
			for (EnumConstantDeclaration ed : eds) {
				codes.add(ed.getName());
			}
		}
		
		VisitMethodCall eVisit = new VisitMethodCall(ownPackage,ownImport,classNameIndex,variableType,classFileRep);
		eVisit.visit(n,null);
		LinkedList<String> eCode = eVisit.getCodes();
		for (int tmp = 0; tmp < eCode.size(); ++tmp)
			codes.add(eCode.get(tmp));
	}

	private int judgeClass(String type){//在ownImport内找类的位置
		int i;
		int pos;
		for (i = 0; i < ownImport.size(); ++i){
			String impo = ownImport.get(i);
			pos = impo.lastIndexOf('.');
			if (pos != -1){
				if (type.equals(impo.substring(pos+1,impo.length())))
					return i;
			}
		}

		return -1;
	}

	private String eliminateSymbol(String name){
		int pos = name.indexOf('[');
		if (pos == -1)
			return name;
		else return name.substring(0,pos);
	}
	private String getClassFullName(String type){
		int pos = judgeClass(type);
		if (pos != -1)
			return ownImport.get(pos)+"."+type;
		else {
			String classString = ownPackage+"."+type;
			if (classNameIndex.containsKey(classString))
				return classString;
			else return null;
		}
	}

	private String getClassName(String type){

		if (type == null)
			return null;

		int pos = judgeClass(type);
                if (pos != -1)
                        return ownImport.get(pos);
		else return ownPackage+"."+type;
	}

	private void parseWords(ClassOrInterfaceDeclaration t){
	
		String extendsName = null;
	
		if (className == null)
			className = ownPackage+"."+t.getName().toString();//包+类名	
//		codes.add("class");//.类名
                codes.add(className);//
		List<ClassOrInterfaceType> exts = t.getExtends();//继承？？
		List<ClassOrInterfaceType> imps = t.getImplements();
		if (exts != null){
			for (ClassOrInterfaceType p : exts) {
				//codes.add("ExtendClass"+p.getName());
				extendsName = getClassName(p.getName().toString());//继承
		//		System.out.println(className +" | "+extendsName);
//				System.out.println(getClassName(p.getName().toString()));
			}
		}
		if (imps != null) {
			for (ClassOrInterfaceType p : imps) {
				//codes.add("imps");//接口
                                //codes.add("imp"+p.getName());//接口
			}
		}

//                int a = 0;
//                        System.out.println(t.getName());
                        for (Node nd:t.getChildrenNodes())//孩子结点？
                        {       //a++; System.out.println(a);
//                                System.out.println(nd.getClass().toString());
//                                System.out.println(nd);
                                if (nd instanceof FieldDeclaration){//域？
					String fullName = null;
					FieldDeclaration fd = (FieldDeclaration) nd;
					VisitMethodCall fVisit = new VisitMethodCall(ownPackage,ownImport,classNameIndex,variableType,classFileRep);
					fVisit.visit(fd,null);
					LinkedList<String> fieldCode = fVisit.getCodes();//
					for (int tmp = 0; tmp < fieldCode.size();++tmp)
						//codes.add("fieldCode"+ tmp);//
                                               codes.add(fieldCode.get(tmp));//



					Type ft = fd.getType();
					if (ft != null){
						//codes.add("ft");
                                                codes.add("type"+ft.toStringWithoutComments());
						fullName = getClassFullName(eliminateSymbol(ft.toStringWithoutComments()));
					}
					List<VariableDeclarator> vds = fd.getVariables();//方法的变量列表
					if (vds != null){
						for (VariableDeclarator vd : vds) {
							VariableDeclaratorId vid = vd.getId();//
							if (vid != null){
								//codes.add("vid");
                                                                codes.add("vid"+vid.getName());
								if (fullName != null)
									variableType.put(eliminateSymbol(vid.getName()),fullName);//找到方法，放入变量名
							}
						}
					}
                                }
                                else if (nd instanceof ClassOrInterfaceDeclaration){//接口
					parseWords((ClassOrInterfaceDeclaration) nd);
                                }
                                else if (nd instanceof EnumDeclaration){//枚举类型
					parseWords((EnumDeclaration) nd);
                                }
                                else if (nd instanceof ConstructorDeclaration){//构造函数
					ConstructorDeclaration md = (ConstructorDeclaration) nd;


//-----------------------------------------------------------------------------------------------
//生成parse tree
/*
List<List<String>> list = SplitTool.split(md.getName().toString());
String tags = "";
for (String str : list.get(0)){
	Bmcl.vocabulary.addLanguage(str);
	tags += str + " ";
}
if (md.getBlock() != null){
	try{
		GenParseTree a = new GenParseTree(md.getBlock().toString(),Bmcl.dim,Bmcl.pcfg,className,variableType,ownImport,ownPackage,classNameIndex,extendsName,classFileRep);
		Bmcl.pcfg = a.getPCFG();
		Bmcl.codeParseTrees.put(tags,a);
		for (Entry<String,Integer> en : a.getVocabulary().getCodeWordToIndex().entrySet()){
			Bmcl.vocabulary.addCode(en.getKey());
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}
*/
//pairs.put(tags,md.getBlock().toString());


//-----------------------------------------------------------------------------------------------

					List<Parameter> ps = md.getParameters();
					List<NameExpr> ts = md.getThrows();
					Element oneMethod = methods.addElement("method").addAttribute("name",className+"."+md.getName().toString()).addAttribute("filepath",filePath);
					
//System.out.println(md.getName());
					//codes.add(md.getName());
					LinkedList<String> cThisCodes = new LinkedList<String>();
					for (Node cd : md.getChildrenNodes()){
						if (cd instanceof BlockStmt){
							BlockStmt bd = (BlockStmt) cd;
							VisitVariableDeclaration cVisit = new VisitVariableDeclaration(ownPackage,ownImport,classNameIndex,variableType,classFileRep,oneMethod,className,extendsName);
							cVisit.visit(bd,null);
							oneMethod = cVisit.getOneMethod();
							//cThisCodes = cVisit.getCodes();
//for (int iji = 0; iji < sscode.size();++iji)
//System.out.println(sscode.get(iji));

							LinkedList<String> method_call_graph = cVisit.getMethodCallGraph();
							if (methodCallGraph.containsKey(className+"."+md.getName().toString())){
								for(int j = 0; j < method_call_graph.size(); ++j){
									if (!methodCallGraph.get(className+"."+md.getName().toString()).contains(method_call_graph.get(j)))
										methodCallGraph.get(className+"."+md.getName().toString()).add(method_call_graph.get(j));
								}
							}
							else{//类名+构造函数+
								methodCallGraph.put(className+"."+md.getName().toString(),method_call_graph);
							}
						}
					}
					
					cThisCodes.add(md.getName());
					if (ps != null) {
						for (Parameter p : ps) {
							Type type = p.getType();
							if (type != null)
								cThisCodes.add(type.toStringWithoutComments());
							VariableDeclaratorId vid = p.getId();
							if (vid != null)
								cThisCodes.add(vid.getName());
						}
					}

//抛出的异常					if (ts != null) {
//						for (NameExpr ne : ts)
//							cThisCodes.add(ne.getName());
//					}
					String methodName = className+"."+md.getName().toString();
					if (!Bmcl.methodCodes.containsKey(methodName))
						Bmcl.methodCodes.put(methodName,cThisCodes);
					else{
						for (int j = 0; j < cThisCodes.size(); ++j){
							Bmcl.methodCodes.get(methodName).add(cThisCodes.get(j));
						}
					}
                                }
                              else if (nd instanceof MethodDeclaration)       
                                {

					MethodDeclaration md = (MethodDeclaration) nd; 

//------------------------------------------------------------------------------------------------
//生成parse tree
/*
List<List<String>> list = SplitTool.split(md.getName().toString());
String tags = "";
for (String str : list.get(0)){
        Bmcl.vocabulary.addLanguage(str);
        tags += str + " ";
}
if (md.getBody() != null){
	try{
		GenParseTree a = new GenParseTree(md.getBody(),Bmcl.dim,className,variableType,ownImport,ownPackage,classNameIndex,extendsName,classFileRep,className+"."+md.getName().toString());

        	//Bmcl.pcfg = a.getPCFG();
		Bmcl.codeParseTrees.put(className+"."+md.getName().toString(),a);
		Bmcl.functionTags.put(className+"."+md.getName().toString(),tags);
	}catch(Exception e){
	        e.printStackTrace();
	}
}
*/

//------------------------------------------------------------------------------------------------

//System.out.println("GLOBAL: " + variableType); 
					//获取函数的名字 
//					String methodName = ((MethodDeclaration)nd).getName().toString(); 
					LinkedList<String> thisCodes = new LinkedList<String>();
					Element oneMethod = methods.addElement("method").addAttribute("name",className+"."+md.getName().toString()).addAttribute("filepath",filePath);
//                                      System.out.println(((MethodDeclaration)nd).getName()); 
//                                      System.out.println(md.getChildrenNodes());      
					for (Node cd : md.getChildrenNodes()) {       
					//子节点分别为函数的返回值类型、参数类型 
//                                               System.out.println(cd.getClass().toString()); 
						if (cd instanceof BlockStmt) { 
							BlockStmt bd = (BlockStmt) cd;
 //System.out.println(((BlockStmt)cd).getAllContainedComments()); 
							VisitVariableDeclaration visit = new VisitVariableDeclaration(ownPackage,ownImport,classNameIndex,variableType,classFileRep,oneMethod,className,extendsName); 
							visit.visit(bd,null); 
							oneMethod = visit.getOneMethod();
							//thisCodes = visit.getCodes();
//for (int ii = 0; ii < ssscode.size(); ++ii)
//System.out.println(ssscode.get(ii)); 
//							System.out.println("METHOD: " + visit.getVariableType());
				
							LinkedList<String> method_call_graph = visit.getMethodCallGraph();
                                                        if (methodCallGraph.containsKey(className+"."+md.getName().toString())){
                                                                for(int j = 0; j < method_call_graph.size(); ++j){
									if (!methodCallGraph.get(className+"."+md.getName().toString()).contains(method_call_graph.get(j)))
                                                                        	methodCallGraph.get(className+"."+md.getName().toString()).add(method_call_graph.get(j));
                                                                }
                                                        }
                                                        else{
                                                                methodCallGraph.put(className+"."+md.getName().toString(),method_call_graph);
                                                        }

						 } 
//                                                else System.out.println(cd); 
					} 
					List<Parameter> ps = md.getParameters(); 
					Type type = md.getType(); 
					if (type != null)
						thisCodes.add(type.toStringWithoutComments());
//					List<NameExpr> ts = md.getThrows();
                                        
					thisCodes.add(md.getName());
					if (ps != null){
						for (Parameter p:ps) {
							Type ty = p.getType();
							if (ty != null)
								thisCodes.add(ty.toStringWithoutComments());

							VariableDeclaratorId vid = p.getId();
							if (vid != null)
								thisCodes.add(vid.getName());
						}
					}
//抛出的异常
//					if (ts != null){
//						for (NameExpr ne : ts)
//							thisCodes.add(ne.getName());
//					}
					
					String methodName = className+"."+md.getName().toString();
                                        if (!Bmcl.methodCodes.containsKey(methodName))
                                                Bmcl.methodCodes.put(methodName,thisCodes);
                                        else{ 
                                                for (int j = 0; j < thisCodes.size(); ++j){
                                                        Bmcl.methodCodes.get(methodName).add(thisCodes.get(j));
                                                }
                                        }

                                }
                      }
//                      for (BodyDeclaration b:t.getMembers())  
//                      {//每个member是一个函数的全部内容或者一个变量的声明
//                              a++;
//                              System.out.println(a);
//                              System.out.println(b.getComment());
//                              System.out.println(b);
//                      }

/*              SourceFileLowLevelExtractor lowEx = new SourceFileLowLevelExtractor();
                lowEx.visit(cu,null);
*/
        }
}
