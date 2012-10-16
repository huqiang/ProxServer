import java.io.*;
import java.net.*;
import java.util.*;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class Proxy{
	public static void main(String[] args) throws Exception{
		int portNumber=8989;
		String toLanguage = "";
		boolean run = true;
		if(args.length == 1)
			portNumber = Integer.parseInt(args[0]);
		else if (args.length == 2){
			portNumber = Integer.parseInt(args[0]);
			toLanguage = args[1];
		}
		try{
			int counter = 1;
			ServerSocket s = new ServerSocket(portNumber);

			while(run){
				Socket incoming = s.accept();
				Runnable r = new ProxyHandler(incoming, counter, toLanguage);
				r.run();
				counter++;
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
}

class ProxyHandler implements Runnable{
	private Socket incoming;
	private int counter;
	String toLanguage;

	public ProxyHandler(Socket i, int c, String to){
		this.incoming = i;
		this.counter = c;
		toLanguage = to;
	}

	public void run(){
		try{
			//			This acts as a server
			System.out.println("A new thread: "+counter);
			InputStream inStreamFromClient = incoming.getInputStream();
			OutputStream outStreamToClient = incoming.getOutputStream();
			Scanner inFromClient = new Scanner(inStreamFromClient);
			PrintWriter outToClient = new PrintWriter(outStreamToClient, true);

			while(inFromClient.hasNextLine()){
				String line = inFromClient.nextLine();
				String urlStr = findUrl(line);
				//				System.out.println(line);
				if(urlStr != null){
					if(filter(urlStr)){
						outToClient.println("HTTP/1.0 404 Not Found\n\n");
						outToClient.println("This webpage is blocked");
						incoming.close();
						System.out.println("404 sent to: "+counter);
						break;
					}
					else{
						URL url = new URL(urlStr);
						try {
							Socket s = new Socket(url.getHost(),80);

							OutputStreamWriter outToServer = new OutputStreamWriter(s.getOutputStream());
							outToServer.write("GET " + url.getFile() + " HTTP/1.0\r\nHost: " + url.getHost() + "\nUser-Agent: Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405\n\n");
							outToServer.flush();

							if (toLanguage != null){
								String[] response = getResopnseString(s.getInputStream());
								System.out.println("REsponse line 89***************");
								System.out.println(response[0]);
								System.out.println("REsponse line 91***************");
								System.out.println(response[1]);
								//						if(text.length() < 1024){
								if (toLanguage != null){
									System.out.println("Start translating!");
									try{
										String translatedText = translate(response[1]);
										System.out.println("translated line 101***************");
										System.out.println(translatedText);

										outToClient.print(response[0]);
										outToClient.print(translatedText);
									}catch(Exception e){
										outToClient.print(response[0]);
										outToClient.print(response[1]);
									}
								}
								else{
									outToClient.print(response[0]);
									outToClient.print(response[1]);
								}
							}else{
								Scanner sc = new Scanner(s.getInputStream());
								while(sc.hasNextLine()){
									outToClient.println(sc.nextLine());
								}
							}
							incoming.close();
							s.close();
							System.out.println("Closed thread: "+counter);
							break;
						}
						catch(UnknownHostException e){
							outToClient.println("HTTP/1.0 404 Not Found\n\n");
							outToClient.println("The Host is known");
							incoming.close();
							System.out.println("Known host error sent to: "+counter);
							break;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private String translate(String string) throws Exception {
		// TODO Auto-generated method stub
		Translate.setClientId("huqiang");
		Translate.setClientSecret("U9F9jkEAbbJmQHQym1qLDkwXvbQi7jkwvtMsJQHwCv0=");
		String translatedText = "";
		int c = string.length()/10200;
		for (int i = 0; i < c; i++){
			translatedText += Translate.execute(string.substring(i*10200, (i+1)*10200-1), Language.valueOf(toLanguage.toUpperCase()));
		}
		translatedText += Translate.execute(string.substring(c*10200), Language.valueOf(toLanguage.toUpperCase()));
		return translatedText;
	}

	private String[] getResopnseString(InputStream inputStream) {
		// TODO Auto-generated method stub
		String[] result = new String[2];
		DataInputStream dis = new DataInputStream(inputStream);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int ch;
		boolean f1 = false, f2=false, f3=false;
		try {
			while ((ch = dis.read()) != -1) {
				if(ch == 10 && !f1)
					f1 = true;
				else if (ch == 13 && f1)
					f2 = true;
				else if (ch == 10 && f2)
					f3 = true;
				else f1=f2=f3=false;
				baos.write(ch);
				if(f1 && f2 && f3){
					result[0] = baos.toString();
					break;
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		baos.reset();
		try {
			while((ch = dis.read()) != -1){
				baos.write(ch);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result[1] = baos.toString();
		return result;
	}

	private boolean filter(String url) {
		// TODO Auto-generated method stub
		String[] filters={"clickads", "addclick", "baidu","torrent"};
		for(String k: filters){
			if (url.contains(k))
				return true;
		}
		return false;
	}

	private String findUrl(String line) {
		// TODO Auto-generated method stub
		String[] tokens = line.split(" ");
		for(String str:tokens){
			if (str.contains("http"))
				return str;
		}
		return null;
	}
}