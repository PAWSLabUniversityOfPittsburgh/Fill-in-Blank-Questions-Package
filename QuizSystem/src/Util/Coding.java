package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;

import bean.Jquiz;

public class Coding {

	/**
	 * generate the code automatically
	 * 
	 * @param jquiz
	 * @return the code from the database, which has replaced the param_ to the
	 *         Integer number
	 */
	public static String getCode(Jquiz jquiz) {
		String codeStr;
		int min = jquiz.getMinVar();
		int max = jquiz.getMaxVar();
		int p = min + (int) new Random().nextInt((max - min) + 1);
		byte[] codeByte = null;
		;
		try {
			codeByte = jquiz.getCode().getBytes(1, (int) jquiz.getCode().length());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		codeStr = new String(codeByte);
		codeStr = codeStr.replace("\r\n", "\n").replace("\r", "\n").replaceAll(">", "&gt;")
				.replaceAll("\"", "&quot;");
		codeStr = codeStr.replace("_Param", Integer.toString(p));
		if (!codeStr.endsWith("\n"))
			codeStr += "\n";
		return codeStr;
	}

	/**
	 * transfer some characters of code so that it can be recognized by compiler
	 * 
	 * @param code
	 * @return
	 */
	public static String code2Exec(String code) {
		return code.replaceAll("&lt;", "<").replaceAll("&quot;", "\"").replaceAll("&gt;", ">");
	}

	/**
	 * transfer some characters of code so that it can be recognized by
	 * front-end
	 * 
	 * @param codeStr
	 * @return
	 */
	public static String code2fontend(String codeStr) {
		return codeStr.replaceAll("/r/n", "<br/>").replaceAll("/n", "<br/>").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;").replaceAll(" ", "&nbsp;");
	}

	/**
	 * Split the code into three parts, the first and second part are the codes
	 * which will show on the screen the third one is the missing code
	 * 
	 * @param code
	 * @return
	 */
	public static String spilitCode(String code, int start, int end,String language) {
		String str=null;
		if("java".equals(language)){
			str="//";
		}else{
			str="#";
		}
		List<Integer> list = Tool.getAllSemicolon(code);
		String newCodes=null;
		if (start == end) {
			newCodes = code.substring(0, list.get(start - 1));
			newCodes += "\n"+str+"write your code here\n\n"+str+"end" + code.substring(list.get(end));
		} else {
			newCodes = code.substring(0, list.get(start - 1));
			newCodes += "\n"+str+"write your code here\n\n"+str+"end" + code.substring(list.get(end));
		}
		return newCodes;
	}

	/**
	 * get the commend to compile the code
	 * 
	 * @param window
	 *            true/false. determine this is Win OS or Mac OS?
	 * @param compilerHomeDir
	 *            get it from web.xml
	 * @param curActionDir
	 *            An action file path which is generated by
	 *            createActionFile(...) function in FileOperation class
	 * @param testerSurfix
	 *            An string which is generated by createTempFile(...) function
	 *            in FileOperation class
	 * @param language
	 *            java or py
	 * @param multiClassFlag
	 *            whether it has one than one class
	 * @return The compile String
	 */
	public static String getCmd2compile(boolean window, String compilerHomeDir, String curActionDir,
			String testerSurfix, String language, boolean multiClassFlag) {
		String cmd = null;
		if (language.equals("java")) {
			if (!multiClassFlag) {
				cmd = (window ? "cmd /c " : "") + compilerHomeDir + "javac " + curActionDir + "Tester" + testerSurfix
						+ ".java";
			} else {
				if (!multiClassFlag) {
					cmd = (window ? "cmd /c " : "") + compilerHomeDir + "javac " + curActionDir + "Tester"
							+ testerSurfix + ".java";
				} else {
					cmd = (window ? "cmd /c " : "") + compilerHomeDir + "javac -classpath " + curActionDir + " "
							+ curActionDir + "Tester1" + testerSurfix + ".java";
				}
			}
		} else {
			cmd = compilerHomeDir + "python " + curActionDir + "Tester" + testerSurfix + ".py";
		}
		return cmd;
	}

	/**
	 * get the execute query according to the compile string which is get by
	 * getCmd2compile funcions
	 * 
	 * @param window
	 *            true/false. determine this is Win OS or Mac OS?
	 * @param compilerHomeDir
	 *            get it from web.xml
	 * @param curActionDir
	 *            An action file path which is generated by
	 *            createActionFile(...) function in FileOperation class
	 * @param testerSurfix
	 *            An string which is generated by createTempFile(...) function
	 *            in FileOperation class
	 * @param language
	 *            java or py.
	 * @param multiClassFlag
	 *            multiClassFlag whether it has one than one class
	 * @return the execute query
	 */
	public static String getCmd2execute(boolean window, String compilerHomeDir, String curActionDir,
			String testerSurfix, String language, boolean multiClassFlag) {
		String cmd = null;
		if (language.equals("java")) {
			if (!multiClassFlag) {
				cmd = compilerHomeDir + "java -classpath " + curActionDir + " Tester" + testerSurfix;
			} else {
				cmd = compilerHomeDir + "java -classpath " + curActionDir + " Tester1" + testerSurfix;
			}
		} else {
			cmd = compilerHomeDir + "python " + curActionDir + "Tester" + testerSurfix + ".py";
		}
		return cmd;
	}

	/**
	 * run the code
	 * 
	 * @param compile
	 *            The compile string that generated by the getCmd2compile()
	 *            function
	 * @param exec
	 *            The execute string that generated by the getCmd2execute()
	 *            function
	 * @param multiClassFlag
	 *            whether this kind of quiz has several classes
	 * @param curActionDir
	 *            An action file path which is generated by
	 *            createActionFile(...) function in FileOperation class
	 * @param language
	 *            java or py
	 * @param testerSurfix
	 *            An string which is generated by createTempFile(...) function
	 *            in FileOperation class
	 * @param queType
	 *            The type of this quiz, we can get it from database
	 * @param size
	 *            how many extra classes this quiz has
	 * @param fileNames
	 *            the names of extra classes
	 * @param extraClassDir
	 *            the directory of extra class
	 * @param out
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the result of this code
	 */
	public static String runCode(String compile, String exec, boolean multiClassFlag, String curActionDir,
			String language, String testerSurfix, Integer queType, int size, ArrayList<String> fileNames,
			String extraClassDir) throws IOException, InterruptedException {
//		String correctAns = "";
		File singleClassMainFile = new File(curActionDir + "Tester" + testerSurfix + "." + language);
		File multiClassMainFile = new File(curActionDir + "Tester1" + testerSurfix + "." + language);
//		if(!singleClassMainFile.exists()){
//			singleClassMainFile.createNewFile();
//		}
		if (!multiClassFlag && singleClassMainFile.exists()) {
			Process p = Runtime.getRuntime().exec(compile);
			p.waitFor();

			if (p.exitValue() != 0)
				System.err.println("ERROR: online compile failed: " + compile + ", exitValue: " + p.exitValue());
			else if (p.waitFor() != 0)
				System.err.println("ERROR: online compile is not finished yet: " + compile);
			else {
				Process p1 = Runtime.getRuntime().exec(exec);
				p1.waitFor();
				//start
//				final ExecutorService execService = Executors.newFixedThreadPool(1);  
//				Callable<String> call = new Callable<String>() {  
//		            public String call() throws Exception { 
//		            	return "";
//		            }  
//		        }; 
//		        
//		        try {  
//		            Future<String> future = execService.submit(call);  
//		            String obj = future.get(1000 * 5, TimeUnit.MILLISECONDS); //5s 
//		            System.out.println("complete the task:" + obj);  
//		        } catch (TimeoutException ex) {  
//		            System.out.println("time out....");  
//		            ex.printStackTrace();  
//		        } catch (Exception e) {  
//		            System.out.println("fail.");  
//		            e.printStackTrace();  
//		        }  
//		        execService.shutdown();
		        
		        InputStreamReader reader = new InputStreamReader(p1.getInputStream());
				byte[] correctAnsInByte = IOUtils.toByteArray(reader, "UTF-8");
				String correctAns = new String(correctAnsInByte, "UTF-8");
				if ((queType != null) && (queType != 2)) {
					if (correctAns.endsWith("\r\n"))
						correctAns = correctAns.substring(0, correctAns.length() - 2);
					else if (correctAns.endsWith("\r") || correctAns.endsWith("\n"))
						correctAns = correctAns.substring(0, correctAns.length() - 1);
				}
				correctAns = correctAns.replace("\r", "\n").replace("\n\n", "\n").replace("\n", FileOperation.replace);
//				InputStream error=p.getErrorStream();
//				byte[]bys=new byte[20124];
//				int len=0;
//				while((len=error.read(bys))!=-1){
//					System.out.println(new String(bys,0,len));
//				}
				return correctAns; 
			}
		} else if (multiClassFlag && multiClassMainFile.exists()) {
			File extraClassFile;
			File outputFile;
			String[] tempRealFiles = new String[size];
			for (int i = 0; i < size; i++) {
				String Name = fileNames.get(i).toString();
				extraClassFile = new File(extraClassDir + Name);
				
				//##############it seems to check whether the fileName starts with number or not
//				if (fileNames.get(i).toString().substring(0, 1).equals("0")) {
//					String RealFileName = fileNames.get(i).toString().substring(2,
//							fileNames.get(i).toString().length());
//					outputFile = new File(curActionDir + RealFileName);
//					tempRealFiles[i] = RealFileName;
//				} else if (fileNames.get(i).toString().substring(0, 1).equals("1")) {
//					String RealFileName = fileNames.get(i).toString().substring(2,
//							fileNames.get(i).toString().length());
//					outputFile = new File(curActionDir + RealFileName);
//					tempRealFiles[i] = RealFileName;
//				} else {
//					outputFile = new File(curActionDir + fileNames.get(i));
//					tempRealFiles[i] = fileNames.get(i);
//				}
				
				int start=0;
				for(int j=0;j<Name.length();j++){
					if(Character.isDigit(Name.charAt(j))){
						start++;
					}else{
						break;
					}
				}
				String RealFileName=Name.substring(start);
				System.out.println(RealFileName);
				outputFile = new File(curActionDir + RealFileName);
				tempRealFiles[i] = RealFileName;
				
				FileInputStream input = new FileInputStream(extraClassFile);
				FileOutputStream output = new FileOutputStream(outputFile);
				int c;
				while ((c = input.read()) != -1)
					output.write(c);

				input.close();
				output.close();
			}
			Process p2 = Runtime.getRuntime().exec(compile);
			p2.waitFor();

			if (p2.exitValue() != 0)
				FileOperation.log("ERROR: Online compilation/interpretation failed: " + compile + "\nexitValue: "
						+ p2.exitValue());
			else if (p2.waitFor() != 0)
				FileOperation.log(
						"ERROR: Online compilation/interpretation with importing class(es) is not finished yet!");
			else {
				Process p3 = Runtime.getRuntime().exec(exec);
				p3.waitFor();

//				final ExecutorService execService = Executors.newFixedThreadPool(1);  
//				Callable<String> call = new Callable<String>() {  
//		            public String call() throws Exception { 
//		            	return "";
//		            }  
//		        }; 
//		        
//		        try {  
//		            Future<String> future = execService.submit(call);  
//		            String obj = future.get(1000 * 5, TimeUnit.MILLISECONDS); //5s 
//		            System.out.println("complete the task:" + obj);  
//		        } catch (TimeoutException ex) {  
//		            System.out.println("time out....");  
//		            ex.printStackTrace();  
//		        } catch (Exception e) {  
//		            System.out.println("fail.");  
//		            e.printStackTrace();  
//		        }  
//		        execService.shutdown();
				
				InputStreamReader reader = new InputStreamReader(p3.getInputStream());
				byte[] correctAnsInByte = IOUtils.toByteArray(reader, "UTF-8");
				String correctAns = new String(correctAnsInByte, "UTF-8");
				correctAns = correctAns.replace("\r", "\n").replace("\n\n", "\n").replace("\n", "___");
				return correctAns;
			}
		}
		return "";
		
	}
}
