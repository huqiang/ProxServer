CS3103 Programming Assignment 1
Sem1 AY1213

Hu Qiang
A0077857

1,Development Platform:
	Mac OS X 10.8.2 Mountain Lion
	
	Java:
		java version "1.6.0_37"
		Java(TM) SE Runtime Environment (build 1.6.0_37-b06-434-11M3909)
		Java HotSpot(TM) 64-Bit Server VM (build 20.12-b01-434, mixed mode)
	
	Eclipse IDE for Java Developers
		Version: Juno Service Release 1
		Build id: 20121004-1855
		
2, Third party library and resource: 
	1):	microsoft-translator-java-api @ http://code.google.com/p/microsoft-translator-java-api/
		It is a Java wrapper for the Microsoft Translator API
		
	2):	Microsoft Translator:
		
4,	Execution instruction:
	The arguments are:
		portNumber:	it should be the first argument if arguments are specified, if not port number is 
					inputed, it will run at default port 8989.
		-nonimage:	if put, the Proxy server will discard requests to .jpg, .jpeg, .png, and .gif files
		-t [target language]:	translate request page using Microsoft Translator API.
		-mobile:	let the browser act as a mobile browser(iPad)
		
5,	Features:
		1),	Blocking image files to increase loading speed;
		2), translate to desired language (Problems to be discussed in next section)
		3), Make the browser act as mobile browser, to reduce use of flash/ads in websites that are
			optimized for mobile devices, such as Youtube. 

6,	Problems and discussions:
		1),	From projects in CS2105, I knew image file must use be read written in byte level using 
			ImputStream and OutputStream
			
		2),	I applied free subscription of Microsoft Translator Data <https://datamarket.azure.com/dataset/1899a118-d202-492c-aa16-ba21c33c06cb>
			since Google translate API is no longer free.
			It seems I have exceed my quota (2,000,000 characters).
			
			It limits per translation to 10240 bytes, and it does not always work well. 
			
		3),	Filters are hard coded, more filters can be added at method filter(String urlStr) at line 227 	
		
		4),	Streaming of video is not supported.