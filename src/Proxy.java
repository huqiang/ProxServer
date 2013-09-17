import java.io.*;
import java.net.*;
import java.util.*;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;


public class Proxy{
	public static void main(String[] args) throws Exception{
		int portNumber=8989;
		String toLanguage = null;
		boolean loadImage = true;
		boolean run = true;
		boolean mobile = false;
		if(args.length >= 1)
			portNumber = Integer.parseInt(args[0]);
		for(int i = 0; i < args.length; i++){
			if(args[i].trim().toLowerCase().equalsIgnoreCase("-noimage"))
				loadImage = false;
			if(args[i].trim().equalsIgnoreCase("-t")){
				toLanguage = args[i+1].trim();
			}
			if(args[i].trim().equalsIgnoreCase("-mobile")){
				mobile = true;
			}
		}
		System.out.println("Server is running at port: "+portNumber+" "+toLanguage+" "+"loadimage?:"+loadImage);
		try{
			int counter = 1;
			ServerSocket s = new ServerSocket(portNumber);

			while(run){
				Socket incoming = s.accept();
				Runnable r = new ProxyHandler(incoming, counter, toLanguage, loadImage, mobile);
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
	boolean loadImage = true;
	boolean asMobile = false;

	public ProxyHandler(Socket i, int c, String to, boolean image, boolean mobile){
		this.incoming = i;
		this.counter = c;
		toLanguage = to;
		loadImage = image;
		asMobile = mobile;
	}

	public void run(){
		try{
			//			This acts as a server
			System.out.println("A new thread: "+counter);
			System.out.println("Required language is:"+(toLanguage !=null)+"*****");
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
//						404, since the use may change the filter lists, and will be able to view the site in the future.
						outToClient.println("HTTP/1.0 404 Not Found\n\n");
						outToClient.println("This webpage is blocked");
						//						incoming.close();
						System.out.println("404 sent to: "+counter);
						break;
					}
					else if(urlStr.toLowerCase().contains("jpg") ||urlStr.toLowerCase().contains("jpeg") ||
							urlStr.toLowerCase().contains("png") || urlStr.toLowerCase().contains("gif")){
						System.out.println("Loading an image!");
						if (loadImage)getImage(urlStr);
						break;
					}
					else{
						System.out.println("Loading an text!");
						getText(urlStr);
						break;
					}
				}
			}
			incoming.close();
		}
		catch (IOException e){
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void getText(String urlStr) {
		// TODO Auto-generated method stub
		try{
			OutputStream outStreamToClient = incoming.getOutputStream();
			PrintWriter outToClient = new PrintWriter(outStreamToClient, true);

			URL url = new URL(urlStr);
			try {
				Socket s = new Socket(url.getHost(),80);

				OutputStreamWriter outToServer = new OutputStreamWriter(s.getOutputStream());
				String agent = (asMobile)?"Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405":"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1";
				outToServer.write("GET " + url.getFile() + " HTTP/1.0\r\nHost: " + url.getHost() + "\nUser-Agent: "+agent+"\n\n");
				outToServer.flush();

				if (toLanguage != null && !(url.getFile().contains(".js")) && !(url.getFile().contains(".css"))){
					String[] response = getResopnseString(s.getInputStream());
//					System.out.println("REsponse line 89***************");
//					System.out.println(response[0]);
//					System.out.println("REsponse line 91***************");
//					System.out.println(response[1]);
					//						if(text.length() < 1024){
					System.out.println("Start translating!");
					try{
						String translatedText = translate(response[1]);
//						String translatedText = translate(getResStringv2(s.getInputStream()));
						System.out.println("translated line 101***************");
//						System.out.println(translatedText);

						outToClient.println(response[0]);
						outToClient.print(translatedText);
					}catch(Exception e){
						outToClient.println(response[0]);
						outToClient.print(response[1]);
					}
					outToClient.print(response[0]);
					outToClient.print(response[1]);
				}else{
					Scanner sc = new Scanner(s.getInputStream());
					while(sc.hasNextLine()){
						outToClient.println(sc.nextLine());
					}
				}
				//				incoming.close();
				s.close();
				System.out.println("Closed thread: "+counter);
			}
			catch(UnknownHostException e){
				outToClient.println("HTTP/1.0 404 Not Found\n\n");
				outToClient.println("The Host is known");
				//				incoming.close();
				System.out.println("Known host error sent to: "+counter);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				outToClient.println("HTTP/1.0 500 Internal Server Error\n\n");
				outToClient.println("Oops! Something wrong happens at server, you may consider to restart it!");
				//				incoming.close();
				System.out.println("Known host error sent to: "+counter);
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			//			incoming.close();
			e.printStackTrace();
		}
	}

	private String translate(String string) throws Exception {
		// TODO Auto-generated method stub
		Translate.setClientId("CLIENT ID");
		Translate.setClientSecret("CLIENTSECRET FROM BING TRANSLATE");
		String translatedText = "";
		int c = string.length()/10200;
		for (int i = 0; i < c; i++){
			translatedText += Translate.execute(string.substring(i*10200, (i+1)*10200-1), Language.valueOf(toLanguage.toUpperCase()));
		}
		translatedText += Translate.execute(string.substring(c*10200), Language.valueOf(toLanguage.toUpperCase()));
		return translatedText;
	}
	private String getResStringv2(InputStream inputStream){
		Scanner sc = new Scanner(inputStream);
		String result = "";
		while(sc.hasNext())
			result += sc.nextLine()+'\n';
		return result;
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

	private void getImage(String urlStr) {
		try {

			URL url = new URL(urlStr);
			String webHost = url.getHost();
			System.out.println(webHost);
			String file = url.getFile();

			Socket clientSocket = new Socket(webHost, 80);
			System.out.println("Socket opened to " + webHost + "\n");

			OutputStreamWriter outWriter = new OutputStreamWriter(clientSocket.getOutputStream());
			outWriter.write("GET " + file + " HTTP/1.0\r\nHost: " + webHost + "\n\n");
			outWriter.flush();

			InputStream in = clientSocket.getInputStream();

			DataInputStream dis = new DataInputStream(in);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int ch;

			while ((ch = dis.read()) != -1) {
				baos.write(ch);
			}

			byte[] httpResponse = baos.toByteArray();
			int contentStart = 0;
			for (int i = 3; i < httpResponse.length; i++) {
				if (httpResponse[i - 2] == 10 && httpResponse[i - 1] == 13 && httpResponse[i] == 10) {
					contentStart = i + 1;
				}
			}

			incoming.getOutputStream().write(httpResponse, contentStart, httpResponse.length - contentStart);

			//			incoming.close();
			clientSocket.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
}